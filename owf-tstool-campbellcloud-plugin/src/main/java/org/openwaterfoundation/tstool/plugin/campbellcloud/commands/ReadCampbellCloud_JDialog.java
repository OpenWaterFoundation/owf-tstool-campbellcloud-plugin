// ReadCampbellCloud_JDialog - editor for the ReadCampbellCloud() command.

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

package org.openwaterfoundation.tstool.plugin.campbellcloud.commands;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.Station;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.StationComparator;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.campbellcloud.datastore.CampbellCloudDataStore;
import org.openwaterfoundation.tstool.plugin.campbellcloud.ui.CampbellCloud_TimeSeries_InputFilter_JPanel;

import riverside.datastore.DataStore;
import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.TS.TSFormatSpecifiersJPanel;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor for the ReadCampbellCloud() command.
*/
@SuppressWarnings("serial")
public class ReadCampbellCloud_JDialog extends JDialog
implements ActionListener, DocumentListener, ItemListener, KeyListener, WindowListener
{
private SimpleJButton dataStoreDocumentation_JButton = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private SimpleJButton __help_JButton = null;
private ReadCampbellCloud_Command __command = null;
// General (top).
private SimpleJComboBox __DataStore_JComboBox = null;
private SimpleJComboBox __DataType_JComboBox;
private SimpleJComboBox __Interval_JComboBox;
// Single.
private JTabbedPane __tsInfo_JTabbedPane = null;
private SimpleJComboBox __StationId_JComboBox = null;
private JTextField __StationIdNote_JTextField;
private JTextField __DataSource_JTextField;
private JTextField __TSIDFromParts_JTextField;
private JTextField __TSID_JTextField;
// Multiple.
private JPanel __multipleTS_JPanel = null;
private SimpleJComboBox __ReadTimeSeries_JComboBox;
private JTextField __TimeSeriesCatalogTableID_JTextField;
// General (bottom).
private TSFormatSpecifiersJPanel __Alias_JTextField = null;
private JTextField __InputStart_JTextField;
private JTextField __InputEnd_JTextField;
private SimpleJComboBox __ReadData_JComboBox = null;
private SimpleJComboBox __IncludeNullData_JComboBox = null;
private SimpleJComboBox __TimePrecision_JComboBox = null;
private SimpleJComboBox __Timezone_JComboBox;
private JTextField __Units_JTextField;
private JTextField __Timeout_JTextField;
private SimpleJComboBox	__Debug_JComboBox;
private JTextArea __command_JTextArea = null;

// Contains all input filter panels.  Use the CampbellCloudDataStore name/description and data type for each to
// figure out which panel is active at any time.
// Using the general panel and casting later causes a ClassCastException since classes are loaded in different ClassLoader.
// private List<InputFilter_JPanel> __inputFilterJPanelList = new ArrayList<>();
private List<CampbellCloud_TimeSeries_InputFilter_JPanel> __inputFilterJPanelList = new ArrayList<>();
private CampbellCloudDataStore __dataStore = null; // Selected CampbellCloudDataStore.
private boolean __error_wait = false; // Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Was OK pressed when closing the dialog?
private boolean __ignoreEvents = false; // Used to ignore cascading events when initializing the components.

private List<Prop> __propList = null; // List of discovery mode Prop so that DataStore can be a ${Property}.

private String dataTypeInitial = null; // Initial DataType command parameter, to allow ${Property} to be shown in the list.
private String stationIdInitial = null; // Initial LocId command parameter, to allow ${Property} to be shown in the list.
private String timezoneInitial = null; // Initial Timezone command parameter, to allow ${Property} to be shown in the list.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param propList list of discovery mode Prop, so that DataStore can be a ${Property}
*/
public ReadCampbellCloud_JDialog ( JFrame parent, ReadCampbellCloud_Command command, List<Prop> propList ) {
	super(parent, true);
	initialize ( parent, command, propList );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event ) {
	if ( __ignoreEvents ) {
        return; // Startup.
    }
    Object o = event.getSource();

    if ( o == __cancel_JButton ) {
        response ( false );
    }
    else if ( o == dataStoreDocumentation_JButton ) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse ( new URI(dataStoreDocumentation_JButton.getActionCommand()) );
        }
        catch ( Exception e ) {
            Message.printWarning(1, null, "Unable to display Campbell Cloud web services documentation using \"" +
                dataStoreDocumentation_JButton.getActionCommand() + "\"" );
        }
    }
	else if ( o == __help_JButton ) {
		HelpViewer.getInstance().showHelp("command", "ReadCampbellCloud",
			"https://software.openwaterfoundation.org/tstool-campbellcloud-plugin/latest/doc-user");
	}
    else if ( o == __ok_JButton ) {
        refresh ();
        checkInput ();
        if ( !__error_wait ) {
            response ( true );
        }
    }
    else {
        // ComboBoxes.
        refresh();
    }
}

/**
Refresh the data type choices in response to the currently selected Campbell Cloud datastore.
@param value if non-null, then the selection is from the command initialization,
in which case the specified data type should be selected
*/
private void actionPerformedDataStoreSelected ( ) {
    if ( __DataStore_JComboBox.getSelected() == null ) {
        // Startup initialization.
        return;
    }
    setDataStoreForSelectedInput();
    //Message.printStatus(2, "", "Selected data store " + __dataStore + " __dmi=" + __dmi );
    // Now populate the data type choices corresponding to the data store
    populateDataTypeChoices ( getSelectedDataStore(), this.dataTypeInitial );
}

/**
Refresh the query choices for the currently selected Campbell Cloud datastore.
@param value if non-null, then the selection is from the command initialization,
in which case the specified data type should be selected
*/
private void actionPerformedDataTypeSelected ( ) {
    if ( __DataType_JComboBox.getSelected() == null ) {
        // Startup initialization.
        return;
    }
    // Populate the interval choices corresponding to the data type.
    populateIntervalChoices ( getSelectedDataStore() );
    //populateStationIdChoices ( getSelectedDataStore() );
}

/**
Set visible the appropriate input filter, based on the interval and other previous selections.
*/
private void actionPerformedIntervalSelected ( ) {
    if ( __Interval_JComboBox.getSelected() == null ) {
        // Startup initialization.
        return;
    }
    // Populate the filters corresponding to the data type and interval.
    selectInputFilter ( getDataStore() );
    // Populate the station identifiers.
    populateStationIdChoices ( getSelectedDataStore(), this.stationIdInitial );
}

/**
Refresh the query choices for the currently selected Campbell Cloud location identifier.
@param value if non-null, then the selection is from the command initialization,
in which case the specified data type should be selected
*/
/*
private void actionPerformedStationIdSelected ( ) {
    if ( __StationId_JComboBox.getSelected() == null ) {
        // Startup initialization.
        return;
    }
}
*/

// Start event handlers for DocumentListener...

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void changedUpdate ( DocumentEvent e ) {
	checkGUIState();
    refresh();
}

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void insertUpdate ( DocumentEvent e ) {
	checkGUIState();
    refresh();
}

/**
Handle DocumentEvent events.
@param e DocumentEvent to handle.
*/
public void removeUpdate ( DocumentEvent e ) {
	checkGUIState();
    refresh();
}

// ...end event handlers for DocumentListener.

