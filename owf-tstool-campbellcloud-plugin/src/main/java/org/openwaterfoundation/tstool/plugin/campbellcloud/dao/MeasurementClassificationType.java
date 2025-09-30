// MeasurementClassificationType - results from list-measurement-classification-types service

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
 * Campbell Cloud API "list-measurement-classification-types"  object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class MeasurementClassificationType {
	/**
	 * Location.
	 */
	private Location location = null;
	
	/**
	 * "$profile"
	 */
	@JsonProperty("$profile")
	private String profile = "";

	/**
	 * "$version"
	 */
	@JsonProperty("$version")
	private Integer version = null;

	/**
	 * "$assets"
	 */
	@JsonProperty("$assets")
	private List<String> assets = null;

	/**
	 * "name"
	 */
	@JsonProperty("name")
	private String name = "";

	/**
	 * "description"
	 */
	@JsonProperty("description")
	private String description = "";
	
	/**
	 * Default constructor used by Jackson.
	 */
	public MeasurementClassificationType () {
	}

	/**
	 * Return the station metadata location.
	 * @return the station metadata location. 
	 */
	public Location getLocation () {
		return this.location;
	}

	/**
	 * Return the station metadata name.
	 * @return the station metadata name. 
	 */
	public String getName () {
		return this.name;
	}

	/**
	 * Return the station metadata profile.
	 * @return the station metadata profile. 
	 */
	public String getProfile () {
		return this.profile;
	}

	/**
	 * Return the station metadata version.
	 * @return the station metadata version. 
	 */
	public Integer getVersion () {
		return this.version;
	}

}
