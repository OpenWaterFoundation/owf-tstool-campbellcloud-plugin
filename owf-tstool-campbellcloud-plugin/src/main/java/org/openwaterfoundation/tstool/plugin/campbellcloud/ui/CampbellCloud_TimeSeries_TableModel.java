// CampbellCloud_TimeSeries_TableModel - table model for the time series catalog

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

package org.openwaterfoundation.tstool.plugin.campbellcloud.ui;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import org.openwaterfoundation.tstool.plugin.campbellcloud.datastore.CampbellCloudDataStore;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.TimeSeriesCatalog;

/**
This class is a table model for time series header information for Campbell Cloud web services time series.
By default the sheet will contain row and column numbers.
*/
@SuppressWarnings({ "serial", "rawtypes" })
public class CampbellCloud_TimeSeries_TableModel extends JWorksheet_AbstractRowTableModel {
	
	/**
	Number of columns in the table model.
	*/
	private final int COLUMNS = 23;

	public final int COL_STATION_ID = 0;
	public final int COL_STATION_NAME = 1;
	public final int COL_DATA_SOURCE = 2;
	public final int COL_DATA_TYPE = 3;
	public final int COL_DATA_INTERVAL = 4;
	//public final int COL_STATISTIC = 4;
	public final int COL_DATA_UNITS = 5;
	// Datastream data.
	public final int COL_DATASTREAM_ID = 6;
	public final int COL_DATASTREAM_ALIAS = 7;
	public final int COL_DATASTREAM_FIELD = 8;
	public final int COL_DATASTREAM_STATUS = 9;
	// Station data.
	public final int COL_STATION_LONGITUDE = 10;
	public final int COL_STATION_LATITUDE = 11;
	public final int COL_STATION_ELEVATION = 12;
	//public final int COL_STATION_DESCRIPTION = 9;
	// Asset.
	public final int COL_ASSET_ID = 13;
	public final int COL_ASSET_NAME = 14;
	public final int COL_ASSET_DESCRIPTION = 15;
	public final int COL_ASSET_MANUFACTURER = 16;
	public final int COL_ASSET_MODEL = 17;
	public final int COL_ASSET_STATUS = 18;
	public final int COL_ASSET_MAINT_MODE = 19;
	//
	public final int COL_TS_ID = 20;
	public final int COL_PROBLEMS = 21;
	public final int COL_DATASTORE = 22;
	
	/**
	Datastore corresponding to datastore used to retrieve the data.
	*/
	CampbellCloudDataStore datastore = null;

	/**
	Data are a list of TimeSeriesCatalog.
	*/
	private List<TimeSeriesCatalog> timeSeriesCatalogList = null;

	/**
	Constructor.  This builds the model for displaying the given Campbell Cloud time series data.
	@param dataStore the data store for the data
	@param data the list of Campbell Cloud TimeSeriesCatalog that will be displayed in the table.
	@throws Exception if an invalid results passed in.
	*/
	@SuppressWarnings("unchecked")
	public CampbellCloud_TimeSeries_TableModel ( CampbellCloudDataStore dataStore, List<? extends Object> data ) {
		if ( data == null ) {
			_rows = 0;
		}
		else {
		    _rows = data.size();
		}
	    this.datastore = dataStore;
		_data = data; // Generic
		// TODO SAM 2016-04-17 Need to use instanceof here to check.
		this.timeSeriesCatalogList = (List<TimeSeriesCatalog>)data;
	}

	/**
	From AbstractTableModel.  Returns the class of the data stored in a given column.
	@param columnIndex the column for which to return the data class.
	*/
	@SuppressWarnings({ "unchecked" })
	public Class getColumnClass (int columnIndex) {
		switch (columnIndex) {
			// List in the same order as top of the class.
			case COL_STATION_ID: return Integer.class;
			case COL_STATION_LATITUDE: return Double.class;
			case COL_STATION_LONGITUDE: return Double.class;
			case COL_STATION_ELEVATION: return Double.class;
			case COL_TS_ID: return Integer.class;
			default: return String.class; // All others.
		}
	}

	/**
	From AbstractTableMode.  Returns the number of columns of data.
	@return the number of columns of data.
	*/
	public int getColumnCount() {
		return this.COLUMNS;
	}