/**
Check the state of the dialog, disabling/enabling components as appropriate.
*/
private void checkGUIState() {
	// If "AllMatchingTSID", enable the list.
	// Otherwise, clear and disable.
	if ( __DataType_JComboBox != null ) {
		String DataType = getSelectedDataType();
		if ( DataType == null ) {
		    // Initialization.
		    DataType = "*";
		}
	}

    // If datastore is selected and has the property for API documentation, enable the documentation buttons.
    CampbellCloudDataStore dataStore = getSelectedDataStore();
    if ( dataStore != null ) {
        String urlString = dataStore.getProperty ( "ServiceAPIDocumentationUrl" );
        if ( urlString == null ) {
            this.dataStoreDocumentation_JButton.setEnabled(false);
        }
        else {
            this.dataStoreDocumentation_JButton.setActionCommand(urlString);
            this.dataStoreDocumentation_JButton.setEnabled(true);
        }
    }
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag to true.
This should be called before response() is allowed to complete.
*/
private void checkInput () {
	if ( __ignoreEvents ) {
        return; // Startup.
    }
    // Create a list of parameters to check.
	PropList props = new PropList ( "" );
	__error_wait = false;

	// General (top).
    String DataStore = __DataStore_JComboBox.getSelected();
    if ( DataStore.length() > 0 ) {
        props.set ( "DataStore", DataStore );
    }
    String DataType = getSelectedDataType();
	if ( DataType.length() > 0 ) {
		props.set ( "DataType", DataType );
	}
	String Interval = getSelectedInterval();
	if ( Interval.length() > 0 ) {
		props.set ( "Interval", Interval );
	}
    
    // Single.
	String StationId = getSelectedStationId();
	if ( StationId.length() > 0 ) {
		props.set ( "StationId", StationId );
	}
	String TSID = __TSID_JTextField.getText().trim();
	if ( TSID.length() > 0 ) {
		props.set ( "TSID", TSID );
	}
    
    // Multiple.
	InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	int whereCount = 0; // Number of non-empty Where parameters specified.
	if ( filterPanel != null ) {
		if ( filterPanel != null ) {
    		for ( int i = 1; i <= filterPanel.getNumFilterGroups(); i++ ) {
    	    	String where = getWhere ( i - 1 );
    	    	// Blank where is something like: ";operator;"
    	    	if ( !where.isEmpty() && !where.startsWith(";") && !where.endsWith(";") ) {
    	    		++whereCount;
    	    	}
    	    	if ( where.length() > 0 ) {
    	        	props.set ( "Where" + i, where );
    	    	}
        	}
		}
	}
    if ( whereCount > 0 ) {
        // Input filters are specified so check:
    	// - this is done in the input filter because that code is called from this command and main TSTool UI
        InputFilter_JPanel ifp = getVisibleInputFilterPanel();
        if ( ifp != null ) {
        	// Set a property to pass to the general checkCommandParameters method so that the
        	// results can be combined with the other command parameter checks.
        	props.set("InputFiltersCheck",ifp.checkInputFilters(false));
        }
    }
	String ReadTimeSeries = __ReadTimeSeries_JComboBox.getSelected();
	if ( !ReadTimeSeries.isEmpty() ) {
		props.set ( "ReadTimeSeries", ReadTimeSeries );
	}
	String TimeSeriesCatalogTableID = __TimeSeriesCatalogTableID_JTextField.getText().trim();
	if ( !TimeSeriesCatalogTableID.isEmpty() ) {
		props.set ( "TimeSeriesCatalogTableID", TimeSeriesCatalogTableID );
	}
    
    // General (bottom).
	String Alias = __Alias_JTextField.getText().trim();
	if ( Alias.length() > 0 ) {
		props.set ( "Alias", Alias );
	}
	String InputStart = __InputStart_JTextField.getText().trim();
	if ( InputStart.length() > 0 ) {
		props.set ( "InputStart", InputStart );
	}
	String InputEnd = __InputEnd_JTextField.getText().trim();
	if ( InputEnd.length() > 0 ) {
		props.set ( "InputEnd", InputEnd );
	}
	String ReadData = __ReadData_JComboBox.getSelected();
	if ( !ReadData.isEmpty() ) {
		props.set ( "ReadData", ReadData );
	}
    String IncludeNullData = __IncludeNullData_JComboBox.getSelected();
    if ( IncludeNullData.length() > 0 ) {
        props.set ( "IncludeNullData", IncludeNullData );
    }
    String TimePrecision = __TimePrecision_JComboBox.getSelected();
    if ( TimePrecision.length() > 0 ) {
        props.set ( "TimePrecision", TimePrecision );
    }
	String Timezone = __Timezone_JComboBox.getSelected();
	if ( Timezone.length() > 0 ) {
		props.set ( "Timezone", Timezone );
	}
	String Units = __Units_JTextField.getText().trim();
	if ( !Units.isEmpty() ) {
		props.set ( "Units", Units );
	}
	String Timeout = __Timeout_JTextField.getText().trim();
	if ( !Timeout.isEmpty() ) {
		props.set ( "Timeout", Timeout );
	}
	String Debug = __Debug_JComboBox.getSelected();
	if ( Debug.length() > 0 ) {
		props.set ( "Debug", Debug );
	}

	try {
	    // This will warn the user.
		__command.checkCommandParameters ( props, null, 1 );
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.
In this case the command parameters have already been checked and no errors were detected.
*/
private void commitEdits () {
	// General (top).
	String DataStore = __DataStore_JComboBox.getSelected();
    __command.setCommandParameter ( "DataStore", DataStore );
	String DataType = getSelectedDataType();
	__command.setCommandParameter ( "DataType", DataType );
	String Interval = getSelectedInterval();
	__command.setCommandParameter ( "Interval", Interval );

	// Single.
	String StationId = getSelectedStationId();
	__command.setCommandParameter ( "StationId", StationId );
	String TSID = __TSID_JTextField.getText().trim();
	__command.setCommandParameter ( "TSID", TSID );

	// Multiple.
	String delim = ";";
	//InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	CampbellCloud_TimeSeries_InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	if ( filterPanel != null ) {
		for ( int i = 1; i <= filterPanel.getNumFilterGroups(); i++ ) {
	    	String where = getWhere ( i - 1 );
	    	if ( where.startsWith(delim) ) {
	        	where = "";
	    	}
	    	__command.setCommandParameter ( "Where" + i, where );
		}
	}
	String ReadTimeSeries = __ReadTimeSeries_JComboBox.getSelected();
	__command.setCommandParameter ( "ReadTimeSeries", ReadTimeSeries );
	String TimeSeriesCatalogTableID = __TimeSeriesCatalogTableID_JTextField.getText().trim();
	__command.setCommandParameter ( "TimeSeriesCatalogTableID", TimeSeriesCatalogTableID );

	// General (bottom).
	String Alias = __Alias_JTextField.getText().trim();
	__command.setCommandParameter ( "Alias", Alias );
	// Both versions of the commands use these.
	String InputStart = __InputStart_JTextField.getText().trim();
	__command.setCommandParameter ( "InputStart", InputStart );
	String InputEnd = __InputEnd_JTextField.getText().trim();
	__command.setCommandParameter ( "InputEnd", InputEnd );
	String ReadData = __ReadData_JComboBox.getSelected();
	__command.setCommandParameter (	"ReadData", ReadData );
	String IncludeNullData = __IncludeNullData_JComboBox.getSelected();
	__command.setCommandParameter (	"IncludeNullData", IncludeNullData );
	String TimePrecision = __TimePrecision_JComboBox.getSelected();
	__command.setCommandParameter (	"TimePrecision", TimePrecision );
	String Timezone = __Timezone_JComboBox.getSelected();
	__command.setCommandParameter ( "Timezone", Timezone );
	String Units = __Units_JTextField.getText().trim();
	__command.setCommandParameter ( "Units", Units );
	String Timeout = __Timeout_JTextField.getText().trim();
	__command.setCommandParameter ( "Timeout", Timeout );
	String Debug = __Debug_JComboBox.getSelected();
	__command.setCommandParameter (	"Debug", Debug );
}

/**
Return the datastore that is in effect.
@return the datastore that is in effect
*/
private CampbellCloudDataStore getDataStore() {
    return __dataStore;
}

/**
Get the input filter list.
@return the input filter list
*/
private List<CampbellCloud_TimeSeries_InputFilter_JPanel> getInputFilterJPanelList () {
    return __inputFilterJPanelList;
}

/**
Get the input name to use for the TSID.
@return the input name to use for the TSID
*/
private String getInputNameForTSID() {
    // Use the data store name if specified.
    String DataStore = __DataStore_JComboBox.getSelected();
    if ( (DataStore != null) && !DataStore.equals("") ) {
        return DataStore;
    }
    else {
        return "CampbellCloud"; // Default.
    }
}

/**
Get the selected data store from the processor.
@return the selected data store from the processor
*/
private CampbellCloudDataStore getSelectedDataStore () {
	String routine = getClass().getSimpleName() + ".getSelectedDataStore";
    String DataStore = __DataStore_JComboBox.getSelected();
    CampbellCloudDataStore dataStore = (CampbellCloudDataStore)((TSCommandProcessor)
        __command.getCommandProcessor()).getDataStoreForName( DataStore, CampbellCloudDataStore.class );
    if ( dataStore != null ) {
        //Message.printStatus(2, routine, "Selected datastore is \"" + dataStore.getName() + "\"." );
    }
    else {
        Message.printStatus(2, routine, "Cannot get datastore for \"" + DataStore + "\"." );
    }
    return dataStore;
}

/**
Return the selected data type, omitting the trailing SHEF code from "dataType - SHEF PE", should it be present.
However, include the statistic, as in "WaterLevelRiver-Max".
@return the selected data type
*/
private String getSelectedDataType() {
    if ( __DataType_JComboBox == null ) {
        return null;
    }
    String dataType = __DataType_JComboBox.getSelected();
    if ( dataType == null ) {
    	return dataType;
    }
    // Make sure to use spaces around the dashes because dash without space is used to indicate statistic,
    // and want that included in the data type.
  	int pos = dataType.indexOf(" - ");
    if ( pos > 0 ) {
    	// Return the first item.
        dataType = dataType.substring(0,pos).trim();
    }
    else {
    	// Return the full string.
        dataType = dataType.trim();
    }
    return dataType;
}

/**
Return the selected data interval, omitting the trailing SHEF code from "Interval - SHEF duration", should it be present.
@return the selected data interval
*/
private String getSelectedInterval() {
    if ( __Interval_JComboBox == null ) {
        return null;
    }
    String interval = __Interval_JComboBox.getSelected();
    if ( interval == null ) {
    	return interval;
    }
   	int pos = interval.indexOf(" - ");
    if ( pos > 0 ) {
    	// Return the first item.
        interval = interval.substring(0,pos).trim();
    }
    else {
    	// Return the full string.
        interval = interval.trim();
    }
    return interval;
}

/**
 * Get the selected station ID.
 * Only the actual station ID is returned.  The informative note is discarded.
 * @return the selected LocId
 */
private String getSelectedStationId() {
	String stationId = __StationId_JComboBox.getSelected();
	if ( stationId == null ) {
		return null;
	}
	else {
		stationId = stationId.trim();
		int pos = stationId.indexOf(" ");
		if ( pos > 0 ) {
			return stationId.substring(0,pos);
		}
		else {
			return stationId;
		}
	}
}

/**
Return the visible input filter panel, or null if none visible.
@return the visible input filter panel
*/
//private InputFilter_JPanel getVisibleInputFilterPanel() {
private CampbellCloud_TimeSeries_InputFilter_JPanel getVisibleInputFilterPanel() {
    //List<InputFilter_JPanel> panelList = getInputFilterJPanelList();
    List<CampbellCloud_TimeSeries_InputFilter_JPanel> panelList = getInputFilterJPanelList();
    String panelName;
    //for ( InputFilter_JPanel panel : panelList ) {
    for ( CampbellCloud_TimeSeries_InputFilter_JPanel panel : panelList ) {
        // Skip default.
        panelName = panel.getName();
        if ( (panelName != null) && panelName.equalsIgnoreCase("Default") ) {
            continue;
        }
        if ( panel.isVisible() ) {
        	if ( Message.isDebugOn ) {
        		Message.printStatus(2,"","Visible filter panel name is \"" + panelName + "\"");
        	}
            return panel;
        }
    }
    return null;
}

/**
Return the "WhereN" parameter for the requested input filter.
@param ifg the Input filter to process (zero index).
@return the "WhereN" parameter for the requested input filter.
*/
private String getWhere ( int ifg ) {
	String delim = ";";	// To separate input filter parts.
	//InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	CampbellCloud_TimeSeries_InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
    String where = "";
    if ( filterPanel != null ) {
    	// Use the internal value for the where to ensure integration.
        where = filterPanel.toString(ifg,delim,3).trim();
    }
	return where;
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param propList list of discovery mode Prop, so that DataStore can be a ${Property}
*/
private void initialize ( JFrame parent, ReadCampbellCloud_Command command, List<Prop> propList ) {
	//String routine = getClass().getSimpleName() + ".initialize";
	this.__command = command;
	this.__propList = propList;
	CommandProcessor processor = __command.getCommandProcessor();
	addWindowListener( this );
    Insets insetsTLBR = new Insets(2,2,2,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
    	"Read 1+ time series from a Campbell Cloud web services datastore, using options from the choices below to select time series."),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Refer to the Campbell Cloud web services documentation for information about data types (datastream fields) and intervals (always IrregSecond)." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specifying the period will limit data that are available " +
		"for later commands but can increase performance." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Reading time series for a single location takes precedence over reading multiple time series." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
   	JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Requests may be constrained by the software to prevent unintended large bulk queries." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    // Add buttons for the documentation:
    // - the checkGUIState() method checks for and sets the URL in the button's action

	this.dataStoreDocumentation_JButton = new SimpleJButton("Campbell Cloud Documentation", this);
	this.dataStoreDocumentation_JButton.setToolTipText("View the Campbell Cloud documentation for the datastore in a web browser.");
    JGUIUtil.addComponent(main_JPanel, this.dataStoreDocumentation_JButton,
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

   	__ignoreEvents = true; // So that a full pass of initialization can occur.

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Campbell Cloud datastore:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataStore_JComboBox = new SimpleJComboBox ( false );
    TSCommandProcessor tsProcessor = (TSCommandProcessor)processor;
    Message.printStatus(2, "ReadCampbellCloud", "Getting datastores for CampbellCLoudDataStore class");
    List<DataStore> dataStoreList = tsProcessor.getDataStoresByType( CampbellCloudDataStore.class );
    // Datastore is required, so no blank
    List<String> datastoreChoices = new ArrayList<>();
    for ( DataStore dataStore: dataStoreList ) {
    	datastoreChoices.add ( dataStore.getName() );
    }
    __DataStore_JComboBox.setData(datastoreChoices);
    if ( datastoreChoices.size() > 0 ) {
    	__DataStore_JComboBox.select ( 0 );
    }
    __DataStore_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __DataStore_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("Required - Campbell Cloud datastore."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    //JGUIUtil.addComponent(main_JPanel, inputFilterJPanel,
    //    0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Data type:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__DataType_JComboBox = new SimpleJComboBox ( true ); // Allow editing so that properties can be used.
    __DataType_JComboBox.setPrototypeDisplayValue("CAMPBELLCLOUD-SOMEORGANIZATIONNAME123456789"); // Same as DataStore.
	__DataType_JComboBox.setToolTipText("Data types from Campbell Cloud 'field', used in TSID data type, can use ${Property}.");
	__DataType_JComboBox.setMaximumRowCount(20);
	__DataType_JComboBox.addItemListener ( this );
    __DataType_JComboBox.getEditor().getEditorComponent().addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __DataType_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required to match a single location - data type for time series."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Data interval:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__Interval_JComboBox = new SimpleJComboBox ();
	__Interval_JComboBox.setToolTipText("Data interval for Campbell Cloud time series, currently always IrregSecond.");
	__Interval_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Interval_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required to match a single location - data interval (time step) for time series."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    __tsInfo_JTabbedPane = new JTabbedPane ();
    __tsInfo_JTabbedPane.setBorder(
        BorderFactory.createTitledBorder ( BorderFactory.createLineBorder(Color.black),
        "Indicate how to match time series in Campbell Cloud" ));
    JGUIUtil.addComponent(main_JPanel, __tsInfo_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JPanel singleTS_JPanel = new JPanel();
    singleTS_JPanel.setLayout(new GridBagLayout());
    __tsInfo_JTabbedPane.addTab ( "Match Single Time Series", singleTS_JPanel );

    int ySingle = -1;
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel(
    	"Match a single time series for a location (station_id)."),
        0, ++ySingle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel(
    	"A unique TSID is formed from the Campbell Cloud station ID and data type (datastream field)."),
        0, ++ySingle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel(
    	"The data type and interval must be specified above (DO NOT USE * for data type)."),
        0, ++ySingle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++ySingle, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Station ID:"),
        0, ++ySingle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__StationId_JComboBox = new SimpleJComboBox ( true ); // Allow edit so that ${Property} can be specified.
    __StationId_JComboBox.setToolTipText("Station identifier (station_id) to match.");
	__StationId_JComboBox.setMaximumRowCount(20);
	__StationId_JComboBox.addItemListener ( this ); // For choice selection.
    JTextComponent tc = (JTextComponent)__StationId_JComboBox.getEditor().getEditorComponent();
    tc.getDocument().addDocumentListener ( this ); // For text field keys, copy and paste.
    JGUIUtil.addComponent(singleTS_JPanel, __StationId_JComboBox,
        1, ySingle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Used in the TSID."),
        3, ySingle, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    __StationIdNote_JTextField = new JTextField ( "", 50 );
    __StationIdNote_JTextField.setEditable ( false );
    JGUIUtil.addComponent(singleTS_JPanel, __StationIdNote_JTextField,
        1, ++ySingle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Information."),
		3, ySingle, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Data source:"),
        0, ++ySingle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __DataSource_JTextField = new JTextField ( "CampbellCloud", 20 );
    // Protect the size because sometimes can shrink to the minimum size.
    Dimension d = __DataSource_JTextField.getPreferredSize();
    __DataSource_JTextField.setMinimumSize(d);
    __DataSource_JTextField.setMaximumSize(d);
    __DataSource_JTextField.setToolTipText("Data source to match, currently always 'CampbellCloud'");
    __DataSource_JTextField.setEditable(false);
    __DataSource_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(singleTS_JPanel, __DataSource_JTextField,
        1, ySingle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Used in the TSID, currently always 'CampbellCloud'."),
        3, ySingle, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "TSID from parts (full):"),
        0, ++ySingle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TSIDFromParts_JTextField = new JTextField ( "" );
    __TSIDFromParts_JTextField.setEditable ( false );
    __TSIDFromParts_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(singleTS_JPanel, __TSIDFromParts_JTextField,
        1, ySingle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ( "Information - created from above parameters."),
        3, ySingle, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(singleTS_JPanel, new JLabel("TSID:"),
        0, ++ySingle, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TSID_JTextField = new JTextField(40);
    __TSID_JTextField.setToolTipText("Time series identifier to request, can use ${Property}.");
    __TSID_JTextField.addKeyListener ( this );
    __TSID_JTextField.getDocument().addDocumentListener(this);
    JGUIUtil.addComponent(singleTS_JPanel, __TSID_JTextField,
        1, ySingle, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(singleTS_JPanel, new JLabel ("Optional - TSID to request (default=use other query parameters)."),
        3, ySingle, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    __multipleTS_JPanel = new JPanel();
    __multipleTS_JPanel.setLayout(new GridBagLayout());
    __tsInfo_JTabbedPane.addTab ( "Match 1+ Time Series", __multipleTS_JPanel );
    // Note to warn about performance.
    int yMult = -1;
    JGUIUtil.addComponent(__multipleTS_JPanel, new JLabel("Use filters (\"where\" clauses) to limit result size and " +
        "increase performance.  Filters are AND'ed."),
        0, ++yMult, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(__multipleTS_JPanel, new JLabel(
    	"The time series catalog is used to filter time series and then each time series data are read."),
        0, ++yMult, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(__multipleTS_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yMult, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    // Initialize all the filters (selection will be based on data store).
    initializeInputFilters ( __multipleTS_JPanel, ++yMult, dataStoreList );

    JGUIUtil.addComponent(__multipleTS_JPanel, new JLabel ( "Read time series?:"),
		0, ++yMult, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> ReadTimeSeries_List = new ArrayList<>( 3 );
	ReadTimeSeries_List.add ( "" );
	ReadTimeSeries_List.add ( __command._False );
	ReadTimeSeries_List.add ( __command._True );
	__ReadTimeSeries_JComboBox = new SimpleJComboBox ( false );
	__ReadTimeSeries_JComboBox.setToolTipText("Read time series? Used when only reading the time series catalog.");
	__ReadTimeSeries_JComboBox.setData ( ReadTimeSeries_List);
	__ReadTimeSeries_JComboBox.select ( 0 );
	__ReadTimeSeries_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(__multipleTS_JPanel, __ReadTimeSeries_JComboBox,
		1, yMult, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(__multipleTS_JPanel, new JLabel (
		"Optional - read time series (default=" + __command._True + ")?"),
		3, yMult, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(__multipleTS_JPanel, new JLabel ("Time series catalog table ID:"),
        0, ++yMult, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TimeSeriesCatalogTableID_JTextField = new JTextField (20);
    __TimeSeriesCatalogTableID_JTextField.setToolTipText("Table ID to save the time series catalog.");
    __TimeSeriesCatalogTableID_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(__multipleTS_JPanel, __TimeSeriesCatalogTableID_JTextField,
        1, yMult, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(__multipleTS_JPanel, new JLabel ( "Optional - table for catalog (default=no table)."),
        3, yMult, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel("Alias to assign:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Alias_JTextField = new TSFormatSpecifiersJPanel(10);
    __Alias_JTextField.setToolTipText("Use %L for location, %T for data type, %I for interval.");
    __Alias_JTextField.addKeyListener ( this );
    __Alias_JTextField.getDocument().addDocumentListener(this);
    __Alias_JTextField.setToolTipText("%L for location, %T for data type.");
    JGUIUtil.addComponent(main_JPanel, __Alias_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - use %L for location, etc. (default=no alias)."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Input start:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InputStart_JTextField = new JTextField (30);
    __InputStart_JTextField.setToolTipText("Starting date/time to read data (default is current minus 30 days), default timezone is computer's, can use ${Property}");
    __InputStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __InputStart_JTextField,
        1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - overrides the global input start (default=last 30 days)."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Input end:"),
        0, ++y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InputEnd_JTextField = new JTextField (30);
    __InputEnd_JTextField.setToolTipText("Ending date/time to read data (default is current time), default timezone is computer's, can use ${Property}");
    __InputEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __InputEnd_JTextField,
        1, y, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - overrides the global input end (default=current time)."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Read data?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> ReadData_List = new ArrayList<>( 3 );
	ReadData_List.add ( "" );
	ReadData_List.add ( __command._False );
	ReadData_List.add ( __command._True );
	__ReadData_JComboBox = new SimpleJComboBox ( false );
	__ReadData_JComboBox.setToolTipText("Read data? Useful to streamline workflow development.");
	__ReadData_JComboBox.setData ( ReadData_List);
	__ReadData_JComboBox.select ( 0 );
	__ReadData_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __ReadData_JComboBox,
		1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - read data values (default=" + __command._True + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Include null data?:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> IncludeNullData_List = new ArrayList<>( 3 );
	IncludeNullData_List.add ( "" );
	IncludeNullData_List.add ( __command._False );
	IncludeNullData_List.add ( __command._True );
	__IncludeNullData_JComboBox = new SimpleJComboBox ( false );
	__IncludeNullData_JComboBox.setToolTipText("Include null data values (useful for troubleshooting)?");
	__IncludeNullData_JComboBox.setData ( IncludeNullData_List);
	__IncludeNullData_JComboBox.select ( 0 );
	__IncludeNullData_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __IncludeNullData_JComboBox,
		1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - include null data values (default=" + __command._False + ")."),
		3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Time precision:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> timePrecision_List = new ArrayList<>( 6 );
	timePrecision_List.add("");
	timePrecision_List.add("Second");
	timePrecision_List.add("HSecond");
	timePrecision_List.add("MilliSecond");
	timePrecision_List.add("MicroSecond");
	timePrecision_List.add("NanoSecond");
	__TimePrecision_JComboBox = new SimpleJComboBox ( false );
	__TimePrecision_JComboBox.setToolTipText("Precision for time associated with data values.");
	__TimePrecision_JComboBox.setData(timePrecision_List);
	__TimePrecision_JComboBox.select ( 0 );
	__TimePrecision_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __TimePrecision_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - precision for time (default=MilliSecond)."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Timezone:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Timezone_JComboBox = new SimpleJComboBox ( true ); // Allow editing so that properties can be used.
    __Timezone_JComboBox.setToolTipText("Timezone for response, for example US/Eastern (default is UTC), can use ${Property}.");
	__Timezone_JComboBox.addActionListener ( this );
    __Timezone_JComboBox.getEditor().getEditorComponent().addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Timezone_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - output timezone (default = system/station timezone)."),
        3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Units:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Units_JTextField = new JTextField ( "", 20 );
    __Units_JTextField.setToolTipText("Data units for the time series, needed because Campbell Cloud does not provide.");
    __Units_JTextField.setToolTipText("Units for time series.");
    __Units_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Units_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - data units for time series."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Timeout:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Timeout_JTextField = new JTextField ( "", 20 );
    __Timeout_JTextField.setToolTipText("Timeout for connection and read, in seconds.");
    __Timeout_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Timeout_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - timeout for requests, seconds (default = 300 = 5 minutes)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Debug:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    List<String> Debug_List = new ArrayList<>( 3 );
	Debug_List.add ( "" );
	Debug_List.add ( __command._False );
	Debug_List.add ( __command._True );
	__Debug_JComboBox = new SimpleJComboBox ( false );
	__Debug_JComboBox.setToolTipText("Enable debug for web services, used for troubleshooting).");
	__Debug_JComboBox.setData ( Debug_List);
	__Debug_JComboBox.select ( 0 );
	__Debug_JComboBox.addActionListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Debug_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Optional - enable debug for web services (default=" + __command._False + ")."),
		3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,50);
    // Protect the size because sometimes can shrink to the minimum size.
    d = __command_JTextArea.getPreferredSize();
    __command_JTextArea.setMinimumSize(d);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	// Refresh the contents (still ignoring events).
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel,
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__ok_JButton = new SimpleJButton("OK", this);
	__ok_JButton.setToolTipText("Save changes to command");
	button_JPanel.add ( __ok_JButton );
	__cancel_JButton = new SimpleJButton( "Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__cancel_JButton.setToolTipText("Cancel without saving changes to command");
	button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
	__help_JButton.setToolTipText("Show command documentation in web browser");

	setTitle ( "Edit " + __command.getCommandName() + " Command" );

    // Because it is necessary to select the proper input filter during initialization (to transfer an old command's parameter values),
	// the selected input filter may not be desirable for dialog sizing.
	// Therefore, manually set all panels to visible and then determine the preferred size as the maximum.
	// Then reselect the appropriate input filter before continuing.
    setAllFiltersVisible();
    // All filters are visible at this point so pack chooses good sizes.
    pack();
    setPreferredSize(getSize()); // Will reflect all filters being visible.
    __multipleTS_JPanel.setPreferredSize(__multipleTS_JPanel.getSize()); // So initial height is maximum height.
    selectInputFilter( getDataStore()); // Now go back to the filter for the selected input type and intern.
    JGUIUtil.center( this );
    __ignoreEvents = false; // After initialization of components let events happen to dynamically cause cascade.
    // Now refresh once more.
	refresh();
	checkGUIState(); // Do this again because it may not have happened due to the special event handling.
	// TODO smalers 2025-09-30 have some issues with controls not sizing well.
	//setResizable ( false );
    super.setVisible( true );
}

/**
Initialize input filters for all of the available Campbell Cloud datastores.
The input filter panels will be layered on top of each other, but only one will be set visible,
based on the other visible selections.
@param parent_JPanel the panel to receive the input filter panels
@param y position in the layout to add the input filter panel
@param dataStoreList the list of available CampbellCloudDataStore
*/
private void initializeInputFilters ( JPanel parent_JPanel, int y, List<DataStore> dataStoreList ) {
	String routine = getClass().getSimpleName() + ".initializeInputFilters";
    // Loop through data stores and add filters for all data groups.
    for ( DataStore ds : dataStoreList ) {
    	Message.printStatus(2,routine,"Initializing data store list for datastore name \"" + ds.getName() +
    		"\" class: " + ds.getClass() );
    	Message.printStatus(2, routine, "Casting to CampbellCloudDataStore class: " + CampbellCloudDataStore.class);
        initializeInputFilters_OneFilter ( parent_JPanel, y, (CampbellCloudDataStore)ds);
    }

    // Blank panel indicating data type was not matched.
    // Add in the same position as the other filter panels.

    int buffer = 3;
    Insets insets = new Insets(1,buffer,1,buffer);
    //List<InputFilter_JPanel> ifPanelList = getInputFilterJPanelList();
    List<CampbellCloud_TimeSeries_InputFilter_JPanel> ifPanelList = getInputFilterJPanelList();
    //InputFilter_JPanel panel = new InputFilter_JPanel("Data type and interval have no input filters.");
    CampbellCloud_TimeSeries_InputFilter_JPanel panel =
    	new CampbellCloud_TimeSeries_InputFilter_JPanel("Data type and interval have no input filters.");
    panel.setName("Default");
    JGUIUtil.addComponent(parent_JPanel, panel,
        0, y, 7, 1, 0.0, 0.0, insets, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
    ifPanelList.add ( panel );
}

/**
Initialize input filters for one NoavStar web service datastore.
@param parent_JPanel the panel to receive the input filter panels
@param y for layout
@param dataStore datastore to use with the filter
*/
private void initializeInputFilters_OneFilter ( JPanel parent_JPanel, int y, CampbellCloudDataStore dataStore ) {
	String routine = getClass().getSimpleName() + ".initializeInputFilters_OneFilter";
    int buffer = 3;
    Insets insets = new Insets(1,buffer,1,buffer);
    //List<InputFilter_JPanel> inputFilterJPanelList = getInputFilterJPanelList();
    List<CampbellCloud_TimeSeries_InputFilter_JPanel> inputFilterJPanelList = getInputFilterJPanelList();

    boolean visibility = true; // Set this so that the layout manager will figure out the size of the dialog at startup.
    int x = 0; // Position in layout manager, same for all since overlap.
    //int numVisibleChoices = -1; // For the combobox choices, -1 means size to data list size.
    try {
        // Time series.
        CampbellCloud_TimeSeries_InputFilter_JPanel panel = new CampbellCloud_TimeSeries_InputFilter_JPanel ( dataStore, 5 );
        //panel.setName(dataStore.getName() + ".Station" );
        panel.setName(dataStore.getName() );
        JGUIUtil.addComponent(parent_JPanel, panel,
            x, y, 7, 1, 0.0, 0.0, insets, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
        inputFilterJPanelList.add ( panel );
        panel.addEventListeners ( this );
        panel.setVisible ( visibility );
    }
    catch ( Exception e ) {
        Message.printWarning ( 2, routine,
        "Unable to initialize input filter for Campbell Cloud time series catalog (" + e + ")." );
        Message.printWarning ( 3, routine, e );
    }
}

/**
Respond to ItemEvents.
*/
public void itemStateChanged ( ItemEvent event ) {
    if ( __ignoreEvents ) {
        return; // Startup.
    }
    if ( (event.getSource() == __DataStore_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
        // User has selected a data store.
        actionPerformedDataStoreSelected ();
    }
    else if ( (event.getSource() == __DataType_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
        // User has selected a data type.
        actionPerformedDataTypeSelected ();
    }
    else if ( (event.getSource() == __Interval_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
        // User has selected an interval.
        actionPerformedIntervalSelected ();
    }
    /*
    else if ( (event.getSource() == __StationId_JComboBox) && (event.getStateChange() == ItemEvent.SELECTED) ) {
        // User has selected a data type.
        actionPerformedStationIdSelected ();
    }
    */
    refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event ) {
	refresh();
}

/**
Need this to properly capture key events, especially deletes.
*/
public void keyReleased ( KeyEvent event ) {
	refresh();
}

public void keyTyped ( KeyEvent event ) {
}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user canceled.
*/
public boolean ok () {
	return __ok;
}

/**
Populate the data type choices in response to a new datastore being selected.
This should match the main TSTool interface.
@param datastore the selected datastore
@param dataType the initial command parameter
*/
private void populateDataTypeChoices ( CampbellCloudDataStore datastore, String dataType ) {
	String routine = getClass().getSimpleName() + ".populateDataTypeChoices";
	if ( datastore == null ) {
		return;
	}
	if ( Message.isDebugOn ) {
		Message.printStatus ( 2, routine, "Populating data types for datastore=\"" + datastore.getName() + "\" initial data type=" + dataType );
	}
	// Don't include the interval because it comes after the data type selection.
	String selectedInterval = null;
	// Include * so that it can be used with 'Where' choices.
	boolean includeWildcards = true;
    List<String> dataTypes = datastore.getTimeSeriesDataTypeStrings ( selectedInterval, includeWildcards );
    if ( (dataType != null) && dataType.contains("${") ) { // } - so editor matches correctly
    	// Initial command was a property so add.
    	if ( dataTypes.size() == 0 ) {
    		dataTypes.add(dataType);
    	}
    	else {
    		dataTypes.add(0, dataType);
    	}
    }
    __DataType_JComboBox.setData ( dataTypes );
    // Select the default:
    // - will either be the first in the list (wildcard) or the 'commandParameter'
    // - select null first to force the event to happen in any case
    if ( __DataType_JComboBox.getItemCount() == 0 ) {
    	// For some reason there were no data types so add an empty string and select to ensure that events chain.
    	__DataType_JComboBox.add("");
    }
    if ( (dataType != null) && !dataType.isEmpty() && dataType.contains("${") ) { // } - so editor matches correctly
    	// Select what the initial command parameter had.
    	if ( Message.isDebugOn ) {
		   	Message.printStatus ( 2, routine, "Selecting initial data type: " + dataType );
	   	}
    	__DataType_JComboBox.select(null);
    	__DataType_JComboBox.select(dataType);
    }
    else {
    	if ( Message.isDebugOn ) {
		   	Message.printStatus ( 2, routine, "Selecting data type [0]." );
	   	}
    	__DataType_JComboBox.select(null);
   		__DataType_JComboBox.select(0);
    }
}

/**
Populate the data interval choices in response to a new data type being selected.
This code matches the TSTool main interface code.
*/
private void populateIntervalChoices ( CampbellCloudDataStore datastore ) {
	String routine = getClass().getSimpleName() + ".populateIntervalChoices";
	String selectedDataType = getSelectedDataType();
	if ( (selectedDataType != null) && selectedDataType.startsWith("${") ) { // } - to allow editor to match
		// Data type is a property so don't use to filter intervals.
		selectedDataType = null;
		if ( Message.isDebugOn ) {
			Message.printStatus ( 2, routine, "Setting data type to null for catalog lookup since a property.");
		}
	}
    Message.printStatus ( 2, routine, "Populating intervals for selected data type \"" + selectedDataType + "\"" );
	List<String> dataIntervals = null;
	if ( datastore == null ) {
		dataIntervals = new ArrayList<>();
	}
	else {
		//boolean includeWildcards = false;
		boolean includeWildcards = true;
		dataIntervals = datastore.getTimeSeriesDataIntervalStrings(selectedDataType, includeWildcards);
	}
    __Interval_JComboBox.setData ( dataIntervals );
    // Select the first item.
    try {
        __Interval_JComboBox.select ( null ); // To force event.
        __Interval_JComboBox.select ( 0 );
    }
    catch ( Exception e ) {
        // Cases when for some reason no choice is available.
        __Interval_JComboBox.add ( "" );
        __Interval_JComboBox.select ( 0 );
    }
}

/**
Set the location ID choices in response to a new datastore being selected.
The location choices are also in the where filter (for multiple time series)
but a single location is needed when reading a single time series.
@param datastore the datastore to use to determine the data types
@param stationId the station identifier for the command, will be added if it contains ${ indicating a property
*/
private void populateStationIdChoices ( CampbellCloudDataStore datastore, String stationId ) {
	String routine = getClass().getSimpleName() + ".populateStationIdChoices";
	if ( datastore == null ) {
		return;
	}

	// Because editing is in discovery mode, expand the parameters that allow properties.

	// Get the cached stations:
	// - should be sorted by name
    String selectedDataType = getSelectedDataType();
    String dataType = TSCommandProcessorUtil.expandParameterDiscoveryValue(this.__propList, this.__command, selectedDataType);
    boolean doDataType = false;
    if ( (dataType != null) && !dataType.isEmpty() && !dataType.equals("*") ) {
    	doDataType = true;
    }
    String interval = getSelectedInterval();
    boolean doInterval = false;
    if ( (interval != null) && !interval.isEmpty() && !interval.equals("*") ) {
    	doInterval = true;
    }
    // List of station IDs:
    // - formatted as "StationID = StationName"
   	// - only include stations that match the data type and interval
    // - later need to add the input filter
    List<String> stationIds = new ArrayList<>();
    boolean doInclude = true;
    List<Station> stationList = datastore.getStationList();
    // Get the list.
   	if ( doDataType || doInterval) {
    	List<TimeSeriesCatalog> tscatalogList = datastore.readTimeSeriesCatalog(dataType, interval, getVisibleInputFilterPanel());
    	for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
    		String tscatalogStationId = tscatalog.getStationId();
    		// Only add to the station list if not already added.
    		boolean found = false;
    		for ( Station station : stationList ) {
    			if ( station.getId().equals(tscatalogStationId) ) {
    				found = true;
    				break;
    			}
    		}
    		if ( !found ) {
    			Station station = Station.findStationForStationId(datastore.getStationList(), tscatalogStationId);
    			stationList.add(station);
    		}
    	}
    	// Sort by station name.
    	Collections.sort(stationList, new StationComparator() );
    }
   	else {
   		stationList = datastore.getStationList();
   	}
    for ( Station station: stationList ) {
    	// Include unless a filter indicates to not include.
    	if ( doInclude ) {
    		stationIds.add(station.getId() + " - " + station.getMetadata().getName() );
    	}
    }
   	__StationIdNote_JTextField.setText("For data type " + dataType + " and interval " + interval + ", have " + stationIds.size() + " stations.");
    // Add a blank because multiple time series tab might be used.
    stationIds.add(0,"");
    if ( (stationId != null) && stationId.contains("${") ) { // } - match for text editor
    	// If a property has been specified, add it because it won't be in the tscatalog list.
    	stationIds.add(1, stationId);
    }
    __StationId_JComboBox.setData ( stationIds );
    if ( (stationId != null) && !stationId.isEmpty() && stationId.contains("${") ) { // } - match for text editor
    	// Select what the initial command parameter had.
    	if ( Message.isDebugOn ) {
		   	Message.printStatus ( 2, routine, "Selecting initial locId: " + stationId );
	   	}
    	__StationId_JComboBox.select(null);
    	__StationId_JComboBox.select(stationId);
    }
    else {
    	// Select the default:
    	// - select null first to force an event to cascade
    	// TODO smalers 2018-06-21 evaluate whether need datastore method for default.
    	__StationId_JComboBox.select(null);
    	__StationId_JComboBox.select(0);
    }
}

/**
Set the timezone choices.
@param timezone the initial command parameter
*/
private void populateTimezoneChoices ( String timezone ) {
	String routine = getClass().getSimpleName() + ".populateTimezoneChoices";
	if ( Message.isDebugOn ) {
		Message.printStatus ( 2, routine, "Populating timezone, initial timezone=" + timezone );
	}
    List<String> timezones = new ArrayList<>();
    Set<String> zoneIds = ZoneId.getAvailableZoneIds();
    for ( String zoneId : zoneIds ) {
    	timezones.add(zoneId);
    }
    Collections.sort(timezones,String.CASE_INSENSITIVE_ORDER);
    timezones.add(0, "");
    // Add the local computer and UTC at the top (afer sorting) to streamline editing.
	timezones.add(1, ZoneId.systemDefault().toString());
    timezones.add(2, "UTC");
    if ( (timezone != null) && timezone.contains("${") ) { // } - so editor matches correctly
    	// Initial command was a property so add.
    	if ( timezones.size() == 0 ) {
    		timezones.add(timezone);
    	}
    	else {
    		timezones.add(0, timezone);
    	}
    }
    __Timezone_JComboBox.setData ( timezones );
    // Select the default:
    // - will either be the first in the list (wildcard) or the 'commandParameter'
    // - select null first to force the event to happen in any case
    if ( __Timezone_JComboBox.getItemCount() == 0 ) {
    	// There were no group names so add an empty string and select to ensure that events chain.
    	__Timezone_JComboBox.add("");
    }
    if ( (timezone != null) && !timezone.isEmpty() && timezone.contains("${") ) { // } - so editor matches correctly
    	// Select what the initial command parameter had.
    	if ( Message.isDebugOn ) {
		   	Message.printStatus ( 2, routine, "Selecting initial timezone: " + timezone );
	   	}
    	__Timezone_JComboBox.select(null);
    	__Timezone_JComboBox.select(timezone);
    }
    else {
    	if ( Message.isDebugOn ) {
		   	Message.printStatus ( 2, routine, "Selecting timezone [0]." );
	   	}
    	__Timezone_JComboBox.select(null);
   		__Timezone_JComboBox.select(0);
    }
}

/**
Refresh the command string from the dialog contents.
*/
private void refresh () {
	String routine = getClass().getSimpleName() + ".refresh";
	__error_wait = false;
	// General (top).
	String DataStore = "";
	String DataType = "";
	String Interval = "";
	// Single.
	String StationId = "";
	String TSID = "";
	// Multiple.
	String filterDelim = ";";
	String ReadTimeSeries = "";
	String TimeSeriesCatalogTableID = "";
	// General (bottom).
	String Alias = "";
	String InputStart = "";
	String InputEnd = "";
	String ReadData = "";
	String IncludeNullData = "";
	String TimePrecision = "";
	String Timezone = "";
	String Units = "";
	String Timeout = "";
	String Debug = "";
	PropList props = null;
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command.
		props = __command.getCommandParameters();
		// General (top).
	    DataStore = props.getValue ( "DataStore" );
	    DataType = props.getValue ( "DataType" );
	    Interval = props.getValue ( "Interval" );
	    // Single.
	    StationId = props.getValue ( "StationId" );
	    TSID = props.getValue ( "TSID" );
	    // Multiple.
	    ReadTimeSeries = props.getValue ( "ReadTimeSeries" );
	    TimeSeriesCatalogTableID = props.getValue ( "TimeSeriesCatalogTableID" );
	    // General (bottom).
		Alias = props.getValue ( "Alias" );
		InputStart = props.getValue ( "InputStart" );
		InputEnd = props.getValue ( "InputEnd" );
		ReadData = props.getValue ( "ReadData" );
		IncludeNullData = props.getValue ( "IncludeNullData" );
		TimePrecision = props.getValue ( "TimePrecision" );
		Timezone = props.getValue ( "Timezone" );
		Units = props.getValue ( "Units" );
		Timeout = props.getValue ( "Timeout" );
		Debug = props.getValue ( "Debug" );

		// Get initial command parameter values, which will be shown in lists in addition to other values:
		// - needed to handle ${Property} in parameter values
        //this.dataStoreInitial = this.__command.getCommandParameters().getValue("DataStore");
        this.dataTypeInitial = this.__command.getCommandParameters().getValue("DataType");
        //this.intervalInitial = this.__command.getCommandParameters().getValue("Interval");
        this.stationIdInitial = this.__command.getCommandParameters().getValue("StationId");
        this.timezoneInitial = this.__command.getCommandParameters().getValue("Timezone");
		
		// General (top).
        // The data store list is set up in initialize() but is selected here.
        if ( JGUIUtil.isSimpleJComboBoxItem(__DataStore_JComboBox, DataStore, JGUIUtil.NONE, null, null ) ) {
            __DataStore_JComboBox.select ( null ); // To ensure that following causes an event.
            __DataStore_JComboBox.select ( DataStore ); // This will trigger getting the DMI for use in the editor.
        }
        else {
            if ( (DataStore == null) || DataStore.equals("") ) {
                // New command...select the default.
                __DataStore_JComboBox.select ( null ); // To ensure that following causes an event.
                if ( __DataStore_JComboBox.getItemCount() > 0 ) {
                	__DataStore_JComboBox.select ( 0 );
                }
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "DataStore parameter \"" + DataStore + "\".  Select a\ndifferent value or Cancel." );
            }
        }

        // Also need to make sure that the input type and DMI are actually selected.
        // Call manually because events are disabled at startup to allow cascade to work properly.
        setDataStoreForSelectedInput();

        // First populate the data type choices based on the datastore that is selected.
        populateDataTypeChoices(getSelectedDataStore(), this.dataTypeInitial );
        // Then set to the value from the command.
        int [] index = new int[1];
        //Message.printStatus(2,routine,"Checking to see if DataType=\"" + DataType + "\" is a choice.");
        //if ( JGUIUtil.isSimpleJComboBoxItem(__DataType_JComboBox, DataType, JGUIUtil.CHECK_SUBSTRINGS, "-", 0, index, true ) ) { // }
	    if ( JGUIUtil.isSimpleJComboBoxItem( __DataType_JComboBox, DataType, JGUIUtil.NONE, null, null ) ) {
            // Existing command so select the matching choice.
	    	if ( Message.isDebugOn ) {
	    		Message.printStatus(2,routine,"DataType=\"" + DataType + "\" was a choice, selecting " + DataType + ".");
	    	}
            __DataType_JComboBox.select(DataType);
        }
        else if ( JGUIUtil.isSimpleJComboBoxItem(__DataType_JComboBox, DataType, JGUIUtil.CHECK_SUBSTRINGS, "seq: -", 0, index, true ) ) {
            // Existing command so select the matching choice (first token before "-").
	    	if ( Message.isDebugOn ) {
	    		Message.printStatus(2,routine,"DataType=\"" + DataType + "\" was a choice (with notes), selecting index " + index[0] + ".");
	    	}
            __DataType_JComboBox.select(index[0]);
        }
        else {
        	if ( Message.isDebugOn ) {
        		Message.printStatus(2,routine,"DataType=\"" + DataType + "\" is not a choice - selecting item [0].");
        	}
            if ( (DataType == null) || DataType.equals("") ) {
                // New command.  Select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
                if ( __DataType_JComboBox.getItemCount() > 0 ) {
                	__DataType_JComboBox.select(0);
                }
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "DataType parameter \"" + DataType + "\".  Select a\ndifferent value or Cancel." );
                if ( __DataType_JComboBox.getItemCount() > 0 ) {
                	__DataType_JComboBox.select(0);
                }
            }
        }

        // Populate the interval choices based on the selected data type.
        populateIntervalChoices(getSelectedDataStore());
        // Now select what the command had previously (if specified).
        //if ( JGUIUtil.isSimpleJComboBoxItem(__Interval_JComboBox, Interval, JGUIUtil.CHECK_SUBSTRINGS, "-", 1, index, true ) ) {
	    if ( JGUIUtil.isSimpleJComboBoxItem( __Interval_JComboBox, Interval, JGUIUtil.NONE, null, null ) ) {
            //__Interval_JComboBox.select (index[0] );
            __Interval_JComboBox.select (Interval);
        }
        else {
            Message.printStatus(2,routine,"Interval=\"" + Interval + "\" is not a choice.");
            if ( (Interval == null) || Interval.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__Interval_JComboBox.select (0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "Interval parameter \"" + Interval + "\".  Select a\ndifferent value or Cancel." );
            	__Interval_JComboBox.select (0);
            }
        }
	    
	    // Single.
	    
	    // Populate the location ID choices.  This will include ${Property} location that will work in discovery mode.
	    populateStationIdChoices(getDataStore(), this.stationIdInitial);
        //int [] index = new int[1];
	    if ( JGUIUtil.isSimpleJComboBoxItem( __StationId_JComboBox, StationId, JGUIUtil.NONE, null, null ) ) {
            // Existing command so select the matching choice.
            //Message.printStatus(2,routine,"StationId=\"" + StationId + "\" was a choice, selecting index " + index[0] + "...");
            //__StationId_JComboBox.select(index[0]);
            //Message.printStatus(2,routine,"Selecting StationId=\"" + StationId + "\" because exact match.");
            __StationId_JComboBox.select(StationId);
        }
        else if ( JGUIUtil.isSimpleJComboBoxItem(__StationId_JComboBox, StationId, JGUIUtil.CHECK_SUBSTRINGS, "seq: -", 0, index, true ) ) {
            // Existing command so select the matching choice (first token before "-").
            //Message.printStatus(2,routine,"StationId=\"" + StationId + "\" was a choice, selecting index " + index[0] + "...");
            //__StationId_JComboBox.select(index[0]);
            //Message.printStatus(2,routine,"Selecting StationId=\"" + StationId + "\" because first token.");
            __StationId_JComboBox.select(index[0]);
        }
        else {
            Message.printStatus(2,routine,"StationId=\"" + StationId + "\" is not a choice and does not use ${Property}.");
            if ( (StationId == null) || StationId.equals("") ) {
                // New command.  Select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
                if ( __StationId_JComboBox.getItemCount() > 0 ) {
                	__StationId_JComboBox.select(0);
                }
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "StationId parameter \"" + StationId + "\".  Select a\ndifferent value or Cancel." );
                if ( __StationId_JComboBox.getItemCount() > 0 ) {
                	__StationId_JComboBox.select(0);
                }
            }
        }
	    if ( TSID != null ) {
		    __TSID_JTextField.setText ( TSID );
	    }

	    // Multiple.
	    
		// Selecting the data type and interval will result in the corresponding filter group being selected.
		selectInputFilter(getDataStore());
		InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
		if ( filterPanel == null ) {
			Message.printWarning(1, routine, "Trouble finding visible input filter panel for selected Campbell Cloud datastore." );
		}
		else {
    		int nfg = filterPanel.getNumFilterGroups();
    		String where;
    		for ( int ifg = 0; ifg < nfg; ifg++ ) {
    			where = props.getValue ( "Where" + (ifg + 1) );
    			if ( (where != null) && (where.length() > 0) ) {
    				// Set the filter.
    				try {
    				    Message.printStatus(2,routine,"Setting filter Where" + (ifg + 1) + "=\"" + where + "\" from panel " + filterPanel );
    				    filterPanel.setInputFilter (ifg, where, filterDelim );
    				}
    				catch ( Exception e ) {
    					Message.printWarning ( 1, routine,
    					"Error setting where information using \"" + where + "\"" );
    					Message.printWarning ( 3, routine, e );
    				}
    				if ( !where.startsWith(";") ) {
    					// Select the tab.
    					__tsInfo_JTabbedPane.setSelectedIndex(1);
    				}
    			}
    		}
		    // For some reason the values do not always show up so invalidate the component to force redraw.
		    // TODO SAM 2016-08-20 This still does not work.
    		Message.printStatus(2,routine,"Revalidating component to force redraw.");
		    filterPanel.revalidate();
		    //filterPanel.repaint();
		}
		if ( (ReadTimeSeries == null) || ReadTimeSeries.isEmpty() ) {
			// Select default.
			__ReadTimeSeries_JComboBox.select ( 0 );
		}
		else {
		    if ( JGUIUtil.isSimpleJComboBoxItem( __ReadTimeSeries_JComboBox,
				ReadTimeSeries, JGUIUtil.NONE, null, null ) ) {
				__ReadTimeSeries_JComboBox.select ( ReadTimeSeries );
				// Select the multiple tab.
			    __tsInfo_JTabbedPane.setSelectedIndex(1);
			}
			else {
			    Message.printWarning ( 1, routine,
				"Existing command references an invalid ReadTimeSeries value \"" +
				ReadTimeSeries + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	    if ( (TimeSeriesCatalogTableID != null) && !TimeSeriesCatalogTableID.isEmpty() ) {
		    __TimeSeriesCatalogTableID_JTextField.setText ( TimeSeriesCatalogTableID );
			// Select the multiple tab.
		    __tsInfo_JTabbedPane.setSelectedIndex(1);
	    }
		
		// General (bottom).
		
	    if ( Alias != null ) {
		    __Alias_JTextField.setText ( Alias );
	    }
		if ( InputStart != null ) {
			__InputStart_JTextField.setText ( InputStart );
		}
		if ( InputEnd != null ) {
			__InputEnd_JTextField.setText ( InputEnd );
		}
		if ( ReadData == null ) {
			// Select default.
			__ReadData_JComboBox.select ( 0 );
		}
		else {
		    if ( JGUIUtil.isSimpleJComboBoxItem( __ReadData_JComboBox,
				ReadData, JGUIUtil.NONE, null, null ) ) {
				__ReadData_JComboBox.select ( ReadData);
			}
			else {
			    Message.printWarning ( 1, routine,
				"Existing command references an invalid ReadData value \"" +
				ReadData + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
		if ( IncludeNullData == null ) {
			// Select default.
			__IncludeNullData_JComboBox.select ( 0 );
		}
		else {
		    if ( JGUIUtil.isSimpleJComboBoxItem( __IncludeNullData_JComboBox,
				IncludeNullData, JGUIUtil.NONE, null, null ) ) {
				__IncludeNullData_JComboBox.select ( IncludeNullData);
			}
			else {
			    Message.printWarning ( 1, routine,
				"Existing command references an invalid IncludeNullData value \"" +
				IncludeNullData + "\".  Select a different value or Cancel.");
				__error_wait = true;
			}
		}
	    if ( JGUIUtil.isSimpleJComboBoxItem( __TimePrecision_JComboBox, TimePrecision, JGUIUtil.NONE, null, null ) ) {
            //__TimePrecision_JComboBox.select (index[0] );
            __TimePrecision_JComboBox.select (TimePrecision);
        }
        else {
            Message.printStatus(2,routine,"TimePrecision=\"" + TimePrecision + "\" is invalid.");
            if ( (TimePrecision == null) || TimePrecision.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__TimePrecision_JComboBox.select (0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "TimePrecision parameter \"" + Interval + "\".  Select a\ndifferent value or Cancel." );
            	__TimePrecision_JComboBox.select (0);
            }
        }
	    if ( Units != null ) {
	    	__Units_JTextField.setText ( Units );
	    }
	    if ( Timeout != null ) {
	    	__Timeout_JTextField.setText ( Timeout );
	    }

        // First populate the group name choices based on the datastore that is selected.
        populateTimezoneChoices(this.timezoneInitial );
        // Then set to the value from the command.
        index = new int[1];
	    if ( JGUIUtil.isSimpleJComboBoxItem( __Timezone_JComboBox, Timezone, JGUIUtil.NONE, null, null ) ) {
            // Existing command so select the matching choice.
	    	if ( Message.isDebugOn ) {
	    		Message.printStatus(2,routine,"Timezone=\"" + Timezone + "\" was a choice, selecting " + Timezone + ".");
	    	}
            __Timezone_JComboBox.select(Timezone);
        }
        else {
        	if ( Message.isDebugOn ) {
        		Message.printStatus(2,routine,"Timezone=\"" + Timezone + "\" is not a choice - selecting item [0].");
        	}
            if ( (Timezone == null) || Timezone.equals("") ) {
                // New command.  Select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
                if ( __Timezone_JComboBox.getItemCount() > 0 ) {
                	__Timezone_JComboBox.select(0);
                }
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "Timezone parameter \"" + Timezone + "\".  Select a\ndifferent value or Cancel." );
                if ( __Timezone_JComboBox.getItemCount() > 0 ) {
                	__Timezone_JComboBox.select(0);
                }
            }
        }

	    if ( JGUIUtil.isSimpleJComboBoxItem( __Debug_JComboBox, Debug, JGUIUtil.NONE, null, null ) ) {
            //__Debug_JComboBox.select (index[0] );
            __Debug_JComboBox.select (Debug);
        }
        else {
            Message.printStatus(2,routine,"Debug=\"" + Debug + "\" is invalid.");
            if ( (Debug == null) || Debug.equals("") ) {
                // New command...select the default.
                // Populating the list above selects the default that is appropriate so no need to do here.
            	__Debug_JComboBox.select (0);
            }
            else {
                // Bad user command.
                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
                  "Debug parameter \"" + Interval + "\".  Select a\ndifferent value or Cancel." );
            	__Debug_JComboBox.select (0);
            }
        }
	}

	// Regardless, reset the command from the fields.

	// General (top).
    DataStore = __DataStore_JComboBox.getSelected();
    if ( DataStore == null ) {
        DataStore = "";
    }
    DataType = getSelectedDataType();
    Interval = getSelectedInterval();

    // Single:
    // - set derived text fields here
	StationId = getSelectedStationId(); // Will strip out the trailing name.
	//StationId = __StationId_JComboBox.getSelected();
	String DataSource = __DataSource_JTextField.getText().trim();
    // Format a TSID to display in the uneditable text field.
	StringBuffer tsidFromParts = new StringBuffer();
	tsidFromParts.append ( StationId );
	tsidFromParts.append ( "." );
	tsidFromParts.append ( DataSource );
	tsidFromParts.append ( "." );
	String dataType = DataType;
	if ( (dataType.indexOf("-") >= 0) || (dataType.indexOf(".") >= 0) ) {
		dataType = "'" + dataType + "'";
	}
	tsidFromParts.append ( dataType );
	tsidFromParts.append ( "." );
	if ( (Interval != null) && !Interval.equals("*") ) {
		tsidFromParts.append ( Interval );
	}
	tsidFromParts.append ( "~" + getInputNameForTSID() );
	__TSIDFromParts_JTextField.setText ( tsidFromParts.toString() );
	TSID = __TSID_JTextField.getText().trim();

    // Multiple.
	ReadTimeSeries = __ReadTimeSeries_JComboBox.getSelected();
	TimeSeriesCatalogTableID = __TimeSeriesCatalogTableID_JTextField.getText().trim();

    // General (bottom).
	Alias = __Alias_JTextField.getText().trim();
	InputStart = __InputStart_JTextField.getText().trim();
	InputEnd = __InputEnd_JTextField.getText().trim();
	ReadData = __ReadData_JComboBox.getSelected();
	IncludeNullData = __IncludeNullData_JComboBox.getSelected();
	TimePrecision = __TimePrecision_JComboBox.getSelected();
	Timezone = __Timezone_JComboBox.getSelected();
	Units = __Units_JTextField.getText().trim();
	Timeout = __Timeout_JTextField.getText().trim();
	Debug = __Debug_JComboBox.getSelected();

	// Set the properties in the command.
	props = new PropList ( __command.getCommandName() );

	// General (top).
    props.add ( "DataStore=" + DataStore );
	if ( (DataType != null) && !DataType.isEmpty() ) {
		props.add ( "DataType=" + DataType );
	}
	if ( (Interval != null) && !Interval.isEmpty() ) {
		props.add ( "Interval=" + Interval );
	}

    // Single.
	if ( (StationId != null) && !StationId.isEmpty() ) {
		props.add ( "StationId=" + StationId );
	}
	if ( (TSID != null) && !TSID.isEmpty() ) {
		props.add ( "TSID=" + TSID );
	}
	
	// Multiple.
	// Set the where clauses.
	// Since numbers may cause problems, first unset and then set.
	InputFilter_JPanel filterPanel = getVisibleInputFilterPanel();
	if ( filterPanel != null ) {
    	int nfg = filterPanel.getNumFilterGroups();
        //Message.printStatus(2,routine,"Input filter panel has " + nfg + " filter groups.");
    	String where;
    	for ( int ifg = 0; ifg < nfg; ifg ++ ) {
    		// Use the internal value for the where to ensure integration.
    		where = filterPanel.toString(ifg,filterDelim,3).trim();
    		// Make sure there is a field that is being checked in a where clause:
    		// - otherwise, unset the where if blank
    		props.unSet("Where" + (ifg + 1) );
    		if ( (where.length() > 0) && !where.startsWith(filterDelim) ) {
                // FIXME SAM 2010-11-01 The following discards '=' in the quoted string.
                //props.add ( "Where" + (ifg + 1) + "=" + where );
                props.set ( "Where" + (ifg + 1), where );
                //Message.printStatus(2,routine,"Setting command parameter from visible input filter:  Where" +
                //    (ifg + 1) + "=\"" + where + "\"" );
    		}
    		else {
                //Message.printStatus(2,routine,"Visible input filter:  Where" + (ifg + 1) + " is set to blank, "
               	//	+ "where=" + where + " where.length()=" + where.length() + " filterDelim=" + filterDelim );
    		}
    	}
	}
	else {
		//Message.printStatus(2, routine, "Visible input filter panel is null.");
	}
	props.add ( "ReadTimeSeries=" + ReadTimeSeries );
	props.add ( "TimeSeriesCatalogTableID=" + TimeSeriesCatalogTableID );
	
	// General (bottom).
	props.add ( "Alias=" + Alias );
	props.add ( "InputStart=" + InputStart );
	props.add ( "InputEnd=" + InputEnd );
	props.add ( "ReadData=" + ReadData );
	props.add ( "IncludeNullData=" + IncludeNullData );
	props.add ( "TimePrecision=" + TimePrecision );
	props.add ( "Timezone=" + Timezone );
	props.add ( "Units=" + Units );
	props.add ( "Timeout=" + Timeout );
	props.add ( "Debug=" + Debug );
	__command_JTextArea.setText( __command.toString ( props ).trim() );

	// Check the GUI state to determine whether some controls should be disabled.

	checkGUIState();
}

/**
React to the user response.
@param ok if false, then the edit is canceled.
If true, the edit is committed and the dialog is closed.
*/
private void response ( boolean ok ) {
	__ok = ok;	// Save to be returned by ok().
	if ( ok ) {
		// Commit the changes.
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close.
			return;
		}
	}
	// Now close out...
	setVisible( false );
	dispose();
}

/**
Select (set visible) the appropriate input filter based on the other data choices.
For Campbell Cloud, there is currently only one input filter per datastore.
@param dataStore the data store from the DataStore and InputName parameters.
*/
private void selectInputFilter ( CampbellCloudDataStore dataStore ) {
	String routine = getClass().getSimpleName() + ".selectInputFilter";
    // Selected datastore name.
    if ( dataStore == null ) {
        return;
    }
    String dataStoreName = dataStore.getName();
    // Selected data type and interval must be converted to Campbell Cloud internal convention.
    // The following lookups are currently hard coded and not read from Campbell Cloud.
    String selectedDataType = getSelectedDataType();
    String selectedTimeStep = __Interval_JComboBox.getSelected();
    //List<InputFilter_JPanel> inputFilterJPanelList = getInputFilterJPanelList();
    List<CampbellCloud_TimeSeries_InputFilter_JPanel> inputFilterJPanelList = getInputFilterJPanelList();
    // Loop through all available input filters and match the data store name, type (whether legacy or new design),
    // and filter for the data type.  If matched, set to visible and otherwise not visible.
    boolean matched;
    int matchCount = 0;
    Message.printStatus(2, routine, "Trying to set visible the input filter given selected datastore name \"" + dataStoreName +
        "\" selectedDataType=\"" + selectedDataType + "\" selectedTimeStep=\"" + selectedTimeStep + "\"" );
    for ( InputFilter_JPanel panel : inputFilterJPanelList ) {
        matched = false; // Does selected datastore name match the filter datastore?
        CampbellCloudDataStore datastore =
            ((CampbellCloud_TimeSeries_InputFilter_JPanel)panel).getDataStore();
        if ( (datastore != null) && datastore.getName().equalsIgnoreCase(dataStoreName) ) {
            // Have a match in the datastore name so return the panel.
            matched = true;
        }
        // If the panel was matched, set it visible.
        panel.setVisible(matched);
        if ( matched ) {
            ++matchCount;
        }
    }
    // No normal panels were matched enable the generic panel, which will be last panel in list.
    InputFilter_JPanel defaultPanel = inputFilterJPanelList.get(inputFilterJPanelList.size() - 1);
    if ( matchCount == 0 ) {
        defaultPanel.setVisible(true);
        Message.printStatus(2, routine, "Setting default input filter panel visible.");
    }
    else {
        defaultPanel.setVisible(false);
    }
}

/**
Set all the filters visible, necessary to help compute layout dimensions and dialog size.
*/
private void setAllFiltersVisible() {
    //List<InputFilter_JPanel> panelList = getInputFilterJPanelList();
    List<CampbellCloud_TimeSeries_InputFilter_JPanel> panelList = getInputFilterJPanelList();
    for ( InputFilter_JPanel panel : panelList ) {
        panel.setVisible(true);
    }
}

/**
Set the datastore to use for queries based on the selected data store and input name.
*/
private void setDataStoreForSelectedInput() {
    // Data store will be used if set.  Otherwise input name is used.
    String dataStoreString = __DataStore_JComboBox.getSelected();
    if ( dataStoreString == null ) {
        dataStoreString = "";
    }
    if ( !dataStoreString.equals("") ) {
        // Use the selected datastore.
        __dataStore = getSelectedDataStore();
    }
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event ) {
	response ( false );
}

public void windowActivated( WindowEvent evt ) {
}

public void windowClosed( WindowEvent evt ) {
}

public void windowDeactivated( WindowEvent evt ) {
}

public void windowDeiconified( WindowEvent evt ) {
}

public void windowIconified( WindowEvent evt ) {
}

public void windowOpened( WindowEvent evt ) {
}

}