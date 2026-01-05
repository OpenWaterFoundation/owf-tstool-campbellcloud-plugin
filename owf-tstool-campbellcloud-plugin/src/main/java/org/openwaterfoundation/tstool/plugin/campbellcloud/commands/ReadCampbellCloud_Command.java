// ReadCampbellCloud_Command - This class initializes, checks, and runs the ReadCampbellCloud() command.

/* NoticeStart

OWF TSTool Campbell CLoud Plugin
Copyright (C) 2025 Open Water Foundation

OWF TSTool Campbell CLoud Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

OWF TSTool Campbell CLoud Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with OWF TSTool Campbell CLoud Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.campbellcloud.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import org.openwaterfoundation.tstool.plugin.campbellcloud.datastore.CampbellCloudDataStore;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.campbellcloud.ui.CampbellCloud_TimeSeries_InputFilter_JPanel;

import riverside.datastore.DataStore;
import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSProgressListener;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.MissingObjectEvent;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
This class initializes, checks, and runs the ReadCampbellCloud() command.
*/
public class ReadCampbellCloud_Command extends AbstractCommand
implements Command, CommandDiscoverable, ObjectListProvider, TSProgressListener
{

/**
Number of where clauses shown in the editor and available as parameters.
*/
private int __numWhere = 6;

/**
Data values for boolean parameters.
*/
protected String _False = "False";
protected String _True = "True";

/**
List of time series read during discovery.
These are TS objects but with mainly the metadata (TSIdent) filled in.
*/
private List<TS> __discoveryTSList = null;

/**
The discovery table that is created.
*/
private DataTable __discoveryTable = null;

/**
Constructor.
*/
public ReadCampbellCloud_Command () {
	super();
	setCommandName ( "ReadCampbellCloud" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException {
	String routine = getClass().getSimpleName() + "checkCommandParameters";
	String warning = "";
    String message;

    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    // General (top).
    String DataStore = parameters.getValue ( "DataStore" );
    String DataType = parameters.getValue ( "DataType" );
    String Interval = parameters.getValue ( "Interval" );
    // Single.
    String StationId = parameters.getValue ( "StationId" );
    String TSID = parameters.getValue ( "TSID" );
    boolean doTSID = false;
    if ( (TSID != null) && !TSID.isEmpty() ) {
    	doTSID = true;
    }
    // Multiple.
    String InputFiltersCheck = parameters.getValue ( "InputFiltersCheck" ); // Passed in from the editor, not an actual parameter.
    String Where1 = parameters.getValue ( "Where1" );
    String Where2 = parameters.getValue ( "Where2" );
    String Where3 = parameters.getValue ( "Where3" );
    String Where4 = parameters.getValue ( "Where4" );
    String Where5 = parameters.getValue ( "Where5" );
    String ReadTimeSeries = parameters.getValue ( "ReadTimeSeries" );
    // General (bottom).
    String InputStart = parameters.getValue ( "InputStart" );
    String InputEnd = parameters.getValue ( "InputEnd" );
    String ReadData = parameters.getValue ( "ReadData" );
    String TimePrecision = parameters.getValue ( "TimePrecision" );
    String Timeout = parameters.getValue ( "Timeout" );
    String Debug = parameters.getValue ( "Debug" );

	if ( (DataStore == null) || DataStore.isEmpty() ) {
        message = "The datastore must be specified.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the datastore." ) );
	}

	if ( ((DataType == null) || DataType.isEmpty()) && !doTSID) {
        message = "The data type must be specified.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the data type." ) );
	}

	if ( ((Interval == null) || Interval.isEmpty()) && !doTSID) {
        message = "The interval must be specified.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the interval." ) );
	}

	// TODO SAM 2023-01-02 Need to check the WhereN parameters.

	if ( (InputStart != null) && !InputStart.equals("") &&
		!InputStart.equalsIgnoreCase("InputStart") &&
		!InputStart.equalsIgnoreCase("InputEnd") && (InputStart.indexOf("${") < 0)) { // }
		try {
			DateTime.parse(InputStart);
		}
		catch ( Exception e ) {
            message = "The input start date/time \"" + InputStart + "\" is not a valid date/time.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a date/time or InputStart." ) );
		}
	}

	if ( (InputEnd != null) && !InputEnd.equals("") &&
		!InputEnd.equalsIgnoreCase("InputStart") &&
		!InputEnd.equalsIgnoreCase("InputEnd") && (InputEnd.indexOf("${") < 0)) { // }
		try {
			DateTime.parse( InputEnd );
		}
		catch ( Exception e ) {
            message = "The input end date/time \"" + InputEnd + "\" is not a valid date/time.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Specify a date/time or InputEnd." ) );
		}
	}

	if ( (ReadData != null) && !ReadData.isEmpty() ) {
		if ( !ReadData.equalsIgnoreCase(_True) && !ReadData.equalsIgnoreCase(_False) ) {
            message = "Invalid ReadData parameter \"" + ReadData + "\"";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify " + _False + " or " + _True + " (default)." ) );
		}
	}

	if ( (TimePrecision != null) && !TimePrecision.equals("") ) {
		//++paramCount;
		if ( TimePrecision.indexOf("${") < 0 ) { // } so editor matches.
			try {
				TimeInterval.parseInterval(TimePrecision);
			}
			catch ( Exception e ) {
				message = "Invalid time precision (" + TimePrecision + ").";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Select a time precision the choices.") );
			}
		}
	}

	if ( (Timeout != null) && !Timeout.isEmpty() && !StringUtil.isInteger(Timeout) ) {
        message = "The Timeout parameter value is invalid.";
		warning += "\n" + message;
           status.addToLog ( CommandPhaseType.INITIALIZATION,
               new CommandLogRecord(CommandStatusType.FAILURE,
                   message, "Specify as an integer." ) );
	}

	if ( (Debug != null) && !Debug.equals("") &&
		!Debug.equalsIgnoreCase(_False) && !Debug.equalsIgnoreCase(_True) ) {
        message = "The Debug parameter value is invalid.";
		warning += "\n" + message;
           status.addToLog ( CommandPhaseType.INITIALIZATION,
               new CommandLogRecord(CommandStatusType.FAILURE,
                   message, "Specify " + _False + " (default) or " + _True ) );
	}

	// Make sure that some parameters are specified so that a query of all data is disallowed.

	int whereCount = 0;
	if ( (Where1 != null) && !Where1.startsWith(";") ) {
		++whereCount;
	}
	if ( (Where2 != null) && !Where2.startsWith(";") ) {
		++whereCount;
	}
	if ( (Where3 != null) && !Where3.startsWith(";") ) {
		++whereCount;
	}
	if ( (Where4 != null) && !Where4.startsWith(";") ) {
		++whereCount;
	}
	if ( (Where5 != null) && !Where5.startsWith(";") ) {
		++whereCount;
	}

	if ( (whereCount == 0) && (DataType != null) && DataType.equals("*") && (ReadTimeSeries != null) && ReadTimeSeries.equalsIgnoreCase(this._True)) {
        message = "DataType=*, no Where filters are specified, and ReadTimeSeries=True - not allowed because may query many time series.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
	            message, "Specify a data type, one or more Where filters, and/or ReadTimeSeries=False." ) );
	}

	boolean readTimeSeries = true;
	if ( (ReadTimeSeries != null) && !ReadTimeSeries.isEmpty() ) {
		if ( !ReadTimeSeries.equalsIgnoreCase(_True) && !ReadTimeSeries.equalsIgnoreCase(_False) ) {
            message = "Invalid ReadTimeSeries parameter \"" + ReadTimeSeries + "\"";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify " + _False + " or " + _True + " (default)." ) );
		}
		if ( ReadTimeSeries.equalsIgnoreCase(_False) ) {
			readTimeSeries = false;
		}
	}

	boolean readSingle = false;
	boolean readMult = false;
	if ( (StationId != null) && !StationId.isEmpty() ) {
		// Querying one time series.
		readSingle = true;

		// The data type cannot be a wild card.
		if ( DataType.equals("*") ) {
            message = "The data type cannot be * when matching a single time series.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a specific data type." ) );
		}
	}
	if ( whereCount > 0 ) {
		// Querying multiple time series.
		readMult = true;
	}
	if ( Message.isDebugOn ) {
		Message.printStatus(2, routine, "StationId=" + StationId + " whereCount=" + whereCount + " readSingle=" + readSingle + " readMult=" + readMult);
		Message.printStatus(2, routine, "Where1=" + Where1 + " Where2=" + Where2 + " Where3=" + Where3 + " Where4=" + Where4 + " Where5=" + Where5);
	}

	if ( readSingle && readMult ) {
		// Can only read one or multiple.
        message = "Parameters are specified to match a single time series and multiple time series (but not both).";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify parameters to match a single time series OR multiple time series." ) );
	}
	if ( !readSingle && !readMult ) {
		// OK if the DataType is not *.
		// Also OK if ReadTimeSeries=False.
		if ( DataType.equals("*") && readTimeSeries ) {
			// Not enough parameters are specified.
        	message = "Parameters must be specified to match a single time series OR multiple time series (reading ALL time series data is prohibited).";
			warning += "\n" + message;
        	status.addToLog ( CommandPhaseType.INITIALIZATION,
            	new CommandLogRecord(CommandStatusType.FAILURE,
                	message, "Specify parameters to match a single time series OR multiple time series.  At a minimum, specify the data type." ) );
		}
	}
	
	// Make sure the interval is specified if reading one time series.
	if ( readSingle && ((Interval == null) || Interval.isEmpty() || Interval.equals("*"))) {
        message = "The interval must be specified when reading a single time series (wildcard cannot be used).";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify an interval to use for the single time series." ) );
	}

    // If any issues were detected in the input filter add to the message string.
    if ( (InputFiltersCheck != null) && !InputFiltersCheck.isEmpty() ) {
    	warning += InputFiltersCheck;
    }

    // Check for invalid parameters.
    List<String> validList = new ArrayList<>();
    // General (top).
    validList.add ( "DataStore" );
    validList.add ( "DataType" );
    validList.add ( "Interval" );
    // Single.
    validList.add ( "StationId" );
    validList.add ( "TSID" );
    // Multiple.
    int numFilters = 25; // Make a big number so all are allowed.
    for ( int i = 1; i <= numFilters; i++ ) {
        validList.add ( "Where" + i );
    }
    validList.add ( "ReadTimeSeries" );
    validList.add ( "TimeSeriesCatalogTableID" );
    // Bottom (general).
    validList.add ( "Alias" );
    validList.add ( "InputStart" );
    validList.add ( "InputEnd" );
    validList.add ( "ReadData" );
    validList.add ( "TimePrecision" );
    validList.add ( "Timezone" );
    validList.add ( "Units" );
    validList.add ( "Timeout" );
    validList.add ( "Debug" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
		throw new InvalidCommandParameterException ( warning );
	}

    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
 * Create properties for reading time series.
 * @param debug whether to run web service queries in debug
 * @param timeout timeout in seconds
 * @param timePrecision time precision to use for output time series
 * @param timezone time zone to be used for response, important for interval calculations
 * @param tsProgressListener object that will listen for progress reading a time series
 * @param units data units to set in the time series
 */