	/**
	From AbstractTableMode.  Returns the name of the column at the given position.
	@return the name of the column at the given position.
	*/
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case COL_STATION_ID: return "Station ID";
			case COL_STATION_NAME: return "Station Name";
			case COL_DATA_TYPE: return "Data Type";
			case COL_DATA_SOURCE: return "Data Source";
			case COL_DATA_INTERVAL: return "Interval";
			//case COL_STATISTIC: return "Statistic";
			case COL_DATA_UNITS: return "Units";
			// Datastream data.
			case COL_DATASTREAM_ID: return "Datastream ID";
			case COL_DATASTREAM_ALIAS: return "Datastream Alias";
			case COL_DATASTREAM_FIELD: return "Datastream Field";
			case COL_DATASTREAM_STATUS: return "Datastream Status";
			// Station data.
			case COL_STATION_LONGITUDE: return "Longitude";
			case COL_STATION_LATITUDE: return "Latitude";
			case COL_STATION_ELEVATION: return "Elevation";
			//case COL_STATION_DESCRIPTION: return "Station Description";
			// Asset data.
			case COL_ASSET_ID : return "Asset ID";
			case COL_ASSET_NAME : return "Asset Name";
			case COL_ASSET_DESCRIPTION : return "Asset Description";
			case COL_ASSET_MANUFACTURER : return "Asset Manufacturer";
			case COL_ASSET_MODEL : return "Asset Model";
			case COL_ASSET_STATUS : return "Asset Status";
			case COL_ASSET_MAINT_MODE : return "Maintenance Mode";
			case COL_TS_ID: return "Time Series ID (TSID)";
			case COL_PROBLEMS: return "Problems";
			case COL_DATASTORE: return "Datastore";

