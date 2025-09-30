// TimeSeriesCatalog - list of time series

/* NoticeStart

OWF TSTool KiWIS Plugin
Copyright (C) 2022-2023 Open Water Foundation

OWF TSTool KiWIS Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OWF TSTool KiWIS Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OWF TSTool KiWIS Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.campbellcloud.dao;

import java.util.ArrayList;
import java.util.List;

import RTi.TS.TSIdent;

/**
 * Class to store time series catalog (metadata) for KiWIS TSTool time series list.
 * This is a combination of standard time series properties used in TSTool and KiWIS data.
 * More data may be included and shown in the table model while evaluating the web services
 * and will be removed or disabled later.
 * The types are as one would expect, whereas the 'TimeSeries' object uses strings as per web service JSON types.
 */
public class TimeSeriesCatalog {

	// General data, provided by TSTool, extracted/duplicated from KiWIS services.
	//private String locId = "";
	private String dataSource = "";
	private String dataInterval = "";
	private String dataType = "";
	private String dataUnits = ""; // From either point_type.units_abbreviated or rating_table.units_abbreviated.
	
	// Datastream data, listed alphabetically.
	private String datastreamAlias = "";
	private String datastreamField = "";
	private String datastreamId = "";
	private String datastreamStatus = "";

	// Station data, listed alphabetically.
	private String stationDescription = "";
	private Double stationElevation = null;
	private Double stationLatitude = null;
	private Double stationLongitude = null;
	private String stationId = "";
	private String stationName = "";
	
	// Asset data, listed alphabetically.
	private String assetDescription = "";
	private String assetId = "";
	private String assetManufacturer = "";
	private String assetModel = "";
	private String assetName = "";
	private String assetStatus = "";
	private Boolean assetMaintMode = null;
	
	// Time series properties.
	private String tsId = "";

	// List of problems, one string per issue.
	private List<String> problems = null; // Initialize to null to save memory ... must check elsewhere when using.

	/**
	 * Has ReadTSCatalog.checkData() resulted in problems being set?
	 * This is used when there are issues with non-unique time series identifiers.
	 * For example if two catalog are returned for a stationNumId, dataType, and dataInterval,
	 * each of the tscatalog is processed in checkData().  The will each be processed twice.
	 * This data member is set to true the first time so that the 'problems' list is only set once
	 * in TSCatalogDAO.checkData().
	 */
	private boolean haveCheckDataProblemsBeenSet = false;

	/**
	 * Constructor.
	 */
	public TimeSeriesCatalog () {
		this.dataSource = "CampbellCloud";
	}

	/**
	 * Copy constructor.
	 * @param timeSeriesCatalog instance to copy
	 */
	public TimeSeriesCatalog ( TimeSeriesCatalog timeSeriesCatalog ) {
		// Do a deep copy by default as per normal Java conventions.
		this(timeSeriesCatalog, true);
	}

	/**
	 * Copy constructor.
	 * @param timeSeriesCatalog instance to copy
	 * @param deepCopy indicates whether an exact deep copy should be made (true)
	 * or a shallow copy that is typically used when defining a derived catalog record.
	 * For example, use deepCopy=false when copying a scaled catalog entry for a rated time series.
	 */
	public TimeSeriesCatalog ( TimeSeriesCatalog timeSeriesCatalog, boolean deepCopy ) {
		// List in the same order as internal data member list.
		//this.locId = timeSeriesCatalog.locId;
		this.dataSource = timeSeriesCatalog.dataSource;
		this.dataType = timeSeriesCatalog.dataType;
		this.dataInterval = timeSeriesCatalog.dataInterval;
		this.dataUnits = timeSeriesCatalog.dataUnits;

		// Station data, listed alphabetically.
		//this.stationDescription = timeSeriesCatalog.stationDescription;
		//this.stationElevation = timeSeriesCatalog.stationElevation;
		this.stationLatitude = timeSeriesCatalog.stationLatitude;
		this.stationLongitude = timeSeriesCatalog.stationLongitude;
		this.stationId = timeSeriesCatalog.stationId;
		this.stationName = timeSeriesCatalog.stationName;
		
		if ( deepCopy ) {
			// Time series catalog problems.
			if ( timeSeriesCatalog.problems == null ) {
				this.problems = null;
			}
			else {
				// Create a new list.
				this.problems = new ArrayList<>();
				for ( String s : timeSeriesCatalog.problems ) {
					this.problems.add(s);
				}
			}
		}
		else {
			// Default is null problems list.
		}
	}

	/**
	 * Add a problem to the problem list.
	 * @param problem Single problem string.
	 */
	public void addProblem ( String problem ) {
		if ( this.problems == null ) {
			this.problems = new ArrayList<>();
		}
		this.problems.add(problem);
	}
	
	/**
	 * Clear the problems.
	 * @return
	 */
	public void clearProblems() {
		if ( this.problems != null ) {
			this.problems.clear();
		}
	}

