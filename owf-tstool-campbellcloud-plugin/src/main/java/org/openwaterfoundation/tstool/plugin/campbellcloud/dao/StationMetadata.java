// StationMetadata - results from list-stations service, "metadata" object

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

import RTi.Util.Message.Message;

/**
 * Campbell Cloud API "list-stations" "metadata" object.
 * <pre>
  {
    "metadata": {
      "location": {
        "elevation": 121,
        "longitude": 2.159059712412,
        "latitude": 41.414280730711
      },
      "$profile": "station",
      "$version": 1,
      "name": "Barcelona",
      "assets": [
        "76437aaf-5eee-4be6-9de8-2b4a4868c634"
      ],
      "description": "Campbell Scientific Regional Office"
    },
    "created_by": "cfd003d2-8d2c-4bc6-9eff-0b2ea62b5550",
    "id": "0055a718-2116-48a6-aec2-4dc16a4199bd",
    "created_ts": 1715682647131
  },
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class StationMetadata {
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
	@JsonProperty("assets")
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
	public StationMetadata () {
	}

	/**
	 * Clean the data.
	 * No cleanup is currently needed - this was used in troubleshooting.
	 */
	public void cleanData () {
	}

	/**
	 * Return the station metadata location.
	 * @return the station metadata location.
	 */
	public Location getLocation () {
		return this.location;
	}

	/**
	 * Return the station asset identifiers.
	 * @return the station asset identifiers.
	 */
	public List<String> getAssets () {
		return this.assets;
	}

	/**
	 * Return the station metadata description.
	 * @return the station metadata description.
	 */
	public String getDescription () {
		return this.description;
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
