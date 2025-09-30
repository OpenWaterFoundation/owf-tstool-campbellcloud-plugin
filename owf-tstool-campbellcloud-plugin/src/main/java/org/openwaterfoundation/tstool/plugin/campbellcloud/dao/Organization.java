// Organization - results from list-organizations service

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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Campbell Cloud API "list-organizations" object.
 * <pre>
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Organization {
	/**
	 * "metadata".
	 */
	private StationMetadata metadata = null;
	
	/**
	 * "created_by"
	 */
	@JsonProperty("created_by")
	private String createdBy = "";

	/**
	 * "id"
	 */
	@JsonProperty("id")
	private String id = "";

	/**
	 * "created_ts"
	 */
	@JsonProperty("created_ts")
	private Long createdTs = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public Organization () {
	}

	/**
	 * Find a station given the stationId.
	 * @param stations the list of stations to search
	 * @param stationId station identifier to match.
	 * @return a station that matches the requested station ID, or null if not found
	 */
	public static Organization findStationForStationId ( List<Organization> stations, String stationId ) {
		Organization foundStation = null;
		if ( stationId == null ) {
			return null;
		}
		if ( stations != null ) {
			for ( Organization station : stations ) {
				if ( station.getId().equals(stationId) ) {
					foundStation = station;
					break;
				}
			}
		}
		return foundStation;
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