			default: return "";
		}
	}

	/**
	Returns an array containing the column widths (in number of characters).
	@return an integer array containing the widths for each field.
	*/
	public String[] getColumnToolTips() {
	    String[] toolTips = new String[this.COLUMNS];
	    toolTips[COL_STATION_ID] = "Station identifier - unique Campbell Cloud station identifier";
	    toolTips[COL_STATION_NAME] = "Station name (station_name)";
	    toolTips[COL_DATA_TYPE] = "Time series data type (datastream field)";
	    toolTips[COL_DATA_SOURCE] = "Data source";
	    toolTips[COL_DATA_INTERVAL] = "Time series data interval (always IrregSecond)";
	    //toolTips[COL_STATISTIC] = "NOT USED - statistic for regular interval data (included in main data type)";
	    toolTips[COL_DATA_UNITS] = "Time series data value units abbreviation";
	    // Datastream data.
		toolTips[COL_DATASTREAM_ID] = "Datastream identifier";
		toolTips[COL_DATASTREAM_ALIAS] = "Datastream alias";
		toolTips[COL_DATASTREAM_FIELD] = "Datastream field";
		toolTips[COL_DATASTREAM_STATUS] = "Datastream status";
		// Station data.
	    toolTips[COL_STATION_LONGITUDE] = "Station longitude, decimal degrees";
	    toolTips[COL_STATION_LATITUDE] = "Station latitude, decimal degrees";
	    toolTips[COL_STATION_ELEVATION] = "Station elevation";
		//toolTips[COL_STATION_DESCRIPTION] = "Station description";
	    toolTips[COL_STATION_ID] = "Station internal identifier, useful for troubleshooting";
	    // Asset data.
		toolTips[COL_ASSET_ID] = "Asset identifier.";
		toolTips[COL_ASSET_NAME] = "Asset name";
		toolTips[COL_ASSET_DESCRIPTION] = "Asset description";
		toolTips[COL_ASSET_MANUFACTURER] = "Asset manufacturer";
		toolTips[COL_ASSET_MODEL] = "Asset model";
		toolTips[COL_ASSET_STATUS] = "Asset status";
		toolTips[COL_ASSET_MAINT_MODE] = "Asset maintenance mode";
		toolTips[COL_TS_ID] = "Time series identifier";
		toolTips[COL_PROBLEMS] = "Problems";
		toolTips[COL_DATASTORE] = "Datastore name";
	    return toolTips;
	}

	/**
	Returns an array containing the column widths (in number of characters).
	@return an integer array containing the widths for each field.
	*/
	public int[] getColumnWidths() {
		int[] widths = new int[this.COLUMNS];
	    widths[COL_STATION_ID] = 25;
	    widths[COL_STATION_NAME] = 20;
	    widths[COL_DATA_SOURCE] = 10;
	    widths[COL_DATA_TYPE] = 20;
	    widths[COL_DATA_INTERVAL] = 8;
	    //widths[COL_STATISTIC] = 8;
	    widths[COL_DATA_UNITS] = 6;
		widths[COL_DATASTREAM_ID] = 27;
		widths[COL_DATASTREAM_ALIAS] = 15;
		widths[COL_DATASTREAM_FIELD] = 15;
		widths[COL_DATASTREAM_STATUS] = 12;
	    widths[COL_STATION_LONGITUDE] = 7;
	    widths[COL_STATION_LATITUDE] = 6;
	    widths[COL_STATION_ELEVATION] = 6;
		//widths[COL_STATION_DESCRIPTION] = 15;
		widths[COL_ASSET_ID] = 20;
		widths[COL_ASSET_NAME] = 20;
		widths[COL_ASSET_DESCRIPTION] = 20;
		widths[COL_ASSET_MANUFACTURER] = 20;
		widths[COL_ASSET_MODEL] = 10;
		widths[COL_ASSET_STATUS] =  10;
		widths[COL_ASSET_MAINT_MODE] =  15;
	    widths[COL_TS_ID] = 50;
		widths[COL_PROBLEMS] = 30;
		widths[COL_DATASTORE] = 20;
		return widths;
	}

	/**
	Returns the format to display the specified column.
	@param column column for which to return the format.
	@return the format (as used by StringUtil.formatString()).
	*/
	public String getFormat ( int column ) {
		switch (column) {
			case COL_STATION_LONGITUDE: return "%.6f";
			case COL_STATION_LATITUDE: return "%.6f";
			case COL_STATION_ELEVATION: return "%.2f";
			default: return "%s"; // All else are strings.
		}
	}

	/**
	From AbstractTableMode.  Returns the number of rows of data in the table.
	*/
	public int getRowCount() {
		return _rows;
	}

	/**
	From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
	@param row the row for which to return data.
	@param col the column for which to return data.
	@return the data that should be placed in the JTable at the given row and column.
	*/
	public Object getValueAt(int row, int col) {
		// Make sure the row numbers are never sorted.
		if (_sortOrder != null) {
			row = _sortOrder[row];
		}

		TimeSeriesCatalog timeSeriesCatalog = this.timeSeriesCatalogList.get(row);
		switch (col) {
			// OK to allow null because will be displayed as blank.
			case COL_STATION_ID: return timeSeriesCatalog.getStationId();
			case COL_STATION_NAME: return timeSeriesCatalog.getStationName();
			case COL_DATA_SOURCE: return timeSeriesCatalog.getDataSource();
			case COL_DATA_TYPE: return timeSeriesCatalog.getDataType();
			case COL_DATA_INTERVAL: return timeSeriesCatalog.getDataInterval();
			//case COL_STATISTIC: return timeSeriesCatalog.getStatistic();
			// Data units come from the general value, which will be from point_type or rating.
			case COL_DATA_UNITS: return timeSeriesCatalog.getDataUnits();
			case COL_DATASTREAM_ID: return timeSeriesCatalog.getDatastreamId();
			case COL_DATASTREAM_ALIAS: return timeSeriesCatalog.getDatastreamAlias();
			case COL_DATASTREAM_FIELD: return timeSeriesCatalog.getDatastreamField();
			case COL_DATASTREAM_STATUS: return timeSeriesCatalog.getDatastreamStatus();
			case COL_STATION_LONGITUDE: return timeSeriesCatalog.getStationLongitude();
			case COL_STATION_LATITUDE: return timeSeriesCatalog.getStationLatitude();
			case COL_STATION_ELEVATION: return timeSeriesCatalog.getStationElevation();
			//case COL_STATION_DESCRIPTION: return timeSeriesCatalog.getStationDescription();
			case COL_ASSET_ID : return timeSeriesCatalog.getAssetId();
			case COL_ASSET_NAME : return timeSeriesCatalog.getAssetName();
			case COL_ASSET_DESCRIPTION : return timeSeriesCatalog.getAssetDescription();
			case COL_ASSET_MANUFACTURER : return timeSeriesCatalog.getAssetManufacturer();
			case COL_ASSET_MODEL : return timeSeriesCatalog.getAssetModel();
			case COL_ASSET_STATUS : return timeSeriesCatalog.getAssetStatus();
			case COL_ASSET_MAINT_MODE : return timeSeriesCatalog.getAssetMaintMode();
			case COL_TS_ID: return timeSeriesCatalog.getTsId();
			case COL_PROBLEMS: return timeSeriesCatalog.formatProblems();			
			case COL_DATASTORE: return this.datastore.getName();			
			default: return "";
		}
	}

}