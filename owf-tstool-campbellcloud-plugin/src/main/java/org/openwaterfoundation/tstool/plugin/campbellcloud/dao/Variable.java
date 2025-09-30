// Station - results from list-stations service

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
 * Campbell Cloud API "list-stations" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Variable {
	/**
	 * "metadata".
	 */
	private StationMetadata metadata = null;
	
	/**
	 * "created_by"
	 */
	@JsonProperty("$profile")
	private String createdBy = "";

	/**
	 * "id"
	 */
	@JsonProperty("idversion")
	private String id = "";

	/**
	 * "created_ts"
	 */
	@JsonProperty("created_ts")
	private Long createdTs = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public Variable () {
	}

	/**
	 * Return the station "created_by".
	 * @return the station "created_by". 
	 */
	public String getCreatedBy () {
		return this.createdBy;
	}

	/**
	 * Return the station "created_ts".
	 * @return the station "created_ts". 
	 */
	public Long getCreatedTs () {
		return this.createdTs;
	}
	
	/**
	 * Return the station identifier.
	 * @return the station identifier. 
	 */
	public String getId () {
		return this.id;
	}

	/**
	 * Return the station metadata.
	 * @return the station metadata. 
	 */
	public StationMetadata getMetadata () {
		return this.metadata;
	}

}
