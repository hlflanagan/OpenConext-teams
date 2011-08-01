/*
 * Copyright 2011 SURFnet bv, The Netherlands
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

package nl.surfnet.coin.teams.interceptor;

import javax.servlet.http.HttpServletRequest;

/**
 * Like the LoginInterceptor but gets the user id from the environment instead
 * of Shibboleth.
 */
public class MockVOInterceptor extends VOInterceptor {

  @Override
  String getLoggedInVOName(HttpServletRequest request) {
    return getTeamEnvironment().getMockVOName();
  }

}