private HashMap<String,Object> createReadProperties (
	boolean debug,
	int timeout,
	String timePrecision,
	String timezone,
	TSProgressListener tsProgressListener,
	String units
	) {
	HashMap<String,Object> readProperties = new HashMap<>();
	if ( (timezone != null) && !timezone.isEmpty() ) {
		readProperties.put("TimeZone", timezone );
	}
	if ( (units != null) && !units.isEmpty() ) {
		readProperties.put("Units", units );
	}
	if ( debug ) {
		readProperties.put("Debug", Boolean.TRUE );
	}
	if ( (timePrecision != null) && !timePrecision.isEmpty() ) {
		readProperties.put("TimePrecision", timePrecision );
	}
	if ( timeout > 0 ) {
		readProperties.put("Timeout", Integer.valueOf(timeout) );
	}
	if ( (timezone != null) && !timezone.isEmpty() ) {
		readProperties.put("Timezone", timezone );
	}
	if ( tsProgressListener != null ) {
		readProperties.put("ProgressListener", tsProgressListener );
	}
	return readProperties;
}

/**
 * Create a data table from the time series catalog.
 * @param dataStore the datastore used to read the data
 * @param tsCatalogList list of TimeSeriesCatalog to convert into a table
 * @param timeSeriesCatalogTableID identifier for the table to create
 * @return a table for the catalog
 */
