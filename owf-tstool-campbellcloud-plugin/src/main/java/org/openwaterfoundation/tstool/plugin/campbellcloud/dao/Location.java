// StationMetadata - results from list-stations service for "location" part

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

/**
 * Campbell Cloud API "list-stations" object "location" part.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Location {
	/**
	 * "elevation"
	 */
	private Double elevation = null;
	
	/**
	 * "latitude"
	 */
	private Double latitude = null;
	
	/**
	 * "longitude"
	 */
	private Double longitude = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public Location () {
	}

	/**
	 * Return the station elevation.
	 * @return the station elevation. 
	 */
	public Double getElevation () {
		return this.elevation;
	}

	/**
	 * Return the station latitude.
	 * @return the station latitude. 
	 */
	public Double getLatitude () {
		return this.latitude;
	}
	
	/**
	 * Return the station longitude.
	 * @return the station longitude. 
	 */
	public Double getLongitude () {
		return this.longitude;
	}

}