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

package nl.surfnet.coin.teams.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain object for a Group provider
 */
public class GroupProvider {

  private Long id;
  private String identifier;
  private String name;
  private GroupProviderType groupProviderType;
  private Map<String, Object> allowedOptions;

  public GroupProvider(Long id, String identifier, String name, String groupProviderType) {
    this.id = id;
    this.identifier = identifier;
    this.name = name;
    this.groupProviderType = GroupProviderType.fromString(groupProviderType);
    this.allowedOptions = new HashMap<String, Object>();
  }

  /**
   * @return unique identifier of the group provider
   */
  public Long getId() {
    return id;
  }

  /**
   * @return human readable unique identifier of the group provider
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * @return name of the group provider
   */
  public String getName() {
    return name;
  }

  /**
   * @return {@link GroupProviderType} type of group provider
   */
  public GroupProviderType getGroupProviderType() {
    return groupProviderType;
  }

  /**
   * @return a Map<String, Object> with possible configuration options for the Group provider
   */
  public Map<String, Object> getAllowedOptions() {
    return allowedOptions;
  }

  /**
   * Sets a Map<String,Object> with possible configuration options for the Group provider.
   *
   * @param allowedOptions allowed options for a Group provider
   */
  public void setAllowedOptions(Map<String, Object> allowedOptions) {
    this.allowedOptions = allowedOptions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GroupProvider that = (GroupProvider) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("GroupProvider");
    sb.append("{id=").append(id);
    sb.append(", identifier='").append(identifier).append('\'');
    sb.append(", name='").append(name).append('\'');
    sb.append(", groupProviderType=").append(groupProviderType);
    sb.append('}');
    return sb.toString();
  }
}