private DataTable createTimeSeriesCatalogTable ( CampbellCloudDataStore dataStore,
	List<TimeSeriesCatalog> tsCatalogList, String timeSeriesCatalogTableID )
throws Exception {
	String routine = getClass().getSimpleName() + ".createTimeSeriesCatalogTable";
	// Create the table columns (fields):
	// - if any column cannot be created, let the command fail
	// - use camelCase notation and no spaces to facilitate using property names that match
	List<TableField> columnList = new ArrayList<>();
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "StationId", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "StationName", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "DataSource", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "DataType", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Interval", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Units", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "DatastreamId", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "DatastreamAlias", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "DatastreamField", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "DatastreamStatus", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_DOUBLE, "Longitude", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_DOUBLE, "Latitude", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_DOUBLE, "Elevation", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "AssetId", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "AssetName", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "AssetDescription", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "AssetManufacturer", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "AssetModel", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "AssetStatus", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_BOOLEAN, "MaintenanceMode", -1) );
	columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "TSID", -1) );
	// Create the table from the column metadata.
    DataTable table = new DataTable( columnList );
	// Set the table identifier to the command parameter.
	table.setTableID(timeSeriesCatalogTableID);
    // Get the column numbers from the table:
    // - do just in case there are any hidden columns
	// - use the order of the table columns
	int stationIdCol = table.getFieldIndex( "StationId" );
	int stationNameCol = table.getFieldIndex( "StationName" );
	int dataSourceCol = table.getFieldIndex( "DataSource" );
	int dataTypeCol = table.getFieldIndex( "DataType" );
	int intervalCol = table.getFieldIndex( "Interval" );
	int unitsCol = table.getFieldIndex( "Units" );
	int datastreamIdCol = table.getFieldIndex( "DatastreamId" );
	int datastreamAliasCol = table.getFieldIndex( "DatastreamAlias" );
	int datastreamFieldCol = table.getFieldIndex( "DatastreamField" );
	int datastreamStatusCol = table.getFieldIndex( "DatastreamStatus" );
	int longitudeCol = table.getFieldIndex( "Longitude" );
	int latitudeCol = table.getFieldIndex( "Latitude" );
	int elevationCol = table.getFieldIndex( "Elevation" );
	int assetIdCol = table.getFieldIndex( "AssetId" );
	int assetNameCol = table.getFieldIndex( "AssetName" );
	int assetDescriptionCol = table.getFieldIndex( "AssetDescription" );
	int assetManufacturerCol = table.getFieldIndex( "AssetManufacturer" );
	int assetModelCol = table.getFieldIndex( "AssetModel" );
	int assetStatusCol = table.getFieldIndex( "AssetStatus" );
	int maintenanceModeCol = table.getFieldIndex( "MaintenanceMode" );
	int tsidCol = table.getFieldIndex( "TSID" );

	// Convert the catalog to table rows.

	int icatalog = 0;
	Message.printWarning ( 3, routine, "Creating table from " + tsCatalogList.size() + " time series catalog objects." );
	int maxErrors = 50;
	int errorCount = 0;
	for ( TimeSeriesCatalog tscatalog : tsCatalogList ) {
		++icatalog;
		try {
			TableRecord rec = table.emptyRecord();
			rec.setFieldValue(stationIdCol, tscatalog.getStationId());
			rec.setFieldValue(stationNameCol, tscatalog.getStationName());
			rec.setFieldValue(dataSourceCol, tscatalog.getDataSource());
			rec.setFieldValue(dataTypeCol, tscatalog.getDataType());
			rec.setFieldValue(intervalCol, tscatalog.getDataInterval());
			rec.setFieldValue(unitsCol, tscatalog.getDataUnits());
			rec.setFieldValue(datastreamIdCol, tscatalog.getDatastreamId());
			rec.setFieldValue(datastreamAliasCol, tscatalog.getDatastreamAlias());
			rec.setFieldValue(datastreamFieldCol, tscatalog.getDatastreamField());
			rec.setFieldValue(datastreamStatusCol, tscatalog.getDatastreamStatus());
			rec.setFieldValue(longitudeCol, tscatalog.getStationLongitude());
			rec.setFieldValue(latitudeCol, tscatalog.getStationLatitude());
			rec.setFieldValue(elevationCol, tscatalog.getStationElevation());
			rec.setFieldValue(assetIdCol, tscatalog.getAssetId());
			rec.setFieldValue(assetNameCol, tscatalog.getAssetName());
			rec.setFieldValue(assetDescriptionCol, tscatalog.getAssetDescription());
			rec.setFieldValue(assetManufacturerCol, tscatalog.getAssetManufacturer());
			rec.setFieldValue(assetModelCol, tscatalog.getAssetModel());
			rec.setFieldValue(assetStatusCol, tscatalog.getAssetStatus());
			rec.setFieldValue(maintenanceModeCol, tscatalog.getAssetMaintMode());
			rec.setFieldValue(tsidCol, tscatalog.getTSID());
			table.addRecord(rec);
		}
		catch ( Exception e ) {
			// Error adding the table record.
			++errorCount;
			if ( errorCount <= maxErrors ) {
				Message.printWarning ( 3, routine, "Error converting catalog " + icatalog + " to table row." );
				Message.printWarning ( 3, routine, e );
			}
			if ( errorCount == maxErrors ) {
				Message.printWarning ( 3, routine, "Limiting error output to " + maxErrors + " errors." );
			}
		}
	}
	return table;
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent ) {
	// The command will be modified if changed.
	return (new ReadCampbellCloud_JDialog ( parent, this )).ok();
}

/**
Return the table that is read by this class when run in discovery mode.
@return the table that is read by this class when run in discovery mode
*/
private DataTable getDiscoveryTable() {
    return this.__discoveryTable;
}

