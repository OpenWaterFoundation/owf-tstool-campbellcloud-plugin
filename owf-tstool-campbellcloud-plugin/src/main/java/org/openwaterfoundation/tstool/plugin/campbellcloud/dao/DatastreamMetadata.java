// DatastreamMetadata - results from list-datastreams service, "metadata" object

/* NoticeStart

OWF TSTool Campbell Cloud Plugin
Copyright (C) 2025 Open Water Foundation

OWF TSTool Campbell Cloud Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

OWF TSTool Campbell Cloud Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with OWF TSTool Campbell Cloud Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.campbellcloud.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Campbell Cloud API "list-datastreams" "metadata" object.
<pre>
  {
    "station_id": "1bfdaf3d-f921-4fcd-88bf-33e8dabd5be2",
    "created_ts": 1695669740067,
    "created_by": "b7f5814f-df9d-43e2-b790-a6209cd84bb8",
    "metadata": {
      "status": "active",
      "aggregate_func": null,
      "table": "ClimaVUE50",
      "alias": "record@climavue50",
      "$version": 1,
      "$profile": "datastream",
      "field": "record"
    },
    "asset_id": "e4256734-c1a3-43b9-a7a1-62f040fd59d6",
    "organization_id": "867e3a14-7614-47a2-a5fa-73e831cfc170",
    "id": "018acdca-e223-7405-a060-b23851d878b3"
  },
</pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class DatastreamMetadata {
	// List in the order of the example above.
	/**
	 * "status"
	 */
	@JsonProperty("status")
	private String status = "";

	/**
	 * "aggregate_func"
	 */
	@JsonProperty("aggregate_func")
	private String aggregateFunc = "";

	/**
	 * "table"
	 */
	@JsonProperty("table")
	private String table = "";

	/**
	 * "alias"
	 */
	@JsonProperty("alias")
	private String alias = "";
	
	/**
	 * "$version"
	 */
	@JsonProperty("$version")
	private Integer version = null;

	/**
	 * "$profile"
	 */
	@JsonProperty("$profile")
	private String profile = "";

	/**
	 * "field"
	 */
	@JsonProperty("field")
	private String field = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public DatastreamMetadata () {
	}

	/**
	 * Return the datastream metadata alias.
	 * @return the datastream metadata alias. 
	 */
	public String getAlias () {
		return this.alias;
	}

	/**
	 * Return the datastream metadata field.
	 * @return the datastream metadata field. 
	 */
	public String getField () {
		return this.field;
	}

	/**
	 * Return the datastream metadata profile.
	 * @return the datastream metadata profile. 
	 */
	public String getProfile () {
		return this.profile;
	}

	/**
	 * Return the datastream metadata status.
	 * @return the datastream metadata status. 
	 */
	public String getStatus () {
		return this.status;
	}

	/**
	 * Return the datastream metadata version.
	 * @return the datastream metadata version. 
	 */
	public Integer getVersion () {
		return this.version;
	}

}