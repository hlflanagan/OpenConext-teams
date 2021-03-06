/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

COIN.Sandbox = function(core) {
	var subscribersByType = {};
	
	function subscribeToType(id, type, callback) {
		if (typeof subscribersByType[type] === 'undefined') {
			subscribersByType[type] = [];
		}
		subscribers = subscribersByType[type];
		subscribers[subscribers.length] = { id: id, callback: callback};
	}
	
	function unsubscribeByType(id, type) {
		if (typeof subscribersByType[type] === 'undefined') {
			return false;
		}
		
		for (var i=0; i< subscribersByType[type].length; i++) {
			if (subscribersByType[type][i].id === id) {
				subscribersByType[type].splice(i, 1);
				return true;
			}
		}
		return false;
	}

  function _supportsPlaceholder() {
    var testInput = document.createElement('input');
    return ('placeholder' in testInput);
  }

	return {
		log: function() {
			core.log.apply(core, arguments);
		},
		
		warn: function() {
			core.warn.apply(core, arguments);
		},
		
		alert: function() {
			window.alert.apply(window, arguments);
		},
		
		/**
		 * Publish an event, event should be something like:
		 * { type: 'custom-event', data: { id: 8, message: 'hiya!'}}
		 */
		publish: function (event) {
			if (typeof event['type'] === 'undefined') {
				this.warn('Publishing: Typeless event! Ignoring...', event);
				return false;
			}
			
			if (typeof subscribersByType[event.type] === 'undefined') {
				this.log('Publishing: No subscribers to event type', event);
				return false;
			}
			
			core.log('Publishing event: ', event);
			
			var subscribers = subscribersByType[event.type], callback;
			for (var i=0; i < subscribers.length; i++) {
				core.log('Handing it to subscriber: ', subscribers[i].id);
				callback = subscribers[i].callback;
				if (this.typeOf(callback)==='function') {
					callback(event);
				}
				else if (this.typeOf(callback)==='object') {
					callback.method.apply(callback.object, [event]);
				}
			}
		},
		
		/**
		 * Subscribe to a list of event types, when an event of a given type
		 * is published the given callback will be executed.
		 */
		subscribe: function(id, types, callback) {
			if (this.typeOf(types) !== 'array') {
				types = [types];
			}
			
			var subscribers, type;
			for (var i=0; i < types.length; i++) {
				type = types[i];
				subscribeToType(id, type, callback);
			}
		},
		
		/**
		 * Unsubscribe for a list of event types.
		 */
		unsubscribe: function(id, types) {
			if (typeof subscribersByType[type] === 'undefined') {
				return false;
			}
			if (this.typeOf(types) !== 'array') {
				types = [types];
			}
			
			var type;
			for (var i=0; i < types; i++) {
				type = types[i];
				unsubscribeByType(id, type);
			}
		},
		
		post: function(url, data, callback, type) {
			return $.post(url, data, callback, type);
		},
		
		redirectBrowserTo: function(url) {
			this.log("Redirecting browser to: " + url);
			window.location.href = url;
		},
		
		reloadPage: function() {
			window.location.reload(true);
		},

		checkBoxControl: function(id, name) {
			   $("input[name=" + name + "][type='checkbox']").attr('checked', $(id).is(':checked'));
		},

		/**
		 * Fix for broken JavaScript typeof operator (with regards to null and Arrays).
		 * 
		 * @link http://javascript.crockford.com/remedial.html
		 */
		typeOf: function(value) {
			var s = typeof value;
			if (s === 'object') {
				if (value) {
					if (value instanceof Array) {
						s = 'array';
					}
				} else {
					s = 'null';
				}
			}
			return s;
		},
		
		fixTableLayout: function(table) {
			
			if (table instanceof jQuery) {
				// Add odd and even classes to odd and even rows
				table.find('tbody tr:nth-child(even)').addClass('even');
				table.find('tbody tr:nth-child(odd)').addClass('odd');
			}
    },

    addPlaceholderSupport :function () {
      if (_supportsPlaceholder() === true) {
        return;
      }
      var active = document.activeElement;
      $(':text').focus(
          function () {
            if ($(this).attr('placeholder') != '' && $(this).val() == $(this).attr('placeholder')) {
              $(this).val('').removeClass('hasPlaceholder');
            }
          }).blur(function () {
            if ($(this).attr('placeholder') != '' && ($(this).val() == '' || $(this).val() == $(this).attr('placeholder'))) {
              $(this).val($(this).attr('placeholder')).addClass('hasPlaceholder');
            }
          });
      $(':text').blur();
      $(active).focus();
      $('form').submit(function () {
        $(this).find('.hasPlaceholder').each(function () {
          $(this).val('');
        })
      });
    }
  };
}(COIN.Core);