/**
Return the list of time series read in discovery phase.
@return the list of time series read in discovery phase
*/
private List<TS> getDiscoveryTSList () {
    return __discoveryTSList;
}

/**
Return the list of data objects created by this object in discovery mode.
The following classes can be requested:  TS, DataTable
@return a list of discovery mode objects create by this command
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c ) {
	// Check for a table request first.
    if ( c == DataTable.class ) {
    	DataTable discoveryTable = getDiscoveryTable();
    	if ( discoveryTable == null ) {
    		return null;
    	}
    	else {
    		List<DataTable> tableList = new ArrayList<>();
    		tableList.add(discoveryTable);
    		return (List<T>)tableList;
    	}
    }
    else {
    	// Check for a time series request.
    	List<TS> discoveryTsList = getDiscoveryTSList ();
       	if ( (discoveryTsList == null) || (discoveryTsList.size() == 0) ) {
           	return null;
       	}
       	// Since all time series must be the same interval, check the class for the first one (e.g., MonthTS).
       	TS datats = discoveryTsList.get(0);
       	// Also check the base class.
       	if ( (c == TS.class) || (c == datats.getClass()) ) {
           	return (List<T>)discoveryTsList;
       	}
       	else {
           	return null;
       	}
    }
}

// parseCommand is in parent class.

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
	String routine = getClass().getSimpleName() + ".runCommandInternal", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;

	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();
	TSCommandProcessor tsprocessor = (TSCommandProcessor)processor;
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);

    Boolean clearStatus = Boolean.TRUE; // Default.
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen.
    }
    if ( clearStatus ) {
		status.clearLog(commandPhase);
	}

    // General (top).
    
    boolean readData = true;
	List<Prop> propList = null;
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
		// Get all discovery properties, used to handle ${Property} expansion in discovery mode:
    	// - TODO smalers 2025-11-23 currently don't allow the datastore to use a property
    	// - see ReadNovaStar if need to do this
        setDiscoveryTSList ( null );
		propList = TSCommandProcessorUtil.getDiscoveryPropFromCommandsBeforeCommand(
            (TSCommandProcessor)getCommandProcessor(), this);
        readData = false;
    }

	String DataType = parameters.getValue("DataType");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    DataType = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, DataType);
	}
	String Interval = parameters.getValue("Interval");
	
	if ( commandPhase == CommandPhaseType.RUN ) {
	    Interval = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, Interval);
	}
	
	// Single.

	String StationId = parameters.getValue("StationId");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    StationId = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, StationId);
	}
	boolean doStationId = false;
    if ( (StationId != null) && !StationId.isEmpty() ) {
	    doStationId = true;
    }
	String TSID = parameters.getValue("TSID");
	boolean doTSID = false;
	if ( commandPhase == CommandPhaseType.RUN ) {
	    TSID = TSCommandProcessorUtil.expandParameterValue(processor, this, TSID);
	}
    if ( (TSID != null) && !TSID.isEmpty() ) {
	    doTSID = true;
    }

	// Multiple.

	String ReadTimeSeries = parameters.getValue ("ReadTimeSeries" );
	boolean readTimeSeries = true;
	if ( commandPhase == CommandPhaseType.RUN ) {
	    ReadTimeSeries = TSCommandProcessorUtil.expandParameterValue(processor, this, ReadTimeSeries);
	}
    if ( (ReadTimeSeries != null) && ReadTimeSeries.equalsIgnoreCase(this._False)) {
    	readTimeSeries = false;
    }

	String TimeSeriesCatalogTableID = parameters.getValue ("TimeSeriesCatalogTableID" );
	if ( commandPhase == CommandPhaseType.RUN ) {
	    TimeSeriesCatalogTableID = TSCommandProcessorUtil.expandParameterValue(processor, this, TimeSeriesCatalogTableID);
	}
	
	// General (bottom).

	String InputStart = parameters.getValue("InputStart");
	if ( (InputStart == null) || InputStart.isEmpty() ) {
		InputStart = "${InputStart}";
	}
    String InputEnd = parameters.getValue("InputEnd");
	if ( (InputEnd == null) || InputEnd.isEmpty() ) {
		InputEnd = "${InputEnd}";
	}
	String ReadData = parameters.getValue ("ReadData" );
	if ( commandPhase == CommandPhaseType.RUN ) {
	    ReadData = TSCommandProcessorUtil.expandParameterValue(processor, this, ReadData);
	}
	if ( (ReadData != null) && ReadData.equalsIgnoreCase(_False) ) {
		// OK to use the same variable as discovery boolean.
		readData = false;
	}
    String TimePrecision = parameters.getValue("TimePrecision");
	TimePrecision = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, TimePrecision);

	String Timezone = parameters.getValue ("Timezone" );
	if ( (Timezone == null) || Timezone.isEmpty() ) {
		Timezone = "UTC";
	}
	String Units = parameters.getValue ("Units" );
	String Timeout = parameters.getValue ("Timeout" );
	int timeoutSeconds = 5*60; // Default = 5 minutes.
	if ( commandPhase == CommandPhaseType.RUN ) {
	    Timeout = TSCommandProcessorUtil.expandParameterValue(processor, this, Timeout);
	}
	if ( (Timeout != null) && !Timeout.isEmpty() ) {
		timeoutSeconds = Integer.valueOf(timeoutSeconds);
	}
	String Debug = parameters.getValue ("Debug" );
	boolean debug = false; // Default
	if ( (Debug != null) && Debug.equalsIgnoreCase(_True) ) {
		debug = true;
	}

    DateTime InputStart_DateTime = null;
    DateTime InputEnd_DateTime = null;
	if ( commandPhase == CommandPhaseType.RUN ) {
		String tzStart = null;
		String tzEnd = null;
		try {
			InputStart_DateTime = TSCommandProcessorUtil.getDateTime ( InputStart, "InputStart", processor,
				status, warning_level, command_tag );
			if ( InputStart_DateTime != null ) {
				tzStart = InputStart_DateTime.getTimeZoneAbbreviation();
				if ( (tzStart == null) || tzStart.isEmpty() ) {
					// Set the time zone to the Timezone parameter.
					InputStart_DateTime.setTimeZone(Timezone);
				}
			}
		}
		catch ( InvalidCommandParameterException e ) {
			// Warning will have been added above.
			++warning_count;
		}
		try {
			InputEnd_DateTime = TSCommandProcessorUtil.getDateTime ( InputEnd, "InputEnd", processor,
				status, warning_level, command_tag );
			if ( InputEnd_DateTime != null ) {
				tzEnd = InputEnd_DateTime.getTimeZoneAbbreviation();
				if ( (tzEnd == null) || tzEnd.isEmpty() ) {
					// Set the time zone to the Timezone parameter.
					InputEnd_DateTime.setTimeZone(Timezone);
				}
			}
		}
		catch ( InvalidCommandParameterException e ) {
			// Warning will have been added above.
			++warning_count;
		}
		
		// Make sure that the time zones are the same, can be empty.
		if ( ((tzStart == null) && (tzEnd == null)) || (tzStart.equals(tzEnd)) ) {
			// OK case.
		}
		else {
            message = "InputStart timezone (" + tzStart + ") and InputEnd timezone (" + tzEnd + ") are different.";
            Message.printWarning ( 2, routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that the time zones ar the same or override with the Timezone parameter.") );
		}
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	// Now try to read.

	// List for time series results:
	// - will be added to for one time series read or replaced if a list is read
	List<TS> tslist = new ArrayList<>();

	try {
        String Alias = parameters.getValue ( "Alias" );
        String DataStore = parameters.getValue ( "DataStore" );
        CampbellCloudDataStore dataStore = null;
		if ( (DataStore != null) && !DataStore.equals("") ) {
		    // User has indicated that a datastore should be used.
		    DataStore dataStore0 = ((TSCommandProcessor)getCommandProcessor()).getDataStoreForName( DataStore, CampbellCloudDataStore.class );
	        if ( dataStore0 != null ) {
	            Message.printStatus(2, routine, "Selected datastore is \"" + dataStore0.getName() + "\"." );
				dataStore = (CampbellCloudDataStore)dataStore0;
	        }
	    }
		if ( dataStore == null ) {
			if ( commandPhase == CommandPhaseType.DISCOVERY ) {
				message = "Cannot get CampbellCloudDataStore for \"" + DataStore + "\" in discovery mode.";
			}
			else {
				message = "Cannot get CampbellCloudDataStore for \"" + DataStore + "\".";
			}
            Message.printWarning ( 2, routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that a CampbellCloudDataStore datastore is properly configured." ) );
            throw new RuntimeException ( message );
        }
        else {
			// Have a datastore so try to read.
        	// See if a Where has been specified by checking for the first Where clause.
			String WhereN = parameters.getValue ( "Where" + 1 );
			if ( doTSID || doStationId ) {
				// Have single location ID so try to read the matching time series.
				// Try to read the matching single time series:
				// - for StationId, 'TSIDForRequest' is formed from parts
				// - for TSID, 'TSIDForRequest' is the 'TSID'
				String TSIDForRequest = null;
				if ( doStationId ) {
					TSIdent tsident = new TSIdent();
					if ( StationId != null ) {
						tsident.setLocation(StationId);
					}
					tsident.setSource("CampbellCloud");
					if ( (DataType != null) && !DataType.isEmpty() && !DataType.equals("*") ) {
						if ( (DataType.indexOf("-") > 0) || (DataType.indexOf(".") > 0) ) {
							// Escape the data type.
							tsident.setType("'" + DataType + "'");
						}
						else {
							tsident.setType(DataType);
						}
					}
					if ( (Interval != null) && !Interval.isEmpty() && !Interval.equals("*") ) {
						tsident.setInterval(Interval);
					}
					TSIDForRequest = tsident.getIdentifier();
				}
				else if ( doTSID ) {
					TSIDForRequest = TSID;
				}
				// Version that reads a single time series using the TSID.
		    	TS ts = null;
				try {
					if ( (commandPhase == CommandPhaseType.DISCOVERY) && TSIDForRequest.contains("${") ) { // }
						// Discovery mode has property in the TSID:
						// - don't try to read the time series
						Message.printStatus ( 2, routine, "Setting NovaStar web service discovery time series \"" + TSIDForRequest + "\"" );
						ts = new TS();
						ts.setIdentifier(TSIDForRequest);
						Message.printStatus ( 2, routine, "Back from setting NovaStar web service discovery time series \"" + TSIDForRequest + "\"" );
					}
					else {
						// Create the list of properties that control the read:
						// - use the TSProgressListener so that progress reading shows up as the command progress
						TSProgressListener tsProgressListener = this;
						HashMap<String,Object> readProperties = createReadProperties (
							debug,
							timeoutSeconds,
							TimePrecision,
							Timezone,
							// Want to chain progress in the single time series to TSTool.
							tsProgressListener,
							Units
							);
						// Try to read the time series:
						// - if in discover mode, 'readData' will be false
						Message.printStatus ( 2, routine, "Reading Campbell Cloud web service time series \"" + TSIDForRequest + "\"" );
						// Set the notification at 25% to show some progress.
						notifyCommandProgressListeners ( 0, 100, (float)(25.0), "Requesting data." );
               			ts = dataStore.readTimeSeries ( TSIDForRequest, InputStart_DateTime, InputEnd_DateTime, readData, readProperties );
						// Set the notification at 100% to show completion.
						notifyCommandProgressListeners ( 0, 100, (float)(100.0), "Completed requesting data." );
					}
				}
				catch ( Exception e ) {
				    ts = null;
					message = "Unexpected error reading Campbell Cloud web services time series \"" + TSIDForRequest + "\" (" + e + ").";
					Message.printWarning ( 2, routine, message );
					Message.printWarning ( 2, routine, e );
					if ( commandPhase == CommandPhaseType.DISCOVERY ) {
						status.addToLog ( commandPhase,
							new CommandLogRecord(CommandStatusType.WARNING,
								message, "Verify the time series identifier - may need to fix the database configuration." ) );
					}
					else {
						status.addToLog ( commandPhase,
							new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify the time series identifier - may need to fix the database configuration." ) );
						// Throw an exception because could not read the single time series.
						//throw new RuntimeException ( message );
					}
				}
				finally {
			    	if ( ts == null ) {
			       		// Generate an event for listeners.
			       		notifyCommandProcessorEventListeners(
					        new MissingObjectEvent(TSIDForRequest,Class.forName("RTi.TS.TS"),"Time Series", this));
			    	}
				}
				if ( ts != null ) {
					// Set the alias:
					// - OK to use properties
				    if ( Alias != null ) {
				    	//if ( (commandPhase == CommandPhaseType.DISCOVERY) && (Alias.contains("${") || Alias.contains("%")) ) { // } }
				    	if ( (commandPhase == CommandPhaseType.DISCOVERY) ) {
				    		// Set the alias to the command parameter:
				    		// - the time series will have been created above
				    		// - do not expand the alias
				    		ts.setAlias(Alias);
				    	}
				    	else {
				    		// Set the alias based on run-time data.
				    		ts.setAlias ( TSCommandProcessorUtil.expandTimeSeriesMetadataString( processor, ts, Alias, status, commandPhase) );
				    	}
				    }
					tslist.add ( ts );
				}
	        }
			else {
	            // Read 1+ time series using the input filters.
				// Get the input needed to process the file.
				Message.printStatus(2, routine, "Reading multiple Campbell Cloud time series using input filter.");

				if ( (commandPhase == CommandPhaseType.DISCOVERY) &&
					(((DataType != null) && DataType.contains("${")) || // }
					((Interval != null) && Interval.contains("${"))) ) { // }
					// Can't read the discovery time series because of properties.
				}
				else {
					// No property so can attempt to read the time series, even in discovery mode.
					String InputName = parameters.getValue ( "InputName" );
					if ( InputName == null ) {
						InputName = "";
					}
					List<String> whereNList = new ArrayList<>();
					int nfg = 0; // Used below.
					// User may have skipped a where and left a blank so loop over a sufficiently large number of where parameters
					// to get the non-blank filters.
					for ( int ifg = 0; ifg < 25; ifg++ ) {
						WhereN = parameters.getValue ( "Where" + (ifg + 1) );
						if ( WhereN != null ) {
							++nfg;
							whereNList.add ( WhereN );
						}
					}

					boolean haveError = false;
					// Do a runtime check to prohibit DataType=* without input filters because it will hammer the system:
					// - OK if ReadTimeSeries=False
					/* TODO smalers 2025-11-23 evaluate whether to enable.
					if ( (nfg == 0) && DataType.equals("*") && readTimeSeries ) {
	                	message = "DataType=*, no Where filters, and ReadTimeSeries=True are specified - not allowed because may query many time series.";
						Message.printWarning ( 2, routine,message);
						++warning_count;
	                	status.addToLog ( commandPhase,
	                    	new CommandLogRecord(CommandStatusType.FAILURE,
	                        	message, "Specify a data type, one or more Where filters, and/or ReadTimeSeries=False." ) );
	                	haveError = true;
					}
					*/

					if ( !haveError ) {
						// Initialize an input filter based on the data type.

						InputFilter_JPanel filterPanel = null;

						// Create the input filter panel.
						String dataTypeReq = "";
			    		if ( dataTypeReq.indexOf("-") > 0 ) {
			        		dataTypeReq = StringUtil.getToken(DataType,"-",0,1).trim();
			    		}
			    		else {
			        	dataTypeReq = DataType.trim();
			    		}

						filterPanel = new CampbellCloud_TimeSeries_InputFilter_JPanel ( dataStore, 5 );

						// Populate with the where information from the command:
						// - the first part of the where should match the "whereLabelPersistent" used when constructing the input filter
						// - the Campbell Cloud internal field is used to help users correlate the TSTool filter to Campbell Cloud web services

						String filter_delim = ";";
						for ( int ifg = 0; ifg < nfg; ifg ++ ) {
							WhereN = whereNList.get(ifg);
	                		if ( WhereN.length() == 0 ) {
	                    		continue;
	                		}
							// Set the filter.
							try {
	                    		filterPanel.setInputFilter( ifg, WhereN, filter_delim );
							}
							catch ( Exception e ) {
	                    		message = "Error setting where information using \"" + WhereN + "\"";
								Message.printWarning ( 2, routine,message);
								Message.printWarning ( 3, routine, e );
								++warning_count;
	                    		status.addToLog ( commandPhase,
	                        		new CommandLogRecord(CommandStatusType.FAILURE,
	                            		message, "Report the problem to software support - also see the log file." ) );
							}
						}
	
						// Read the list of objects from which identifiers can be obtained.

						Message.printStatus ( 2, routine, "Getting the list of time series..." );

						// Create empty lists for catalogs from each major data category.
						List<TimeSeriesCatalog> tsCatalogList = new ArrayList<>();

						// Read the catalog.
						int size = 0;
						try {
							tsCatalogList = dataStore.readTimeSeriesCatalog ( dataTypeReq, Interval, filterPanel );
							size = tsCatalogList.size();
						}
						catch ( Exception e ) {
							// Probably no data.
						}
	
						// Make sure that size is set.
	       				if ( size == 0 ) {
							Message.printStatus ( 2, routine,"No Campbell Cloud web service time series were found." );
			        		// Warn if nothing was retrieved (can be overridden to ignore).
							if ( commandPhase == CommandPhaseType.DISCOVERY ) {
								if ( readTimeSeries ) {
									message = "No time series were read from NovaStar web service - may be OK if ${Property} are expanded at run time.";
									Message.printWarning ( warning_level,
										MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
									status.addToLog ( commandPhase,
										new CommandLogRecord(CommandStatusType.WARNING,
										message, "Data may not be in the database - may be OK if interval time series are created when the command is run." +
										"  Previous messages may provide more information." ) );
								}
							}
							else {
								if ( readTimeSeries ) {
									message = "No time series were read from NovaStar web service.";
									Message.printWarning ( warning_level,
									MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
									status.addToLog ( commandPhase,
										new CommandLogRecord(CommandStatusType.FAILURE,
											message, "Data may not be in database.  Previous messages may provide more information." ) );
								}
							}
		            		// Generate an event for listeners.
		            		// FIXME SAM 2008-08-20 Need to put together a more readable id for reporting.
	                		notifyCommandProcessorEventListeners(
	                    		new MissingObjectEvent(DataType + ", " + Interval + ", see command for user-specified criteria",
	                        		Class.forName("RTi.TS.TS"),"Time Series", this));
							return;
	       				}
						else {
							Message.printStatus ( 2, "", "Read " + size + " time series catalog for data type '" + dataTypeReq +
								"' and interval '" + Interval + "' for time series request." );
						}

						// Else, convert each header object to a TSID string and read the time series.

						if ( readTimeSeries ) {
							Message.printStatus ( 2, "", "Reading " + size + " time series..." );
		
							String tsidentString = null; // TSIdent string.
							TS ts; // Time series to read.
							TimeSeriesCatalog tsCatalog;
							// For multiple time series, progress for the command counts the time series being read
							// (not the values within a time series) so don't show read progress for each time series.
							TSProgressListener tsProgressListener = null;
							HashMap<String,Object> readProperties = createReadProperties (
								debug,
								timeoutSeconds,
								TimePrecision,
								Timezone,
								tsProgressListener,
								Units );
							for ( int i = 0; i < size; i++ ) {
								// Check to see if reading time series should be canceled because the command has been canceled.
								if ( tsprocessor.getCancelProcessingRequested() ) {
									// The user has requested that command processing should be canceled.
									// Check here in this command because a very large query could take a long time before a single command finishes.
									Message.printStatus(2, routine, "Cancel processing based on user request.");
									break;
								}
								// List in order of likelihood to improve performance.
								tsidentString = null; // Do this in case there is no active match.
								tsCatalog = (TimeSeriesCatalog)tsCatalogList.get(i);
								String stationId = tsCatalog.getStationId();
								String dataSource = tsCatalog.getDataSource();
								String dataType = "";
								// Data type is from the catalog (not the original data type).
								String dataTypeFromCatalog = tsCatalog.getDataType();
								if ( (dataTypeFromCatalog != null) && !dataTypeFromCatalog.isEmpty() && !dataTypeFromCatalog.equals("*") ) {
									if ( dataTypeFromCatalog.indexOf(".") > 0 ) {
										dataType += "'" + dataTypeFromCatalog + "'";
									}
									else {
										dataType += dataTypeFromCatalog;
									}
								}
								String interval = tsCatalog.getDataInterval();
								if ( (interval == null) || interval.equals("*") ) {
									// Don't set the interval so called code can determine.
									interval = "";
								}
								tsidentString =
									stationId
									+ "." + dataSource 
									+ "." + dataType
									+ "." + interval;
		            			// Update the progress.
								message = "Reading Campbell Cloud web service time series " + (i + 1) + " of " + size + " \"" + tsidentString + "\"";
	                			notifyCommandProgressListeners ( i, size, (float)-1.0, message );
								if ( tsidIsValid(tsidentString) ) {
									// TSID is valid:
									// - read the time series
									try {
										ts = dataStore.readTimeSeries (
											tsidentString,
											InputStart_DateTime,
											InputEnd_DateTime,
											readData,
											readProperties );
										// Add the time series to the temporary list.  It will be further processed below.
										if ( (ts != null) && (Alias != null) && !Alias.isEmpty() ) {
											ts.setAlias ( TSCommandProcessorUtil.expandTimeSeriesMetadataString(
												processor, ts, Alias, status, commandPhase) );
										}
										// Allow null to be added here.
										tslist.add ( ts );
									}
									catch ( Exception e ) {
										message = "Unexpected error reading Campbell Cloud web service time series \"" + tsidentString + "\" (" + e + ").";
										Message.printWarning ( 2, routine, message );
										if ( commandPhase == CommandPhaseType.DISCOVERY ) {
											status.addToLog ( commandPhase,
												new CommandLogRecord(CommandStatusType.WARNING,
												message, "Verify the time series identifier - may need to fix the database configuration." ) );
										}
										else {
											Message.printWarning ( 2, routine, e );
											++warning_count;
											status.addToLog ( commandPhase,
												new CommandLogRecord(CommandStatusType.FAILURE,
													message, "Report the problem to software support - also see the log file." ) );
										}
									} // Error reading time series.
								} // TSID is valid.
								else {
									// TSID is invalid:
									// - do not read the time series
									message = "Time series identifier \"" + tsidentString + "\" is invalid - not trying to read.";
									Message.printWarning ( 2, routine, message );
									if ( commandPhase == CommandPhaseType.DISCOVERY ) {
										status.addToLog ( commandPhase,
											new CommandLogRecord(CommandStatusType.WARNING,
											message, "Verify the time series identifier - may need to fix the database configuration." ) );
									}
								} // TSID is invalid.
							} // Read a time series.
						} // ReadTimeSeries=true

						if ( (TimeSeriesCatalogTableID != null) && !TimeSeriesCatalogTableID.isEmpty() ) {
							if ( commandPhase == CommandPhaseType.DISCOVERY ) {
								// Create an empty table and set the ID.
								DataTable table = new DataTable();
								table.setTableID ( TimeSeriesCatalogTableID );
								setDiscoveryTable ( table );
							}
							else {
								// In run mode:
								// - create a table from the catalog and set in the processor
								DataTable table = createTimeSeriesCatalogTable ( dataStore, tsCatalogList, TimeSeriesCatalogTableID );
								Message.printStatus ( 2, routine, "Time series catalog table has " + table.getNumberOfRecords() + " rows." );

								// Set the table in the processor.

								PropList request_params = new PropList ( "" );
								request_params.setUsingObject ( "Table", table );
								try {
									processor.processRequest( "SetTable", request_params);
								}
								catch ( Exception e ) {
									message = "Error processing request SetTable(Table=...) in the processor.";
									Message.printWarning(warning_level,
										MessageUtil.formatMessageTag( command_tag, ++warning_count),
										routine, message );
									status.addToLog ( commandPhase,
										new CommandLogRecord(CommandStatusType.FAILURE,
											message, "Report problem to software support." ) );
								}
							}
						} // End saving table.

					} // End haveError.
				} // End reading time series without ${Property}.
			} // End reading multiple time series
		} // Have non-null datastore

        int size = 0;
        if ( tslist != null ) {
            size = tslist.size();
        }
        Message.printStatus ( 2, routine, "Read " + size + " Campbell Cloud web service time series." );

        if ( commandPhase == CommandPhaseType.RUN ) {
            if ( tslist != null ) {
                // Further process the time series.
                // This makes sure the period is at least as long as the output period.

                int wc = TSCommandProcessorUtil.processTimeSeriesListAfterRead( processor, this, tslist );
                if ( wc > 0 ) {
                    message = "Error post-processing Campbell Cloud web service time series after read.";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag(command_tag,
                        ++warning_count), routine, message );
                        status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report the problem to software support." ) );
                    throw new CommandException ( message );
                }

                // Now add the list in the processor.

                int wc2 = TSCommandProcessorUtil.appendTimeSeriesListToResultsList ( processor, this, tslist );
                if ( wc2 > 0 ) {
                    message = "Error adding Campbell Cloud web service time series after read.";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag(command_tag,
                        ++warning_count), routine, message );
                        status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report the problem to software support." ) );
                    throw new CommandException ( message );
                }
            }

            // Warn if nothing was retrieved (can be overridden to ignore):
            // - only show for runtime since discovery may be impacted by ${Property} and other complications
            if ( (tslist == null) || (size == 0) ) {
            	if ( commandPhase == CommandPhaseType.DISCOVERY ) {
					if ( readTimeSeries ) {
						message = "No time series were read from NovaStar web service - may be OK if ${Property} are expanded at run time.";
						Message.printWarning ( warning_level,
							MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
                    	status.addToLog ( commandPhase,
                    		new CommandLogRecord(CommandStatusType.WARNING,
                            message, "Data may not be in the database.  See previous messages." ) );
					}
            	}
            	else {
					if ( readTimeSeries ) {
						message = "No time series were read from NovaStar web service.";
						Message.printWarning ( warning_level,
							MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
                    	status.addToLog ( commandPhase,
                    		new CommandLogRecord(CommandStatusType.WARNING,
                            message, "Data may not be in the database.  See previous messages." ) );
					}
            	}
                // Generate an event for listeners.
                // TOD SAM 2008-08-20 Evaluate whether need here.
                //notifyCommandProcessorEventListeners(new MissingObjectEvent(DataType + ", " + Interval + filter_panel,this));
            }
        }
        else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
            setDiscoveryTSList ( tslist );
        }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message ="Unexpected error reading time series from the Campbell Cloud web service (" + e + ").";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
               message, "Report the problem to software support - also see the log file." ) );
		throw new CommandException ( message );
	}

	// Throw CommandWarningException in case of problems.
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, ++warning_count ),
			routine, message );
		throw new CommandWarningException ( message );
	}

    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the table that is read by this class in discovery mode.
