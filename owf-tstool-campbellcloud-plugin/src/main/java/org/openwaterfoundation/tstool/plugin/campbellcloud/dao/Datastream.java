// Datastream - results from 'datastream' service

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
 * Campbell Cloud API "datastream" object, for example:
<pre>
[
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
public class Datastream {
	// List in the order shown above.

	/**
	 * "station_id"
	 */
	@JsonProperty("station_id")
	private String stationId = "";
	
	/**
	 * "created_ts"
	 */
	@JsonProperty("created_ts")
	private Long createdTimestamp = null;

	/**
	 * "created_by"
	 */
	@JsonProperty("created_by")
	private String createdBy = "";
	
	/**
	 * Datastream metadata.
	 */
	private DatastreamMetadata metadata = null;

	/**
	 * "asset_id"
	 */
	@JsonProperty("asset_id")
	private String assetId = "";

	/**
	 * "organization_id"
	 */
	@JsonProperty("organization_id")
	private String organizationId = "";

	/**
	 * "id"
	 */
	@JsonProperty("id")
	private String id = "";
	
	/**
	 * Constructor needed by Jackson.
	 */
	public Datastream ( ) {
	}

	/**
	 * Return the asset ID.
	 * @return the asset ID
	 */
	public String getAssetId () {
		return this.assetId;
	}

	/**
	 * Return the created by ID.
	 * @return the created by ID
	 */
	public String getCreatedBy () {
		return this.createdBy;
	}

	/**
	 * Return the created timestamp.
	 * @return the created timestamp
	 */
	public Long getCreatedTimestamp () {
		return this.createdTimestamp;
	}

	/**
	 * Return the ID.
	 * @return the ID
	 */
	public String getId () {
		return this.id;
	}

	/**
	 * Return the metadata.
	 * @return the metadata
	 */
	public DatastreamMetadata getMetadata () {
		return this.metadata;
	}

	/**
	 * Return the organization ID.
	 * @return the organization ID
	 */
	public String getOrganizationId () {
		return this.organizationId;
	}

	/**
	 * Return the station ID.
	 * @return the station ID
	 */
	public String getStationId () {
		return this.stationId;
	}
	
}