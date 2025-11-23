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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Campbell Cloud API "list-stations" object.
 * <pre>
[
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
public class Station {
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
	public Station () {
	}

	/**
	 * Clean the data, for example by replacing curly single quote with straight single quote.
	 * This is helpful when an output format cannot handle such data values.
	 */
	public void cleanData () {
		if ( this.metadata != null ) {
			this.metadata.cleanData();
		}
	}

	/**
	 * Clean the data, for example by replacing curly single quote with straight single quote.
	 * This is helpful when an output format cannot handle such data values.
	 * @param stations list of stations to clean
	 */
	public static void cleanData ( List<Station> stations ) {
		if ( stations != null ) {
			for ( Station station : stations ) {
				station.cleanData ();
			}
		}
	}

	/**
	 * Find a station given the stationId.
	 * @param stations the list of stations to search
	 * @param stationId station identifier to match.
	 * @return a station that matches the requested station ID, or null if not found
	 */
	public static Station findStationForStationId ( List<Station> stations, String stationId ) {
		Station foundStation = null;
		if ( stationId == null ) {
			return null;
		}
		if ( stations != null ) {
			for ( Station station : stations ) {
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