@param table discovery table with TableID set
*/
private void setDiscoveryTable ( DataTable table ) {
    this.__discoveryTable = table;
}

/**
Set the list of time series read in discovery phase.
@param discoveryTSList the list of time series created in discovery mode
*/
private void setDiscoveryTSList ( List<TS> discoveryTSList ) {
    this.__discoveryTSList = discoveryTSList;
}

/**
 * Listen for progress processing a time series and pass the notification to CommandProgressListener method
 * for this command (implemented in AbstractCommand).
 * This is used when single time series are read.
 * No filtering of the messages occurs - if only reporting incremental progress, that logic is in the calling code.
 * The 'commandProgress' method that is called needs to handle other progress parts,
 * such as if a certain amount of the runtime is devoted to the request before results are received.
 * @param istep The number of data reports being processed.
 * Use 0 to initialize the progress and > 0 thereafter.
 * @param nstep The total number of data reports that will be processed.
 * @param percentComplete If >= 0, will be used instead of 'istep' to indicate the progress.
 * @param message A short message describing the status (e.g., "Processing value 1 of 1000..." ).
 */
public void timeSeriesProgress ( int istep, int nstep, float percentComplete, String message ) {
	// Call the similar method on this command class.
	notifyCommandProgressListeners ( istep, nstep, percentComplete, message );
}

