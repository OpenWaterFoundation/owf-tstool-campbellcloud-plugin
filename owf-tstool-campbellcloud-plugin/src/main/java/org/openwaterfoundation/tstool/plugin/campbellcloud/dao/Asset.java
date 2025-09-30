// Asset - results from assets service

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
 * Campbell Cloud API "assets" object.
 * <pre>
[
  {
    "id": "ef8d612a-796c-4a61-94ff-fdf76dc96ff1",
    "created_by": "53c59382-87e7-4bdc-9323-5aafddeca743",
    "updated_by": "00000000-0000-4000-0000-000000000002",
    "metadata": {
      "model": "aspen10",
      "description": "Cactus Hill test site",
      "configuration": {
        "communication_threshold": 60,
        "timezone": "Etc/UTC"
      },
      "$profile": "aspen10",
      "status": "active",
      "maint_mode": false,
      "auto_update": {
        "os": true,
        "recipe": false,
        "os_version": "",
        "recipe_version": ""
      },
      "manufacturer": "Campbell Scientific",
      "name": "WET test ",
      "$version": 1,
      "state": {
        "RecipeVer": "01981f38-eb70-7455-ad9a-c0e35774aa3c",
        "Version": "1.5.0",
        "RevBoard": "0.00",
        "ProgSignature": 51122,
        "ProgState": "Running",
        "SensorAddr": "0",
        "GNSSLat": 40.58948,
        "GNSSLon": -104.9069,
        "Model": "ASPEN10-CELL",
        "OSDate": "04/14/2025",
        "OSVersion": "ASPEN10.1.5.0",
        "CellICCID": "89320420000006690703",
        "CellIMEI": "863859045563348",
        "StartTime": "2025-09-03T21:29:50.000383",
        "CellMSISDN": "",
        "CellVersion": "BG95M2LAR02A04_01.007.01.007",
        "SerialNumber": "1076696797",
        "CompileResults": "CPU:recipe.crb -- Compiled OK.\r\n",
        "SensorVersion": "",
        "GNSSFix": "3D Fix",
        "Sensor": "013CampbellCS45X 004SN=14013507",
        "RunSignature": 3587,
        "OSSignature": 50028,
        "RecipeName": "CS45X Recipe (004)",
        "RecipeID": "eeea7edd-095e-4fc5-ada0-04c4f1231e33",
        "BattType": "PN35360_PHD26650_35L_1S2P_7200",
        "GNSSAlt": 1595.6
      },
      "uid": "7G2L-W48V-SXBR",
      "serial": "7G2L-W48V-SXBR"
    },
    "updated_ts": 1758145740358,
    "created_ts": 1756916477809
  }
]
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Asset {
	/**
	 * "id"
	 */
	@JsonProperty("id")
	private String id = "";

	/**
	 * "created_by"
	 */
	@JsonProperty("created_by")
	private String createdBy = "";

	/**
	 * "updated_by"
	 */
	@JsonProperty("updated_by")
	private String updated_by = "";

	/**
	 * "metadata".
	 */
	private AssetMetadata metadata = null;
	
	/**
	 * "created_ts"
	 */
	@JsonProperty("created_ts")
	private Long createdTimestamp = null;

	/**
	 * "updated_ts"
	 */
	@JsonProperty("updated_ts")
	private Long updatedTimestamp = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public Asset () {
	}

	/**
	 * Find an asset given the assetId.
	 * @param assets the list of assets to search
	 * @param assetId asset identifier to match.
	 * @return an asset that matches the requested asset ID, or null if not found
	 */
	public static Asset findAssetForAssetId ( List<Asset> assets, String assetId ) {
		Asset foundAsset = null;
		if ( assetId == null ) {
			return null;
		}
		if ( assets != null ) {
			for ( Asset asset : assets ) {
				if ( asset.getId().equals(assetId) ) {
					foundAsset = asset;
					break;
				}
			}
		}
		return foundAsset;
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
	public Long getCreatedTimestamp () {
		return this.createdTimestamp;
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
	public AssetMetadata getMetadata () {
		return this.metadata;
	}

	/**
	 * Return the station "updated_ts".
	 * @return the station "updated_ts". 
	 */
	public Long getUpdatedTimestamp () {
		return this.updatedTimestamp;
	}

}