	/**
	 * Create an index list for TimeSeriesCatalog data list, using stationNumId as the index.
	 * This is a list of lists, with outermost list being the stationNumId. 
	 * It is assumed that the catalog is sorted by stationNumId, which should be the case
	 * due to logic in the 'tscatalog' service.
	 * @param tscatalogList list of TimeSeriesCatalog to create an index for.
	 * @return the indexed TimeSeriesCatalog
	 */
	/*
	public static List<IndexedDataList<Integer,TimeSeriesCatalog>> createIndex ( List<TimeSeriesCatalog> tscatalogList ) {
		List<IndexedDataList<Integer,TimeSeriesCatalog>> indexList = new ArrayList<>();
		// Loop through the TimeSeriesCatalog list.
		Integer stationNumIdPrev = null;
		boolean newStationNumId = false;
		Integer stationNumId = null;
		IndexedDataList<Integer,TimeSeriesCatalog> stationTimeSeriesCatalogList = null;
		for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
			stationNumId = tscatalog.getStationNumId();
			newStationNumId = false;
			if ( stationNumIdPrev == null ) {
				// First station.
				newStationNumId = true;
			}
			else if ( ! stationNumId.equals(stationNumIdPrev) ) {
				// Station does not match previous so need to add to index.
				newStationNumId = true;
			}
			// Set the previous stationNumId for the next iteration.
			stationNumIdPrev = stationNumId;
			if ( newStationNumId ) {
				// New station:
				// - create a new list and add to the index list
				// - use the statinNumId for primary identifier and stationId for secondary identifier
				//stationTimeSeriesCatalogList = new IndexedDataList<>(stationNumId, tscatalog.getStationId());
				indexList.add(stationTimeSeriesCatalogList);
			}
			// Add the station to the current list being processed.
			stationTimeSeriesCatalogList.add(tscatalog);
		}
		return indexList;
	}
	*/