/**
Return the string representation of the command.
@param parameters parameters to include in the command
@return the string representation of the command
*/
public String toString ( PropList parameters ) {
	String [] parameterOrder1 = {
		// General (top).
    	"DataStore",
    	"DataType",
    	"Interval",
    	// Match 1.
    	"StationId",
    	"TSID"
	};
  	// Match 1+.
	String delim = ";";
	List<String> whereParameters = new ArrayList<>();
    for ( int i = 1; i <= __numWhere; i++ ) {
    	String where = parameters.getValue("Where" + i);
    	if ( (where != null) && !where.isEmpty() && !where.startsWith(delim) ) {
    		whereParameters.add("Where" + i);
    	}
    }
	String [] parameterOrder2 = {
		// Match 1+.
    	"ReadTimeSeries",
    	"TimeSeriesCatalogTableID",
		// General (bottom).
		"Alias",
		"InputStart",
		"InputEnd",
		"ReadData",
		"TimePrecision",
    	"Timezone",
    	"Units",
    	"Timeout",
		"Debug",
	};

	// Format the final property list.
	String [] parameterOrder = new String[parameterOrder1.length + whereParameters.size() + parameterOrder2.length];
	int iparam = 0;
	for ( int i = 0; i < parameterOrder1.length; i++ ) {
		parameterOrder[iparam++] = parameterOrder1[i];
	}
	for ( int i = 0; i < whereParameters.size(); i++ ) {
		parameterOrder[iparam++] = whereParameters.get(i);
	}
	for ( int i = 0; i < parameterOrder2.length; i++ ) {
		parameterOrder[iparam++] = parameterOrder2[i];
	}
	return this.toString(parameters, parameterOrder);
}

	/**
	 * Check whether the time series identifier is valid.
	 * This is mainly used to check for "NOPOINTTAG" and other parts that will cause an exception when reading.
	 * Avoiding reading bad identifiers will improve performance and avoid confusing errors.
	 */
	 private boolean tsidIsValid ( String tsidentString ) {
		 if ( (tsidentString == null) || tsidentString.isEmpty() ) {
			 return false;
		 }
		 return true;
	 }

}