	/**
	 * Find a list of TimeSeriesCatalog given the time series identifier to match.
	 * @param tsidentReq requested time series identifier to match
	 * @return one or more matching time series identifiers
	 */
    public static List<TimeSeriesCatalog> findForTSIdent ( List<TimeSeriesCatalog> tscatalogList, TSIdent tsidentReq ) {
    	List<TimeSeriesCatalog> tscatalogFoundList = new ArrayList<>();
    	String locId = tsidentReq.getLocation();
    	String dataType = tsidentReq.getType();
    	String interval = tsidentReq.getInterval();
    	for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
    		if ( !tscatalog.getStationId().equals(locId) ) {
    			// Location ID is not a match.
    			continue;
    		}
    		if ( !tscatalog.getDataType().equals(dataType) ) {
    			// Data type is not a match.
    			continue;
    		}
    		if ( !tscatalog.getDataInterval().equalsIgnoreCase(interval) ) {
    			// Data interval is not a match:
    			// - comparison ignores case
    			continue;
    		}
    		// If here can add to the list.
    		tscatalogFoundList.add(tscatalog);
    	}
    	return tscatalogFoundList;
    }

	/**
	 * Format problems into a single string.
	 * @return formatted problems.
	 */
	public String formatProblems() {
		if ( this.problems == null ) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		for ( int i = 0; i < problems.size(); i++ ) {
			if ( i > 0 ) {
				b.append("; ");
			}
			b.append(problems.get(i));
		}
		return b.toString();
	}

	public String getAssetDescription ( ) {
		return this.assetDescription;
	}

	public String getAssetId ( ) {
		return this.assetId;
	}

	public Boolean getAssetMaintMode ( ) {
		return this.assetMaintMode;
	}

	public String getAssetManufacturer ( ) {
		return this.assetManufacturer;
	}

	public String getAssetModel ( ) {
		return this.assetModel;
	}

	public String getAssetName ( ) {
		return this.assetName;
	}

	public String getAssetStatus ( ) {
		return this.assetStatus;
	}

	public String getDatastreamAlias ( ) {
		return this.datastreamAlias;
	}

	public String getDatastreamField ( ) {
		return this.datastreamField;
	}

	public String getDataInterval ( ) {
		return this.dataInterval;
	}

	public String getDatastreamId ( ) {
		return this.datastreamId;
	}

	public String getDatastreamStatus ( ) {
		return this.datastreamStatus;
	}
	
	public String getDataSource ( ) {
		return this.dataSource;
	}

	public String getDataType ( ) {
		return this.dataType;
	}
	
	public String getDataUnits ( ) {
		return this.dataUnits;
	}

	/**
	 * Get the list of distinct data intervals from the catalog, for example "IrregSecond", "15Minute".
	 * @param tscatalogList list of TimeSeriesCatalog to process.
	 * The list may have been filtered by data type previous to calling this method.
	 * @return a list of distinct data interval strings.
	 */
	public static List<String> getDistinctDataIntervals ( List<TimeSeriesCatalog> tscatalogList ) {
	    List<String> dataIntervalsDistinct = new ArrayList<>();
	    String dataInterval;
	    boolean found;
	    for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
	    	// Data interval from the catalog, something like "IrregSecond", "15Minute", "1Hour", "24Hour".
	    	dataInterval = tscatalog.getDataInterval();
	    	if ( dataInterval == null ) {
	    		continue;
	    	}
	    	found = false;
	    	for ( String dataInterval2 : dataIntervalsDistinct ) {
	    		if ( dataInterval2.equals(dataInterval) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		// Add to the list of unique data types.
	    		dataIntervalsDistinct.add(dataInterval);
	    	}
	    }
	    return dataIntervalsDistinct;
	}

	/**
	 * Get the list of distinct data types from the catalog.
	 * @param tscatalogList list of TimeSeriesCatalog to process.
	 * @return a list of distinct data type strings.
	 */
	public static List<String> getDistinctDataTypes ( List<TimeSeriesCatalog> tscatalogList ) {
	    List<String> dataTypesDistinct = new ArrayList<>();
	    String dataType;
	    boolean found;
	    for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
	    	// Data type from the catalog, something like "WaterLevelRiver".
	    	dataType = tscatalog.getDataType();
	    	if ( dataType == null ) {
	    		continue;
	    	}
	    	found = false;
	    	for ( String dataType2 : dataTypesDistinct ) {
	    		if ( dataType2.equals(dataType) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		// Add to the list of unique data types.
	    		dataTypesDistinct.add(dataType);
	    	}
	    }
	    return dataTypesDistinct;
	}

	/**
	 * Return whether checkData() has resulted in problems being set.
	 * @return whether checkData() has resulted in problems being set.
	 */
	public boolean getHaveCheckDataProblemsBeenSet () {
		return this.haveCheckDataProblemsBeenSet;
	}

	/*
	public String getLocId ( ) {
		return this.locId;
	}
	*/

	public String getStationDescription ( ) {
		return this.stationDescription;
	}
	
	public Double getStationElevation ( ) {
		return this.stationElevation;
	}
	
	public String getStationId ( ) {
		return this.stationId;
	}
	
	public Double getStationLatitude ( ) {
		return this.stationLatitude;
	}
	
	public Double getStationLongitude ( ) {
		return this.stationLongitude;
	}

	public String getStationName ( ) {
		return this.stationName;
	}

	public String getTsId ( ) {
		return this.tsId;
	}

	public void setAssetDescription ( String assetDescription ) {
		this.assetDescription = assetDescription;
	}

	public void setAssetId ( String assetId ) {
		this.assetId = assetId;
	}

	public void setAssetMaintMode ( Boolean assetMaintMode ) {
		this.assetMaintMode = assetMaintMode;
	}

	public void setAssetManufacturer ( String assetManufacturer ) {
		this.assetManufacturer = assetManufacturer;
	}

	public void setAssetModel ( String assetModel ) {
		this.assetModel = assetModel;
	}

	public void setAssetName ( String assetName ) {
		this.assetName = assetName;
	}

	public void setAssetStatus ( String assetStatus ) {
		this.assetStatus = assetStatus;
	}
	public void setDataInterval ( String dataInterval ) {
		this.dataInterval = dataInterval;
	}
	
	public void setDatastreamAlias ( String datastreamAlias ) {
		this.datastreamAlias = datastreamAlias;
	}

	public void setDatastreamField ( String datastreamField ) {
		this.datastreamField = datastreamField;
	}

	public void setDatastreamId ( String datastreamId ) {
		this.datastreamId = datastreamId;
	}

	public void setDatastreamStatus ( String datastreamStatus ) {
		this.datastreamStatus = datastreamStatus;
	}
	
	public void setDataType ( String dataType ) {
		this.dataType = dataType;
	}
	
	public void setDataUnits ( String dataUnits ) {
		this.dataUnits = dataUnits;
	}

	/**
	 * Set whether checkData() has resulted in problems being set.
	 * - TODO smalers 2020-12-15 not sure this is needed with the latest code.
	 *   Take out once tested out.
	 */
	public void setHaveCheckDataProblemsBeenSet ( boolean haveCheckDataProblemsBeenSet ) {
		this.haveCheckDataProblemsBeenSet = haveCheckDataProblemsBeenSet;
	}

	/*
	public void setLocId ( String locId ) {
		this.locId = locId;
	}
	*/

	public void setStationDescription ( String stationDescription ) {
		this.stationDescription = stationDescription;
	}

	public void setStationElevation ( Double stationElevation ) {
		this.stationElevation = stationElevation;
	}

	public void setStationId ( String stationId ) {
		this.stationId = stationId;
	}
	
	public void setStationLatitude ( Double stationLatitude ) {
		this.stationLatitude = stationLatitude;
	}
	
	public void setStationLongitude ( Double stationLongitude ) {
		this.stationLongitude = stationLongitude;
	}

	public void setStationName ( String stationName ) {
		this.stationName = stationName;
	}
	
	public void setTsId ( String tsId ) {
		this.tsId = tsId;
	}

	/**
	 * Simple string to identify the time series catalog, for example for logging, using TSID format.
	 */
	public String toString() {
		//return "" + this.locId + ".." + this.dataType + "." + this.dataInterval;
		return "" + this.stationId + ".." + this.dataType + "." + this.dataInterval;
	}
}
