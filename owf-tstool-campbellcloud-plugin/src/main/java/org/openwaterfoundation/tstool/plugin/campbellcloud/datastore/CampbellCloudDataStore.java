// CampbellCloudDataStore - class that implements the CampbellCloudDataStore plugin datastore

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
    along with OWF TSTool CampbellCloud Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.campbellcloud.datastore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openwaterfoundation.tstool.plugin.campbellcloud.PluginMeta;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.Asset;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.AssetMetadata;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.Datastream;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.DatastreamDatapoint;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.DatastreamDatapointData;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.DatastreamMetadata;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.Location;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.MeasurementClassificationType;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.Organization;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.Station;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.StationComparator;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.StationMetadata;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.Token;
import org.openwaterfoundation.tstool.plugin.campbellcloud.dao.Variable;
import org.openwaterfoundation.tstool.plugin.campbellcloud.ui.CampbellCloud_TimeSeries_CellRenderer;
import org.openwaterfoundation.tstool.plugin.campbellcloud.ui.CampbellCloud_TimeSeries_InputFilter_JPanel;
import org.openwaterfoundation.tstool.plugin.campbellcloud.ui.CampbellCloud_TimeSeries_TableModel;
import org.openwaterfoundation.tstool.plugin.campbellcloud.util.TimeUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.PropList;
import RTi.Util.IO.RequirementCheck;
import RTi.Util.IO.UrlReader;
import RTi.Util.IO.UrlResponse;
import RTi.Util.Message.Message;
import RTi.Util.String.MultiKeyStringDictionary;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import riverside.datastore.AbstractWebServiceDataStore;
import riverside.datastore.DataStoreRequirementChecker;
import riverside.datastore.PluginDataStore;

public class CampbellCloudDataStore extends AbstractWebServiceDataStore implements DataStoreRequirementChecker, PluginDataStore {

	/**
	 * Standard request parameters:
	 * - currently nothing
	 */
	//private final String COMMON_REQUEST_PARAMETERS = "?something=something"
	//private final String COMMON_REQUEST_PARAMETERS = "";

	/**
	 * The data source to use for time series.
	 */
	private final String TS_DATA_SOURCE = "CampbellCloud";

	/**
	 * URL for getting the API token:
	 * - see the createAuthenticationToken() method
	 */
	//private static String TOKENS_URL = "https://iot.campbell-cloud.com/api/v1/tokens";

	/**
	 * The organization ID from the configuration file, used for authentication.
	 */
	private String organizationId = "UNKNOWN";

	/**
	 * Properties for the plugin, used to help with application integration.
	 */
	private Map<String,Object> pluginProperties = new LinkedHashMap<>();

	/**
	 * Asset list.
	 */
	List<Asset> assetList = new ArrayList<>();

	/**
	 * Datastream list.
	 */
	List<Datastream> datastreamList = new ArrayList<>();

	/**
	 * Measurement classification type list.
	 */
	List<MeasurementClassificationType> measurementClassificationTypeList = new ArrayList<>();

	/**
	 * Global organization list.
	 */
	List<Organization> organizationList = new ArrayList<>();

	/**
	 * Global station list.
	 */
	List<Station> stationList = new ArrayList<>();

	/**
	 * Global time series catalog, used to streamline creating lists for UI choices.
	 */
	private List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();

	/**
	 * Global variable list.
	 */
	List<Variable> variableList = new ArrayList<>();

	/**
	 * Authentication token.
	 */
	private Token token = null;

	/**
	 * Global debug option for datastore, used for development and troubleshooting.
	 */
	private boolean debug = false;

	/**
	Constructor for web service.
	@param name identifier for the data store (will be used in commands)
	@param description name for the data store
	@param serviceRootUrl the service root URL to which specific requests will be appended, must have a trailing slash
	@param props properties to configure the datastore:
	<ul>
	<li> `Description` - description, longer than name</li>
	<li> `Enabled` - standard datastore property, indicated whether it is enabled</li>
	<li> `Name` - name of the datastore, same as name</li>
	<li> `OrganizationId` - the Campbell Cloud organization ID</li>
	<li> `Password` - Campbell Cloud account password</li>
	<li> `ServiceApiDocumentationUrl` - URL for the API documentation landing page</li>
	<li> `ServiceRootUrl` - the URL for the web service API, for example "ServiceRootUrl = "https://iot.campbell-cloud.com/api/v1/"</li>
	<li> `Type` - must be `CampbellCloudDataStore`</li>
	<li> `UserName` - Campbell Cloud account user name</li>
	</ul>
	*/
	public CampbellCloudDataStore ( String name, String description, URI serviceRootUrl, PropList props ) {
		String routine = getClass().getSimpleName() + ".CampbellCloudDataStore";

		String prop = props.getValue("Debug");
		if ( (prop != null) && prop.equalsIgnoreCase("true") ) {
			Message.printStatus(2, routine, "Datastore \"" + name + "\" - detected Debug=true");
			this.debug = true;
		}
	    setName ( name );
	    setDescription ( description );
	    setServiceRootURI ( serviceRootUrl );
	    setProperties ( props );
		prop = props.getValue("OrganizationId");
		if ( (prop != null) && !prop.isEmpty() ) {
			this.organizationId = prop;
		}

	    // Set standard plugin properties:
        // - plugin properties can be listed in the main TSTool interface
        // - version is used to create a versioned installer and documentation.
        this.pluginProperties.put("Name", "Open Water Foundation Campbell Cloud web services plugin");
        this.pluginProperties.put("Description", "Plugin to integrate TSTool with Campbell Cloud web services.");
        this.pluginProperties.put("Author", "Open Water Foundation, https://openwaterfoundation.org");
        this.pluginProperties.put("Version", PluginMeta.VERSION);

        // Create the authentication token that will be used for the session.
        createAuthenticationToken ();

	    // Read global data used throughout the session:
	    // - in particular a cache of the TimeSeriesCatalog used for further queries

	    readGlobalData();
	}

	/**
 	* Check the database requirement for DataStoreRequirementChecker interface, for example one of:
 	* <pre>
 	* @require datastore CampbellCloud-WET version >= 1.5.5
 	* @require datastore CampbellCloud-WET ?configproperty propname? == Something
 	* @require datastore CampbellCloud-WET configuration system_id == CO-District-MHFD
 	*
 	* @enabledif datastore nsdataws-mhfd version >= 1.5.5
 	* </pre>
 	* @param check a RequirementCheck object that has been initialized with the check text and
 	* will be updated in this method.
 	* @return whether the requirement condition is met, from call to check.isRequirementMet()
 	*/
	public boolean checkRequirement ( RequirementCheck check ) {
		String routine = getClass().getSimpleName() + ".checkRequirement";
		// Parse the string into parts:
		// - calling code has already interpreted the first 3 parts to be able to do this call
		String requirement = check.getRequirementText();
		Message.printStatus(2, routine, "Checking requirement: " + requirement);
		// Get the annotation that is being checked, so messages are appropriate.
		String annotation = check.getAnnotation();
		String [] requireParts = requirement.split(" ");
		// Datastore name may be an original name but a substitute is used, via TSTool command line.
		String dsName = requireParts[2];
		String dsNameNote = ""; // Note to add on messages to help confirm how substitutions are being handled.
		String checkerName = "CloudFrontDataStore";
		if ( !dsName.equals(this.getName())) {
			// A substitute datastore name is being used, such as in testing.
			dsNameNote = "\nCommand file datastore name '" + dsName + "' substitute that is actually used is '" + this.getName() + "'";
		}
		if ( requireParts.length < 4 ) {
			check.setIsRequirementMet(checkerName, false, "Requirement does not contain check type as one of: version, configuration, "
				+ "for example: " + annotation + " datastore nsdataws-mhfd version...");
			return check.isRequirementMet();
		}
		String checkType = requireParts[3];
		if ( checkType.equalsIgnoreCase("configuration") ) {
			// Checking requirement of form:
			// 0        1         2             3             4         5  6
			// @require datastore nsdataws-mhfd configuration system_id == CO-District-MHFD
			String propertyName = requireParts[4];
			String operator = requireParts[5];
			String checkValue = requireParts[6];
			// Get the configuration table property of interest:
			// - currently only support checking system_id
			if ( propertyName.equals("system_id") ) {
				// Know how to handle "system_id" property.
				if ( (checkValue == null) || checkValue.isEmpty() ) {
					// Unable to do check.
					check.setIsRequirementMet ( checkerName, false, "'system_id' value to check is not specified in the requirement." + dsNameNote );
					return check.isRequirementMet();
				}
				else {
					// TODO smalers 2023-01-03 need to evaluate whether Campbell Cloud has configuration properties.
					//String propertyValue = readConfigurationProperty(propertyName);
					String propertyValue = "";
					if ( (propertyValue == null) || propertyValue.isEmpty() ) {
						// Unable to do check.
						check.setIsRequirementMet ( checkerName, false, "Campbell CLoud configuration 'system_id' value is not defined in the database." + dsNameNote );
						return check.isRequirementMet();
					}
					else {
						if ( StringUtil.compareUsingOperator(propertyValue, operator, checkValue) ) {
							check.setIsRequirementMet ( checkerName, true, "Campbell CLoud configuration property '" + propertyName + "' value (" + propertyValue +
								") does meet the requirement: " + operator + " " + checkValue + dsNameNote );
						}
						else {
							check.setIsRequirementMet ( checkerName, false, "Campbell CLoud configuration property '" + propertyName + "' value (" + propertyValue +
								") does not meet the requirement:" + operator + " " + checkValue + dsNameNote );
						}
						return check.isRequirementMet();
					}
				}
			}
			else {
				// Other properties may not be easy to compare.  Probably need to use "contains" and other operators.
				check.setIsRequirementMet ( checkerName, false, "Check type '" + checkType + "' configuration property '" + propertyName + "' is not supported.");
				return check.isRequirementMet();
			}
		}
		/* TODO smalers 2021-07-29 need to implement, maybe need to define the system ID in the configuration file as a cross check for testing.
		else if ( checkType.equalsIgnoreCase("configproperty") ) {
			if ( parts.length < 7 ) {
				// 'property' requires 7 parts
				throw new RuntimeException( "'configproperty' requirement does not contain at least 7 parts for: " + requirement);
			}
		}
		*/
		else if ( checkType.equalsIgnoreCase("version") ) {
			// Checking requirement of form:
			// 0        1         2             3       4  5
			// @require datastore nsdataws-mhfd version >= 1.5.5
			Message.printStatus(2, routine, "Checking web service version.");
			// Do a web service round trip to check version since it may change with software updates.
			String wsVersion = readVersion();
			if ( (wsVersion == null) || wsVersion.isEmpty() ) {
				// Unable to do check.
				check.setIsRequirementMet ( checkerName, false, "Web service version is unknown (services are down or software problem).");
				return check.isRequirementMet();
			}
			else {
				// Web service versions are strings of format A.B.C.D so can do semantic version comparison:
				// - only compare the first 3 parts
				//Message.printStatus(2, "checkRequirement", "Comparing " + wsVersion + " " + operator + " " + checkValue);
				String operator = requireParts[4];
				String checkValue = requireParts[5];
				boolean verCheck = StringUtil.compareSemanticVersions(wsVersion, operator, checkValue, 3);
				String message = "";
				if ( !verCheck ) {
					message = annotation + " web service version (" + wsVersion + ") does not meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				else {
					message = annotation + " web service version (" + wsVersion + ") does meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				return check.isRequirementMet();
			}
		}
		else {
			// Unknown check type.
			check.setIsRequirementMet ( checkerName, false, "Requirement check type '" + checkType + "' is unknown.");
			return check.isRequirementMet();
		}

	}

	/**
	 * Check whether the authentication token is expired if if so, refresh the token.
	 */
	private void checkTokenExpiration() {
		if ( this.token.isExpired() ) {
			// Create a new token
			createAuthenticationToken();
		}
	}

	/**
	 * Create the authentication token:
	 * - see: https://docs.campbellcloud.io/api/
	 * - currently a new token is created (don't refresh an expired token)
	 */
    private void createAuthenticationToken () {
    	String routine = getClass().getSimpleName() + ".createAuthenticationToken";
    	HttpURLConnection urlConnection = null;
   		URL url = null;
    	try {
    		//private static String TOKENS_URL = "https://iot.campbell-cloud.com/api/v1/tokens";
    		String urlString = getServiceRootURI() + "tokens";
    		//String urlString = TOKENS_URL;
    		//url = new URL(TOKENS_URL);
    		url = new URL(urlString);
    	}
    	catch ( MalformedURLException e ) {
    		// This should not happen because it is a basic URL.
    		Message.printWarning(3,routine,"The Campbell Cloud tokens URL is invalid.");
    		return;
    	}
    	try {
    		urlConnection = (HttpURLConnection)url.openConnection();
    		urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Content-Type", "application/json");
    		urlConnection.setDoOutput(true);

    		// Call the 'connect' method to explicitly fire the request.
    		// Check the response code immediately in case it was a redirect.
   			urlConnection.connect();

   			String userName = getProperty("UserName");
   			if ( (userName == null) || userName.isEmpty() ) {
   				userName = "UNKNOWN";
   			}
   			String password = getProperty("Password");
   			if ( (password == null) || password.isEmpty() ) {
   				password = "UNKNOWN";
   			}
   			String payload = "{"
				+ "\"username\" : \"" + userName + "\", "
				+ "\"password\" : \"" + password + "\", "
   				+ "\"client_id\" : \"cloud\", "
   				+ "\"grant_type\": \"password\""
   				+ " }";

   			// Only use this for development.
   			Message.printStatus(2,routine,"Payload: " + payload);

   			// Set the payload for the POST.
  			OutputStreamWriter outputStream = new OutputStreamWriter(urlConnection.getOutputStream());
    		outputStream.write(payload);
    		outputStream.flush();
    		outputStream.close();

   			// Get the response code:
   			// - if 3xx, loop again to follow the redirect
   			int responseCode = urlConnection.getResponseCode();
   			if ( (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) ||
				(responseCode == HttpURLConnection.HTTP_MOVED_PERM) ||
				(responseCode == HttpURLConnection.HTTP_SEE_OTHER) ) {
   				// Redirect.
   				Message.printWarning(3,routine,"Don't know how to handle URL redirect for token request.");
   			}
   			else {
   				// Process the output.
   				InputStream stream = (responseCode >= 400) ? urlConnection.getErrorStream() : urlConnection.getInputStream();

   				if (stream != null) {
   			    	try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"))) {
   			        	StringBuilder response = new StringBuilder();
   			        	String line;
   			        	while ((line = br.readLine()) != null) {
   			            	response.append(line.trim());
   			        	}
   			        	// Uncomment the following for troubleshooting.
   			        	//Message.printStatus ( 2, routine, "Response: " + response );

   			        	// Convert the response JSON to an object.
   			        	ObjectMapper mapper = new ObjectMapper();
   			        	this.token = mapper.readValue(response.toString(), Token.class);
   			    	}
   				}
   			}
    	}
    	catch ( IOException e ) {
    		Message.printWarning(3,routine,"Requesting a Campbell Cloud token generated an exception.");
    		Message.printWarning(3,routine,e);
    		return;
    	}
    }

	/**
	 * Create a time series input filter, used to initialize user interfaces.
	 * @return a time series input filter for Campbell Cloud time series catalog queries
	 */
	public InputFilter_JPanel createTimeSeriesListInputFilterPanel () {
		CampbellCloud_TimeSeries_InputFilter_JPanel ifp = new CampbellCloud_TimeSeries_InputFilter_JPanel(this, 4);
		return ifp;
	}

	/**
	 * Create a time series list table model given the desired data type, time step (interval), and input filter.
	 * The datastore performs a suitable query and creates objects to manage in the time series list.
	 * @param dataType time series data type to query, controlled by the datastore
	 * @param timeStep time interval to query, controlled by the datastore
	 * @param ifp input filter panel that provides additional filter options
	 * @return a TableModel containing the defined columns and rows.
	 */
	@SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel createTimeSeriesListTableModel(String dataType, String timeStep, InputFilter_JPanel ifp ) {
		// First query the database for the specified input.
		List<TimeSeriesCatalog> tsmetaList = readTimeSeriesMeta ( dataType, timeStep, ifp );
		return getTimeSeriesListTableModel(tsmetaList);
	}

	/**
	 * Determine the read end based on the requested readEnd:
	 * - default the time zone to the computer time if not specified.
	 * @param readEnd original read end from global input period or command parameter
	 * @return the read end as requested, with requested time zone
	 */
	private DateTime determineReadEndRequested ( DateTime readEnd ) {
		String routine = getClass().getSimpleName() + ".determineReadEndRequested";
		DateTime readEndRequested = null;
		if ( readEnd == null ) {
			// Read end was not specified:
			// - use the current time, round forward to 5 minutes
			readEndRequested = new DateTime ( DateTime.DATE_CURRENT );
			// The timezone from the computer is used.
			// Round forward to five minutes.
			readEndRequested.round(1, TimeInterval.MINUTE, 5);
			Message.printStatus(2, routine, "Using default read end (current time rounded forward to 5 minutes), with computer timezone: " + readEndRequested );
		}
		else {
			// Read end was specified:
			// - make sure that the time zone is set in a copy of the DateTime
			readEndRequested = new DateTime(readEnd);
			if ( (readEndRequested.getTimeZoneAbbreviation() == null) || readEndRequested.getTimeZoneAbbreviation().isEmpty() ) {
				// No time zone in the read start so use the computer time zone.
				ZoneId zone = ZoneId.systemDefault();
				readEndRequested.setTimeZone(zone.toString());
				Message.printStatus(2, routine, "Using specified read end with computer timezone: " + readEndRequested );
			}
			else {
				Message.printStatus(2, routine, "Using specified read end with specified timezone: " + readEndRequested );
			}
		}
		return readEndRequested;
	}

	/**
	 * Determine the read end in UTC based on the requested read end.
	 * @param readEndRequested the requested read end, with timezone specified
	 * @return the read end as requested, with UTC time zone
	 */
	private DateTime determineReadEndUTC ( DateTime readEndRequested ) {
		String routine = getClass().getSimpleName() + ".determineReadEndUTC";
		// Create a ZonedDateTime using the values in readEndRequested.
		ZonedDateTime zonedDateTime = readEndRequested.toZonedDateTime(null);
		// Get a LocalDateTime in UTC.
		ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
		// Create a new DateTime with the values from the LocalDateTime and the same precision as the original readEndRequested.
		int behavior = (int)readEndRequested.getBehaviorFlag();
		DateTime readEndUTC = new DateTime ( utcDateTime, behavior, null );
		Message.printStatus(2, routine, "Using specified read end UTC: " + readEndUTC );
		return readEndUTC;
	}
	
	/**
	 * Determine the read start based on the requested readStart:
	 * - default the time zone to the computer time if not specified.
	 * @param readStart original read start from global input period or command parameter
	 * @return the read start as requested, with requested time zone
	 */
	private DateTime determineReadStartRequested ( DateTime readStart ) {
		String routine = getClass().getSimpleName() + ".determineReadStartRequested";
		DateTime readStartRequested = null;
		if ( readStart == null ) {
			// Read start was not specified:
			// - use the current time, subtract 30 days, and round back to 5 minutes
			readStartRequested = new DateTime ( DateTime.DATE_CURRENT );
			// The timezone from the computer is used.
			readStartRequested.addDay(-30);
			// Round down to five minutes.
			readStartRequested.round(-1, TimeInterval.MINUTE, 5);
			Message.printStatus(2, routine, "Using default read start (30 days earlier rounded back to 5 minutes), with computer timezone: " + readStartRequested );
		}
		else {
			// Read start was specified:
			// - make sure that the time zone is set in a copy of the DateTime
			readStartRequested = new DateTime(readStart);
			if ( (readStartRequested.getTimeZoneAbbreviation() == null) || readStartRequested.getTimeZoneAbbreviation().isEmpty() ) {
				// No time zone in the read start so use the computer time zone.
				ZoneId zone = ZoneId.systemDefault();
				readStartRequested.setTimeZone(zone.toString());
				Message.printStatus(2, routine, "Using specified read start with computer timezone: " + readStartRequested );
			}
			else {
				Message.printStatus(2, routine, "Using specified read start with specified timezone: " + readStartRequested );
			}
		}
		return readStartRequested;
	}
	
	/**
	 * Determine the read start in UTC based on the requested read start.
	 * @param readStartRequested the requested read start, with timezone specified
	 * @return the read start as requested, with UTC time zone
	 */
	private DateTime determineReadStartUTC ( DateTime readStartRequested ) {
		String routine = getClass().getSimpleName() + ".determineReadStartUTC";
		// Create a ZonedDateTime using the values in readStartRequested.
		ZonedDateTime zonedDateTime = readStartRequested.toZonedDateTime(null);
		// Get a LocalDateTime in UTC.
		ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
		// Create a new DateTime with the values from the LocalDateTime and the same precision as the original readStartRequested.
		int behavior = (int)readStartRequested.getBehaviorFlag();
		DateTime readStartUTC = new DateTime ( utcDateTime, behavior, null );
		Message.printStatus(2, routine, "Using specified read start UTC: " + readStartUTC );
		return readStartUTC;
	}

	/**
 	* Get the properties for the plugin.
 	* A copy of the properties map is returned so that calling code cannot change the properties for the plugin.
 	* @return plugin properties map.
 	*/
	public Map<String,Object> getPluginProperties () {
		Map<String,Object> pluginProperties = new LinkedHashMap<>();
		// For now the properties are all strings so it is easy to copy.
    	for (Map.Entry<String, Object> entry : this.pluginProperties.entrySet()) {
        	pluginProperties.put(entry.getKey(),
                    	entry.getValue());
    	}
		return pluginProperties;
	}

	/**
	 * Indicate whether the datastore provides a time series input filter.
	 * This datastore does provide an input filter panel.
	 */
	public boolean providesTimeSeriesListInputFilterPanel () {
		return true;
	}

	/**
	 * Return the list of cached Station.
	 * @return the list of cached Station
	 */
	public List<Station> getStationList () {
		return this.stationList;
	}

	/**
	 * Return the list of time series catalog.
	 * @param readData if false, return the global cached data, if true read the data and reset in the cache
	 */
	public List<TimeSeriesCatalog> getTimeSeriesCatalog(boolean readData) {
		if ( readData ) {
			String dataTypeReq = null;
			String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
			this.tscatalogList = readTimeSeriesCatalog ( dataTypeReq, dataIntervalReq, ifp );
		}
		return this.tscatalogList;
	}

	/**
 	* Return the comments for a time series in the table model.
 	* The comments are added as commands prior to the TSID comment.
 	* @param tableModel the table model from which to extract data
 	* @param row the displayed table row, may have been sorted
 	* @return a list of comments (return null or an empty list to not add comments to commands).
 	* A single comment is returned.
 	*/
	public List<String> getTimeSeriesCommentsFromTableModel ( @SuppressWarnings("rawtypes") JWorksheet_AbstractRowTableModel tableModel, int row ) {
    	CampbellCloud_TimeSeries_TableModel tm = (CampbellCloud_TimeSeries_TableModel)tableModel;
    	// Should not have any nulls.
    	List<String> comments = new ArrayList<>();
    	StringBuilder commentBuilder = new StringBuilder();
    	String stationName = (String)tableModel.getValueAt(row,tm.COL_STATION_NAME);
    	String datastreamId = (String)tableModel.getValueAt(row,tm.COL_DATASTREAM_ID);
    	String assetModel = (String)tableModel.getValueAt(row,tm.COL_ASSET_MODEL);
    	String assetDescription = (String)tableModel.getValueAt(row,tm.COL_ASSET_DESCRIPTION);
    	commentBuilder.append(stationName);
    	commentBuilder.append(", ");
    	commentBuilder.append(assetModel);
    	commentBuilder.append(" ");
    	commentBuilder.append(assetDescription);
    	commentBuilder.append(", datastream=");
    	commentBuilder.append(datastreamId);
    	comments.add(commentBuilder.toString() );
    	return comments;
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 */
	public List<String> getTimeSeriesDataIntervalStrings(String dataType) {
		boolean includeWildcards = true;
		return getTimeSeriesDataIntervalStrings(dataType, includeWildcards);
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * Interval strings match TSTool conventions such as NewTimeSeries command, which uses "1Hour" rather than "1hour".
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 * @includeWildcards if true, include "*" wildcard.
	 */
	public List<String> getTimeSeriesDataIntervalStrings(String dataType, boolean includeWildcards ) {
		String routine = getClass().getSimpleName() + ".getTimeSeriesDataIntervalStrings";
		List<String> dataIntervals = new ArrayList<>();
		Message.printStatus(2, routine, "Getting interval strings for data type \"" + dataType + "\"");

		// Only check datatype if not a wildcard.
		boolean doCheckDataType = false;
		if ( (dataType != null) && !dataType.isEmpty() && !dataType.equals("*") ) {
			doCheckDataType = true;
			if ( dataType.contains(" - ") ) {
				// Remove the trailing count:
				//   Datatupe - Count
				int pos = dataType.indexOf(" - ");
				if ( pos > 0 ) {
					dataType = dataType.substring(0,pos);
				}
			}
		}

		// Use the cached time series catalog read at startup.
		List<TimeSeriesCatalog> tscatalogList = getTimeSeriesCatalog(false);
		Message.printStatus(2, routine, "  Have " + tscatalogList.size() + " cached time series from the catalog.");
		for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
			if ( doCheckDataType ) {
				// Only check the first part of the data type, which is the 'stationparameter_no'.
				if ( !dataType.equals(tscatalog.getDataType())) {
					// Data type does not match 'stationparameter_no'.
					continue;
				}
			}
			// Only add the interval if not already in the list.
			if ( !StringUtil.isInList(dataIntervals, tscatalog.getDataInterval())) {
				dataIntervals.add(tscatalog.getDataInterval());
			}
		}

		// Sort the intervals:
		// - TODO smalers need to sort by time
		Collections.sort(dataIntervals,String.CASE_INSENSITIVE_ORDER);

		if ( includeWildcards ) {
			// Always allow querying list of time series for all intervals:
			// - always add so that people can get a full list
			// - adding at top makes it easy to explore data without having to scroll to the end

			dataIntervals.add("*");
			if ( dataIntervals.size() > 1 ) {
				// Also add at the beginning to simplify selections:
				// - could check for a small number like 5 but there should always be a few
				dataIntervals.add(0,"*");
			}
		}

		return dataIntervals;
	}

	/**
	 * Return the list of time series data type strings.
	 * This is the version that is required by TSTool UI.
	 * These strings are the same as the datastream field.
	 * @param dataInterval data interval from TimeInterval.getName(TimeInterval.HOUR,0) to filter the list of data types.
	 * If null, blank, or "*" the interval is not considered when determining the list of data types (treat as if "*").
	 */
	public List<String> getTimeSeriesDataTypeStrings(String dataInterval) {
		boolean includeWildcards = true;
		return getTimeSeriesDataTypeStrings(dataInterval, includeWildcards );
	}

	/**
	 * Return the list of time series data type strings.
	 * These strings are the same as the datastream field.
	 */
	public List<String> getTimeSeriesDataTypeStrings(String dataInterval, boolean includeWildcards ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesDataTypeStrings";
		// Map to count how many time series for each daa type.
		Map<String,Integer> countMap = new HashMap<>();

		List<String> dataTypes = new ArrayList<>();

		// Get the cached list of time series catalog objects.
		List<TimeSeriesCatalog> tscatalogList = getTimeSeriesCatalog(false);

		// Create the data type list:
		// - use the global TimeSeriesCatalog to get the data type.
		boolean found = false;
		if ( tscatalogList != null ) {
			for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
				String tscatalogDataType = tscatalog.getDataType();
				// Update the count.
				Integer count = countMap.get(tscatalogDataType);
				if ( count == null ) {
					count = Integer.valueOf(1);
				}
				else {
					// Increment the count.
					count = Integer.valueOf(count + 1);
				}
				countMap.put(tscatalogDataType, count);
				found = false;
				for ( String dataType : dataTypes ) {
					//if ( stationParameterName.equals(dataType) ) {
					if ( tscatalogDataType.equals(dataType) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					//Message.printStatus(2, routine, "Adding data type \"" + tscatalogDataType + "\"");
					dataTypes.add(tscatalogDataType);
				}
			}
		}

		// Add the count to the data types.
		boolean includeCount = true;
		if ( includeCount ) {
			int i = -1;
			for ( String dataType : dataTypes ) {
				++i;
				Integer count = countMap.get(dataType);
				if ( count == null ) {
					dataType += " - 0";
				}
				else {
					dataType += " - " + count;
				}
				dataTypes.set(i, dataType);
			}
		}

		// Sort the names.
		Collections.sort(dataTypes, String.CASE_INSENSITIVE_ORDER);

		if ( includeWildcards ) {
			// Add wildcard at the front and end - allows querying all data types for the location:
			// - always add so that people can get a full list
			// - adding at the top makes it easy to explore data without having to scroll to the end

			dataTypes.add("*");
			dataTypes.add(0,"*");
		}

		return dataTypes;
	}

	/**
 	* Return the identifier for a time series in the table model.
 	* The TSIdent parts will be uses as TSID commands.
 	* @param tableModel the table model from which to extract data
 	* @param row the displayed table row, may have been sorted
 	* @return a time series identifier that can be used for a TSID command
 	*/
	public TSIdent getTimeSeriesIdentifierFromTableModel( @SuppressWarnings("rawtypes") JWorksheet_AbstractRowTableModel tableModel, int row ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesIdentifierFromTableModel";
    	CampbellCloud_TimeSeries_TableModel tm = (CampbellCloud_TimeSeries_TableModel)tableModel;
    	// Should not have any nulls.
    	//String locId = (String)tableModel.getValueAt(row,tm.COL_LOCATION_ID);
    	String source = (String)tableModel.getValueAt(row,tm.COL_DATA_SOURCE); // Default, may add agency or organization later.
    	String dataType = (String)tableModel.getValueAt(row,tm.COL_DATA_TYPE);
    	String interval = (String)tableModel.getValueAt(row,tm.COL_DATA_INTERVAL);
    	String scenario = "";
    	String inputName = ""; // Only used for files.
    	TSIdent tsid = null;
    	boolean useTsid = false;
		String datastoreName = this.getName();
		String locId = "";
    	if ( useTsid ) {
    		// Use the LocType and ts_id.
   			locId = "ts_id:" + tableModel.getValueAt(row,tm.COL_TSID);
    	}
    	else {
    		// Use the station ID for the location.
   			locId = "" + tableModel.getValueAt(row,tm.COL_STATION_ID);
    	}
    	try {
    		tsid = new TSIdent(locId, source, dataType, interval, scenario, datastoreName, inputName );
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}
    	return tsid;
	}

    /**
     * Get the CellRenderer used for displaying the time series in a TableModel.
     */
    @SuppressWarnings("rawtypes")
	public JWorksheet_AbstractExcelCellRenderer getTimeSeriesListCellRenderer(JWorksheet_AbstractRowTableModel tableModel) {
    	return new CampbellCloud_TimeSeries_CellRenderer ((CampbellCloud_TimeSeries_TableModel)tableModel);
    }

    /**
     * Get the TableModel used for displaying the time series.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public JWorksheet_AbstractRowTableModel getTimeSeriesListTableModel(List<? extends Object> data) {
    	return new CampbellCloud_TimeSeries_TableModel(this,(List<TimeSeriesCatalog>)data);
    }

	/**
 	* Read the asset list objects from the "/assets" service.
 	* The JSON looks like the following:
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
 	* @return the list of Asset objects
 	*/
	private List<Asset> readAssetList() throws IOException {
		String routine = getClass().getSimpleName() + ".readAssetList";
		checkTokenExpiration();
		String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/assets";
		// The following returns:
		// (code=404): {"message":"no Route matched with those values"}
		//String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/list-assets";
		Message.printStatus(2, routine, "Reading asset list from: " + urlString);
		List<Asset> assetList = new ArrayList<>();

		String urlStringEncoded = urlString;
		MultiKeyStringDictionary requestProperties = new MultiKeyStringDictionary();
		requestProperties.add("Authorization", "Bearer " + this.token.getAccessToken());
		if ( this.debug ) {
			//Message.printStatus(2, routine, "Reading asset list using token: " + this.token.getAccessToken());
		}
		int timeoutSeconds = 300;
		UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
		UrlResponse urlResponse = null;
		try {
			urlResponse = urlReader.read();
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading 'assets' using \"" + urlString + "\".");
			Message.printWarning(3, routine, e);
			throw new RuntimeException(e);
		}
		if ( urlResponse.hadError() ) {
			// TODO smalers 2020-06-12 would be nice to not catch this immediately.
			throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
				+ "): " + urlResponse.getResponseError() );
		}
		else if ( urlResponse.getResponseCode() != 200 ) {
			throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
		}
		else {
			// Parse the JSON response into objects.
			ObjectMapper mapper = new ObjectMapper();
			// Not needed.
			//mapper.registerModule(new JavaTimeModule());
			String responseJson = urlResponse.getResponse();
			Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
			try {
				if ( this.debug ) {
					Message.printStatus(2, routine, "Response=" + responseJson);
				}
				assetList = mapper.readValue(responseJson, new TypeReference<List<Asset>>(){});
				Message.printStatus(2, routine, "Read " + assetList.size() + " assets.");
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'assets' using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
		}

		// Sort on the name.
		//Collections.sort(assetList, new AssetComparator());

		// Clean the data.
		Asset.cleanData ( assetList );

		return assetList;
	}

	/**
 	* Read the datastream objects from the "/stations" service.
 	* The JSON looks like the following:
 	* <pre>
 [
  {
    "station_id": "1bfdaf3d-f921-4fcd-88bf-33e8dabd5be2",
    "created_ts": 1695669740067,
    "created_by": "b7f5814f-df9d-43e2-b790-a6209cd84bb8",
    "metadata": {
      "status": "active",
      "aggregate_func": null,
      "table": "ClimaVUE50",
      "alias": "record@climavue50",
      "$version": 1,
      "$profile": "datastream",
      "field": "record"
    },
    "asset_id": "e4256734-c1a3-43b9-a7a1-62f040fd59d6",
    "organization_id": "867e3a14-7614-47a2-a5fa-73e831cfc170",
    "id": "018acdca-e223-7405-a060-b23851d878b3"
  },
  ...
]
 	* </pre>
 	* @return the list of Station objects
 	*/
	private List<Datastream> readDatastreamList() throws IOException {
		String routine = getClass().getSimpleName() + ".readDatastreamList";
		checkTokenExpiration();
		String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/datastreams";
		// The following returns:
		// (code=404): {"message":"no Route matched with those values"}
		//String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/list-datastreams";
		Message.printStatus(2, routine, "Reading datastream list from: " + urlString);
		List<Datastream> datastreamList = new ArrayList<>();

		String urlStringEncoded = urlString;
		MultiKeyStringDictionary requestProperties = new MultiKeyStringDictionary();
		requestProperties.add("Authorization", "Bearer " + this.token.getAccessToken());
		if ( this.debug ) {
			//Message.printStatus(2, routine, "Reading datastream list using token: " + this.token.getAccessToken());
		}
		int timeoutSeconds = 300;
		UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
		UrlResponse urlResponse = null;
		try {
			urlResponse = urlReader.read();
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading 'datastreams' using \"" + urlString + "\".");
			Message.printWarning(3, routine, e);
			throw new RuntimeException(e);
		}
		if ( urlResponse.hadError() ) {
			// TODO smalers 2020-06-12 would be nice to not catch this immediately.
			throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
				+ "): " + urlResponse.getResponseError() );
		}
		else if ( urlResponse.getResponseCode() != 200 ) {
			throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
		}
		else {
			// Parse the JSON response into objects.
			ObjectMapper mapper = new ObjectMapper();
			// Not needed.
			//mapper.registerModule(new JavaTimeModule());
			String responseJson = urlResponse.getResponse();
			Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
			try {
				if ( this.debug ) {
					Message.printStatus(2, routine, "Response=" + responseJson);
				}
				datastreamList = mapper.readValue(responseJson, new TypeReference<List<Datastream>>(){});
				Message.printStatus(2, routine, "Read " + datastreamList.size() + " datastreams.");
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'datastreams' using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
		}

		// Sort on the name.
		//Collections.sort(datastreamList, new DatastreamComparator());
		return datastreamList;
	}

	/**
 	* Read a datastreams data points from the "/datastreams/{datastreamId}/get-datastream-datapoints" service.
 	* The JSON looks like the following:
 	* <pre>
 	* </pre>
 	* @param datastreamId identifier for the data stream
 	* @param readStart start for data reading, can be in any time zone
 	* @param readEnd end for data reading, can be in any time zone
 	* @param timeout timeout in seconds for web service requests
 	* @return the list of Station objects
 	*/
	private DatastreamDatapoint readDatastreamDatapoint (
		String datastreamId,
		DateTime readStart, DateTime readEnd,
		Integer timeout ) throws IOException {
		String routine = getClass().getSimpleName() + ".readDatastreamDatapointList";
		checkTokenExpiration();
		StringBuilder urlString = new StringBuilder(getServiceRootURI().toString());
   		Message.printStatus(2,routine, "Reading datastream data points from " + readStart + " to " + readEnd );
		urlString.append("organizations/");
		urlString.append(organizationId);
		urlString.append("/datastreams/" + datastreamId);
		urlString.append("/datapoints");
		urlString.append("?startEpoch=" + TimeUtil.toEpochMsUTC(readStart) );
		urlString.append("&endEpoch=" + TimeUtil.toEpochMsUTC(readEnd) );
		urlString.append("&limit=15000" ); // Maximum number of points that can be returned.
		Message.printStatus(2, routine, "Reading datastream datapoint list from: " + urlString);
		DatastreamDatapoint datastreamDatapoint = null;

		String urlStringEncoded = urlString.toString();
		MultiKeyStringDictionary requestProperties = new MultiKeyStringDictionary();
		requestProperties.add("Authorization", "Bearer " + this.token.getAccessToken());
		if ( this.debug ) {
			//Message.printStatus(2, routine, "Reading datastream datapoint list using token: " + this.token.getAccessToken());
		}
		int timeoutSeconds = 300;
		if ( timeout != null ) {
			timeoutSeconds = timeout;
		}
		UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
		UrlResponse urlResponse = null;
		try {
			urlResponse = urlReader.read();
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading 'datastreams' datapoints using \"" + urlString + "\".");
			Message.printWarning(3, routine, e);
			throw new RuntimeException(e);
		}
		if ( urlResponse.hadError() ) {
			// TODO smalers 2020-06-12 would be nice to not catch this immediately.
			throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
				+ "): " + urlResponse.getResponseError() );
		}
		else if ( urlResponse.getResponseCode() != 200 ) {
			throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
		}
		else {
			// Parse the JSON response into objects.
			ObjectMapper mapper = new ObjectMapper();
			// Not needed.
			//mapper.registerModule(new JavaTimeModule());
			String responseJson = urlResponse.getResponse();
			Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
			try {
				if ( this.debug ) {
					Message.printStatus(2, routine, "Response=" + responseJson);
				}
				//datastreamDatapointList = mapper.readValue(responseJson, new TypeReference<List<DatastreamDatapoint>>(){});
				datastreamDatapoint = mapper.readValue(responseJson, new TypeReference<DatastreamDatapoint>(){});
				Message.printStatus(2, routine, "Read datastreamDatapoint with " + datastreamDatapoint.getRange().getCount() + " data points.");
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'datastreams' datapoint using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
		}

		// Sort on the name.
		//Collections.sort(datastreamList, new DatastreamComparator());
		return datastreamDatapoint;
	}

	/**
	 * Read global data that should be kept in memory to increase performance.
	 * This is called from the constructor.
	 * The following data are read and are available with get() methods:
	 * <ul>
	 * <li>TimeSeriesCatalog - cache used to find time series without re-requesting from the web service</li>
	 * </ul>
	 * If an error is detected, set on the datastore so that TSTool View / Datastores will show the error.
	 * This is usually an issue with a misconfigured datastore.
	 */
	public void readGlobalData () {
		String routine = getClass().getSimpleName() + ".readGlobalData";
		Message.printWarning ( 2, routine, "Reading global data for datastore \"" + getName() + "\"." );

		// Read the asset data.

		try {
			this.assetList = readAssetList();
			Message.printStatus(2, routine, "Read " + this.assetList.size() + " assets." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global asset list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		// Read the datastream data.

		try {
			this.datastreamList = readDatastreamList();
			Message.printStatus(2, routine, "Read " + this.datastreamList.size() + " datastreamList." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global datastreamList list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		// Read the measurement classification types:
		// - this does not seem to work

		/*
		try {
			this.measurementClassificationTypeList = readMeasurementClassificationTypeList();
			Message.printStatus(2, routine, "Read " + this.measurementClassificationTypeList.size() + " measurementClassificationTypes." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global measurementClassificationType list (" + e + ")");
			Message.printWarning(3, routine, e );
		}
		*/

		// Read the organization list:
		// - maybe not allowed?

		/*
		try {
			this.organizationList = readOrganizationList();
			Message.printStatus(2, routine, "Read " + this.organizationList.size() + " organizations." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global organization list (" + e + ")");
			Message.printWarning(3, routine, e );
		}
		*/

		// Read the station list.

		try {
			this.stationList = readStationList();
			Message.printStatus(2, routine, "Read " + this.stationList.size() + " stations." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global station list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		// Read the variable list:
		// - does not seem to work and not sure if it is useful for anything

		/*
		try {
			this.variableList = readVariableList();
			Message.printStatus(2, routine, "Read " + this.variableList.size() + " variables." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global variable list (" + e + ")");
			Message.printWarning(3, routine, e );
		}
		*/

		// The time series catalog COULD be used more throughout TSTool, such as when reading time series.
		// However, the initial implementation of readTimeSeries reads the list each time.
		// The cached list is used to create choices for the UI in order to ensure fast performance.
		// Therefore the slowdown is only at TSTool startup.
		try {
    		String dataTypeReq = null;
    		String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
    		// Read the catalog for all time series.
			this.tscatalogList = readTimeSeriesCatalog ( dataTypeReq, dataIntervalReq, ifp );
			Message.printStatus(2, routine, "Read " + this.tscatalogList.size() + " time series catalog." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global time series catalog list (" + e + ")");
			Message.printWarning(3, routine, e );
		}
	}

	/**
 	* Read the station list objects from the "/measurementClassificationTypes" service.
 	* The JSON looks like the following:
 	* <pre>
 	*
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
  ...
]
 	* </pre>
 	* @return the list of Station objects
 	*/
	private List<MeasurementClassificationType> readMeasurementClassificationTypeList() throws IOException {
		String routine = getClass().getSimpleName() + ".readMeasurementClassificationTypeList";
		checkTokenExpiration();
		// The following return an error:
		// (code=404): {"message":"no Route matched with those values"})
		//String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/measurementClassificationTypeList";
		//String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/measurementClassificationTypes";
		//String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/measurementClassifications";
		//String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/list-measurement-classification-types";
		//String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/measurement-classification-types";
		String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/measurement-classifications";
		Message.printStatus(2, routine, "Reading measurementClassificationType list from: " + urlString);
		List<MeasurementClassificationType> measurementClassificationTypeList = new ArrayList<>();

		String urlStringEncoded = urlString;
		MultiKeyStringDictionary requestProperties = new MultiKeyStringDictionary();
		requestProperties.add("Authorization", "Bearer " + this.token.getAccessToken());
		if ( this.debug ) {
			//Message.printStatus(2, routine, "Reading measurementClassificationType list using token: " + this.token.getAccessToken());
		}
		int timeoutSeconds = 300;
		UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
		UrlResponse urlResponse = null;
		try {
			urlResponse = urlReader.read();
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading 'measurementClassificationTypes' using \"" + urlString + "\".");
			Message.printWarning(3, routine, e);
			throw new RuntimeException(e);
		}
		if ( urlResponse.hadError() ) {
			// TODO smalers 2020-06-12 would be nice to not catch this immediately.
			throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
				+ "): " + urlResponse.getResponseError() );
		}
		else if ( urlResponse.getResponseCode() != 200 ) {
			throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
		}
		else {
			// Parse the JSON response into objects.
			ObjectMapper mapper = new ObjectMapper();
			// Not needed.
			//mapper.registerModule(new JavaTimeModule());
			String responseJson = urlResponse.getResponse();
			Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
			try {
				if ( this.debug ) {
					Message.printStatus(2, routine, "Response=" + responseJson);
				}
				measurementClassificationTypeList = mapper.readValue(responseJson, new TypeReference<List<MeasurementClassificationType>>(){});
				Message.printStatus(2, routine, "Read " + measurementClassificationTypeList.size() + " measurementClassificationTypes.");
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'measurementClassificationTypes' using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
		}

		// Sort on the name.
		//Collections.sort(measurementClassificationTypeList, new MeasurementClassificationTypeComparator());
		return measurementClassificationTypeList;
	}

	/**
 	* Read the organization list objects from the "/organizations" service.
 	* The JSON looks like the following:
 	* <pre>
 	* </pre>
 	* @return the list of Station objects
 	*/
	private List<Organization> readOrganizationList() throws IOException {
		String routine = getClass().getSimpleName() + ".readOrganizationList";
		checkTokenExpiration();
		String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/organizations";
		// The following returns:
		// (code=404): {"message":"no Route matched with those values"}
		//String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/list-organizations";
		Message.printStatus(2, routine, "Reading organization list from: " + urlString);
		List<Organization> organizationList = new ArrayList<>();

		String urlStringEncoded = urlString;
		MultiKeyStringDictionary requestProperties = new MultiKeyStringDictionary();
		requestProperties.add("Authorization", "Bearer " + this.token.getAccessToken());
		if ( this.debug ) {
			//Message.printStatus(2, routine, "Reading organization list using token: " + this.token.getAccessToken());
		}
		int timeoutSeconds = 300;
		UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
		UrlResponse urlResponse = null;
		try {
			urlResponse = urlReader.read();
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading 'organizations' using \"" + urlString + "\".");
			Message.printWarning(3, routine, e);
			throw new RuntimeException(e);
		}
		if ( urlResponse.hadError() ) {
			// TODO smalers 2020-06-12 would be nice to not catch this immediately.
			throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
				+ "): " + urlResponse.getResponseError() );
		}
		else if ( urlResponse.getResponseCode() != 200 ) {
			throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
		}
		else {
			// Parse the JSON response into objects.
			ObjectMapper mapper = new ObjectMapper();
			// Not needed.
			//mapper.registerModule(new JavaTimeModule());
			String responseJson = urlResponse.getResponse();
			Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
			try {
				if ( this.debug ) {
					Message.printStatus(2, routine, "Response=" + responseJson);
				}
				organizationList = mapper.readValue(responseJson, new TypeReference<List<Organization>>(){});
				Message.printStatus(2, routine, "Read " + organizationList.size() + " organizations.");
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'organizations' using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
		}

		// Sort on the name.
		//Collections.sort(organizationList, new OrganizationComparator());
		return organizationList;
	}

	/**
 	* Read the station list objects from the "/stations" service.
 	* The JSON looks like the following:
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
  ...
]
 	* </pre>
 	* @return the list of Station objects
 	*/
	private List<Station> readStationList() throws IOException {
		String routine = getClass().getSimpleName() + ".readStationList";
		checkTokenExpiration();
		String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/stations";
		// The following returns:
		// (code=404): {"message":"no Route matched with those values"}
		//String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/list-stations";
		Message.printStatus(2, routine, "Reading station list from: " + urlString);
		List<Station> stationList = new ArrayList<>();

		String urlStringEncoded = urlString;
		MultiKeyStringDictionary requestProperties = new MultiKeyStringDictionary();
		requestProperties.add("Authorization", "Bearer " + this.token.getAccessToken());
		if ( this.debug ) {
			//Message.printStatus(2, routine, "Reading station list using token: " + this.token.getAccessToken());
		}
		int timeoutSeconds = 300;
		UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
		UrlResponse urlResponse = null;
		try {
			urlResponse = urlReader.read();
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading 'stations' using \"" + urlString + "\".");
			Message.printWarning(3, routine, e);
			throw new RuntimeException(e);
		}
		if ( urlResponse.hadError() ) {
			// TODO smalers 2020-06-12 would be nice to not catch this immediately.
			throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
				+ "): " + urlResponse.getResponseError() );
		}
		else if ( urlResponse.getResponseCode() != 200 ) {
			throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
		}
		else {
			// Parse the JSON response into objects.
			ObjectMapper mapper = new ObjectMapper();
			// Not needed.
			//mapper.registerModule(new JavaTimeModule());
			String responseJson = urlResponse.getResponse();
			Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
			try {
				if ( this.debug ) {
					Message.printStatus(2, routine, "Response=" + responseJson);
				}
				stationList = mapper.readValue(responseJson, new TypeReference<List<Station>>(){});
				Message.printStatus(2, routine, "Read " + stationList.size() + " stations.");
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'stations' using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
		}

		// Sort on the name.
		Collections.sort(stationList, new StationComparator());
		
		// Clean the data.
		Station.cleanData ( stationList );

		return stationList;
	}

    /**
     * Read a single time series given its time series identifier using default read properties.
     * @param tsid time series identifier.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsid, DateTime readStart, DateTime readEnd, boolean readData ) {
    	String routine = getClass().getSimpleName() + ".readTimeSeries";
    	try {
    		return readTimeSeries ( tsid, readStart, readEnd, readData, null );
    	}
    	catch ( Exception e ) {
    		// Throw a RuntimeException since the method interface does not include an exception type.
    		Message.printWarning(2, routine, e);
    		throw new RuntimeException ( e );
    	}
    }

    /**
     * Read a single time series given its time series identifier.
     * @param tsidReq requested time series identifier.
     * The output time series may be different depending on the requested properties.
     * @param readStart start of read, will be set to 'periodStart' service parameter, can be in any time zone
     * @param readEnd end of read, will be set to 'periodEnd' service parameter, can be in any time zone
     * @param readProperties additional properties to control the query:
     * <ul>
     * <li> "Debug" - Boolean, if true, turn on debug for the query</li>
     * <li> "IrregularInterval" - irregular interval (e.g., "IrregHour" to use instead of TSID interval,
     *      where the TSID intervals corresponds to the web services.</li>
     * <li> "Timeout" - timeout (Integer seconds), for web service requests</li>
     * <li> "Timezone" - timezone such as "America/Denver" to convert from Campbell Cloud UTC</li>
     * </ul>
     * @return the time series or null if not read
     */
    public TS readTimeSeries (
    	String tsidReq,
    	DateTime readStart,
    	DateTime readEnd,
    	boolean readData,
    	HashMap<String,Object> readProperties
    	) throws Exception {
    	String routine = getClass().getSimpleName() + ".readTimeSeries";
		checkTokenExpiration();

    	// Get the properties of interest:
    	// - corresponds to parameters in the ReadCampbellCloud command
    	// - TSID command uses the defaults and may result in more exceptions because TSID can only handle general behavior
    	if ( readProperties == null ) {
    		// Create an empty hashmap if necessary to avoid checking for null below.
    		readProperties = new HashMap<>();
    	}
    	String IrregularInterval = null;
    	TimeInterval irregularInterval = null;
    	Object object = readProperties.get("IrregularInterval");
    	if ( object != null ) {
    		IrregularInterval = (String)object;
    		irregularInterval = TimeInterval.parseInterval(IrregularInterval);
    	}
    	// Allow the timeout to be set.
    	object = readProperties.get("Timeout");
    	Integer timeoutSeconds = null;
    	if ( object != null ) {
    		timeoutSeconds = (Integer)object;
    	}
    	// Default output time zone UTC:
    	// - will be overridden below if
    	ZoneId outputZoneId = ZoneId.of("UTC");
    	String timezone = outputZoneId.toString();
    	object = readProperties.get("Timezone");
    	if ( object != null ) {
    		// Have a time zone to use for output.
    		timezone = (String)object;
    		outputZoneId = ZoneId.of(timezone);
    	}
    	Message.printStatus(2, routine, "Output timezone is \"" + timezone + "\".");
    	if ( timezone.equals("UTC") ) {
    		Message.printStatus(2, routine, "  Campbell Cloud UTC data will be output without converting the time zone.");
    	}
    	else {
    		Message.printStatus(2, routine, "  Campbell Cloud UTC data will be converted to \"" + timezone + "\".");
    	}

		// Determine the period to read:
		// - 'readStartRequested' = as requested, defaulting to computer time zone if necessary
		// - 'readEndRequested' = as requested, defaulting to computer time zone if necessary
    	Message.printStatus(2, routine, "Reading requested period readStart=" + readStart + " to readEnd=" + readEnd);
		DateTime readStartRequested = determineReadStartRequested ( readStart );
		DateTime readStartUTC = determineReadStartUTC ( readStartRequested );

		DateTime readEndRequested = determineReadEndRequested ( readEnd );
		DateTime readEndUTC = determineReadEndUTC ( readEndRequested );

    	TS ts = null;

    	// Create a time series identifier for the requested TSID:
    	// - the actual output may be set to a different identifier based on the above properties
    	// - also save interval base and multiplier for the original request
    	TSIdent tsidentReq = TSIdent.parseIdentifier(tsidReq);

    	// Time series catalog for the single matching time series.
 		TimeSeriesCatalog tscatalog = null;

   		// Use Campbell Cloud time series identifier parts to match a time series in the catalog:
   		// - station_no.stationparamer_no-ts_shortname
   		// - if necessary: station_no.'stationparamer_no'-'ts_shortname'
   		List<TimeSeriesCatalog> tscatalogReqList = TimeSeriesCatalog.findForTSIdent ( this.tscatalogList, tsidentReq );
   		if ( tscatalogReqList.size() == 0 ) {
   			// Did not match any time series.
   			throw new RuntimeException ( "No time series catalog found matching TSID = \"" + tsidentReq + "\" in " + this.tscatalogList.size() + " tscatalog.");
   		}
   		else if ( tscatalogReqList.size() > 1 ) {
   			// Matched more than one time series so identifier information is not unique.
   			boolean dev = false;
   			if ( dev ) {
   				// For now use the first one.
   				Message.printStatus ( 2, routine, "Matched " + tscatalogReqList.size() + " time series catalog for TSID = " + tsidentReq + ", using the first.");
   				tscatalog = tscatalogReqList.get(0);
   			}
   			else {
   				// Production:
   				// - this should not happen and may require defining a new statio and re-adding the asset
   				throw new RuntimeException ( "Matched " + tscatalogReqList.size() + " time series catalog for TSID = " + tsidentReq + ", expecting 1.");
   			}
   		}
   		else {
   			// Matched a single time series so can continue:
   			// - ts_id is used below to read data
   			Message.printStatus(2, routine, "Matched a single time series catalog for TSID = \"" + tsidReq + "\"." );
   			tscatalog = tscatalogReqList.get(0);
   		}

    	// Create the time series and set properties:
    	// - above code used "req" (requested) variables based on the requested TSID
    	// - from this point forward the "out" variables are used,
    	//   in case IrregularInterval, Read24HourAsDay, or ReadDayAs24Hour properties were specified

   		if ( (irregularInterval != null) && !IrregularInterval.isEmpty() ) {
   			// Reset the irregular interval if requested.
   			tsidentReq.setInputName(IrregularInterval);
   			tsidReq = tsidentReq.toString();
   		}
    	ts = TSUtil.newTimeSeries(tsidReq, true);

    	// Set the time series properties.
    	//int intervalBaseOut = tsidentOut.getIntervalBase();
    	//int intervalMultOut = tsidentOut.getIntervalMult();
    	try {
    		ts.setIdentifier(tsidReq);
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}
    	// Set the period to bounding data records:
    	// - the period may be reset below depending on time series interval, interval end adjustments, etc.
    	// - TODO smalers 2023-01-17 may need to do more to handle the case of interval data timestamps being adjusted below
    	if ( readStartUTC != null ) {
    		ts.setDate1Original(readStartUTC);
    		/*
    		if ( TimeInterval.isRegularInterval(tsident.getIntervalBase()) ) {
    			// Round the start down to include a full interval.
    			readStart.round(-1, tsident.getIntervalBase(), tsident.getIntervalMult());
    		}
    		*/
    		ts.setDate1(readStartUTC);
    	}
    	if ( readEndUTC != null ) {
    		ts.setDate2Original(readEndUTC);
    		/*
    		if ( TimeInterval.isRegularInterval(tsident.getIntervalBase()) ) {
    			// Round the end up to include a full interval
    			readEnd.round(1, tsident.getIntervalBase(), tsident.getIntervalMult());
    		}
    		*/
    		ts.setDate2(readEndUTC);
    	}

    	// Set standard properties:
    	// - use station name and datastream field for the description because datastream does not have a name or description
		ts.setDescription(tscatalog.getStationName() + " - " + tscatalog.getDatastreamField());
		ts.setDataUnits("");
		ts.setDataUnitsOriginal("");
		ts.setMissing(Double.NaN);

		// Set the time series properties:
		// - additional properties are set below to help understand adjusted timestamps and offset days
		setTimeSeriesProperties ( ts, tscatalog );

    	if ( readData ) {
    		// Also read the time series values.
    		String datastreamId = tscatalog.getDatastreamId();
    		DatastreamDatapoint datastreamDatapoint = readDatastreamDatapoint ( datastreamId, readStartUTC, readEndUTC, timeoutSeconds );

    		// The data are ordered with oldest first:
    		// - the 'dataList' may be null, for example when the Campbell Cloud system is offline
    		List<DatastreamDatapointData> dataList = datastreamDatapoint.getData();
    		if ( (dataList != null) && (dataList.size() > 0) ) {
    			// Set the period based on data from the first and last values:
    			// - this values may be adjusted below
    			Message.printStatus(2, routine, "Read " + dataList.size() + " data points for datastream \""
    				+ datastreamId + "\" for readStartUTC=" + readStartUTC + " to readEndUTC=" + readEndUTC );
    			DateTime readStartOutput = dataList.get(0).getTimestampAsDateTime(outputZoneId);
    			ts.setDate1(readStartOutput);
    			ts.setDate1Original(ts.getDate1());
    			DateTime readEndOutput = dataList.get(dataList.size() - 1).getTimestampAsDateTime(outputZoneId);
    			ts.setDate2(readEndOutput);
    			ts.setDate2Original(ts.getDate2());

    			// Allocate the time series data array:
    			// - do this after adjusting the period for timestamps
    			// - irregular interval does not allocate an array up front
    			ts.allocateDataSpace();

    			// Loop through the data values and set the data.
    			for ( DatastreamDatapointData data : dataList ) {
    				DateTime dt = data.getTimestampAsDateTime ( outputZoneId );
    				Double value = data.getValue();
    				if ( (dt != null) && (value != null) ) {
    					// Nulls may occur if the API changes?
    					ts.setDataValue(dt, data.getValue());
    				}
    			}
    		}
    		else {
    			Message.printStatus(2, routine, "No datapoints read for datastream \""
    				+ datastreamId + "\" for readStartUTC=" + readStartUTC + " to readEndUTC=" + readEndUTC );
    		}

    	}

    	return ts;
    }

	/**
	 * Read time series catalog, which uses the cached datastreams and other data.
	 * @param dataTypeReq Requested data type (e.g., "DischargeRiver") or "*" to read all data types,
	 *        or null to use default of "*".
	 * @param dataIntervalReq Requested data interval (e.g., "IrregSecond") or "*" to read all intervals,
	 *        or null to use default of "*".
	 * @param ifp input filter panel with "where" conditions
	 */
	public List<TimeSeriesCatalog> readTimeSeriesCatalog ( String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp ) {
		String routine = getClass().getSimpleName() + ".readTimeSeriesCatalog";

		// Loop through the datastreams and add an entry in the catalog.
		List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();
		for ( Datastream datastream : this.datastreamList ) {
			if ( (dataTypeReq != null) && !dataTypeReq.isEmpty() && !dataTypeReq.equals("*") ) {
				// Filter based on the data type.
				if ( !datastream.getMetadata().getField().equals(dataTypeReq) ) {
					// Data type did not match.
					continue;
				}
			}

			if ( (dataIntervalReq != null) && !dataIntervalReq.isEmpty() && !dataIntervalReq.equals("*") ) {
				// Filter based on the data interval:
				// - currently all are IrregSecond so no filter
			}

			/*
			// Add query parameters based on the input filter:
			// - this includes list type parameters and specific parameters to match database values
			int numFilterWheres = 0; // Number of filter where clauses that are added.
			if ( ifp != null ) {
	        	int nfg = ifp.getNumFilterGroups ();
	        	InputFilter filter;
	        	for ( int ifg = 0; ifg < nfg; ifg++ ) {
	            	filter = ifp.getInputFilter ( ifg );
	            	//Message.printStatus(2, routine, "IFP whereLabel =\"" + whereLabel + "\"");
	            	boolean special = false; // TODO smalers 2022-12-26 might add special filters.
	            	if ( special ) {
	            	}
	            	else {
	            		// Add the query parameter to the URL.
				    	filter = ifp.getInputFilter(ifg);
				    	String queryClause = WebUtil.getQueryClauseFromInputFilter(filter,ifp.getOperator(ifg));
				    	if ( Message.isDebugOn ) {
				    		Message.printStatus(2,routine,"Filter group " + ifg + " where is: \"" + queryClause + "\"");
				    }
				    	if ( queryClause != null ) {
				    	requestUrl.append("&" + queryClause);
				    		++numFilterWheres;
				    	}
	            	}
	        	}
			}
			*/

			TimeSeriesCatalog tscatalog = new TimeSeriesCatalog();
			Message.printStatus(2, routine, "Adding tscatalog for datastream ID \"" + datastream.getId() + "\".");

			// Standard properties expected by TSTool:
			// - datatype - set below from datastream metadata
			// - interval is always IrregSecond pending other information
			// - not sure how to get the units
			tscatalog.setDataInterval("IrregSecond");
			tscatalog.setDataUnits("");

			// Set datastream data.
			tscatalog.setDatastreamId(datastream.getId());
			DatastreamMetadata datastreamMetadata = datastream.getMetadata();
			if ( datastreamMetadata != null ) {
				tscatalog.setDataType(datastreamMetadata.getField());
				tscatalog.setDatastreamAlias(datastreamMetadata.getAlias());
				tscatalog.setDatastreamField(datastreamMetadata.getField());
				tscatalog.setDatastreamStatus(datastreamMetadata.getStatus());
			}

			// Set station data.
			tscatalog.setStationId ( datastream.getStationId() );
			// Have enough data to set the time series identifier:
			// - this should be the same as the table model getTimeSeriesIdentifierFromTableModel method,
			//   but the table model is not active here
			tscatalog.setTSID(tscatalog.getStationId() + "." + TS_DATA_SOURCE + "." + tscatalog.getDataType() + "." + tscatalog.getDataInterval());
			Station station = Station.findStationForStationId ( this.stationList, datastream.getStationId() );
			if ( station != null ) {
				Message.printStatus(2, routine, "Found station \"" + station.getId() + "\" for datastream ID \"" + datastream.getId() + "\".");
				// Set station metadata in the tscatalog.
				StationMetadata stationMetadata = station.getMetadata();
				if ( stationMetadata != null ) {
					Message.printStatus(2, routine, "  Found station metadata.");
					tscatalog.setStationName(stationMetadata.getName());
					tscatalog.setStationDescription(stationMetadata.getDescription());
					Location location = stationMetadata.getLocation();
					if ( location != null ) {
					    Message.printStatus(2, routine, "  Found station metadata location.");
						tscatalog.setStationLatitude(location.getLatitude());
						tscatalog.setStationLongitude(location.getLongitude());
						tscatalog.setStationElevation(location.getElevation());
					}
					else {
						Message.printStatus(2, routine, "  Station metadata location is null.");
					}
					// Set station assets in the station catalog:
					// - the metadata assets are the asset identifiers.
					List<String> assets = stationMetadata.getAssets();
					if ( (assets != null) && !assets.isEmpty() ) {
						Message.printStatus(2, routine, "  Station metadata assets (" + assets.size() + ") are available.");
						// Only use the first found asset:
						// - seems to be one asset per station
						for ( String assetId : assets ) {
							Asset asset = Asset.findAssetForAssetId ( this.assetList, assetId );
							Message.printStatus(2, routine, "  Found station metadata asset.");
							if ( asset != null ) {
								// Add asset information and then break out of the loop.
								tscatalog.setAssetId(assetId);
								AssetMetadata assetMetadata = asset.getMetadata();
								if ( assetMetadata != null ) {
									tscatalog.setAssetManufacturer(assetMetadata.getManufacturer());
									tscatalog.setAssetModel(assetMetadata.getModel());
									tscatalog.setAssetName(assetMetadata.getName());
									tscatalog.setAssetDescription(assetMetadata.getDescription());
									tscatalog.setAssetStatus(assetMetadata.getStatus());
									tscatalog.setAssetMaintMode(assetMetadata.getMaintMode());
								}
								break;
							}
							else {
								Message.printStatus(2, routine, "  Station metadata asset is null for assetId \"" + assetId + "\".");
							}
						}
					}
					else {
						Message.printStatus(2, routine, "  Station metadata assets are not available.");
					}
				}
				else {
					Message.printStatus(2, routine, "  Station metadata is null.");
				}
			}
			else {
				Message.printStatus(2, routine, "Did not find station \"" + datastream.getStationId() + "\" for datastream \"" + datastream.getId() + "\".");
			}

			Asset asset = Asset.findAssetForAssetId ( this.assetList, datastream.getAssetId() );
			if ( asset != null ) {
				//tscatalog.setStationParameterLongName(timeSeries.getStationParameterLongName());
				//tscatalog.setStationParameterName(timeSeries.getStationParameterName());
				//tscatalog.setStationParameterNo(timeSeries.getStationParameterNo());
			}

			// Save the catalog in the list.
			tscatalogList.add(tscatalog);
		}

		// Check the catalog list for problems.
		for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
			// Make sure that the time series identifier is unique.
			boolean duplicateTsid = false;
			for ( TimeSeriesCatalog tscatalog2 : tscatalogList ) {
				if ( tscatalog == tscatalog2 ) {
					// Don't compare with itself.
					continue;
				}
				if ( tscatalog.getTSID().equals(tscatalog2.getTSID()) ) {
					// The time series identifier is not unique.
					duplicateTsid = true;
					break;
				}
			}
			if ( duplicateTsid ) {
				tscatalog.addProblem("TSID is not unique.");
			}
		}

		return tscatalogList;
	}

    /**
     * Read time series metadata, which results in a query that joins station, station_type, point, point_class, and point_type.
     */
    List<TimeSeriesCatalog> readTimeSeriesMeta ( String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp ) {
    	// Remove note from data type.
	   	int pos = dataTypeReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataTypeReq = dataTypeReq.substring(0, pos);
	   	}
	   	pos = dataIntervalReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataIntervalReq = dataIntervalReq.substring(0, pos).trim();
	   	}
	   	// By default all time series are included in the catalog:
	   	// - the filter panel options can be used to constrain
	    return readTimeSeriesCatalog ( dataTypeReq, dataIntervalReq, ifp );
	}

	/**
 	* Read the variable list objects from the "/variables" service.
 	* The JSON looks like the following:
 	* <pre>
 	*
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
  ...
]
 	* </pre>
 	* @return the list of Variable objects
 	*/
	private List<Variable> readVariableList() throws IOException {
		String routine = getClass().getSimpleName() + ".readVariableList";
		checkTokenExpiration();
		String urlString = getServiceRootURI() + "organizations/" + this.organizationId + "/variables";
		Message.printStatus(2, routine, "Reading variable list from: " + urlString);
		List<Variable> variableList = new ArrayList<>();

		String urlStringEncoded = urlString;
		MultiKeyStringDictionary requestProperties = new MultiKeyStringDictionary();
		requestProperties.add("Authorization", "Bearer " + this.token.getAccessToken());
		if ( this.debug ) {
			//Message.printStatus(2, routine, "Reading variable list using token: " + this.token.getAccessToken());
		}
		int timeoutSeconds = 300;
		UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
		UrlResponse urlResponse = null;
		try {
			urlResponse = urlReader.read();
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading 'variables' using \"" + urlString + "\".");
			Message.printWarning(3, routine, e);
			throw new RuntimeException(e);
		}
		if ( urlResponse.hadError() ) {
			// TODO smalers 2020-06-12 would be nice to not catch this immediately.
			throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
				+ "): " + urlResponse.getResponseError() );
		}
		else if ( urlResponse.getResponseCode() != 200 ) {
			throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
		}
		else {
			// Parse the JSON response into objects.
			ObjectMapper mapper = new ObjectMapper();
			// Not needed.
			//mapper.registerModule(new JavaTimeModule());
			String responseJson = urlResponse.getResponse();
			Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
			try {
				if ( this.debug ) {
					Message.printStatus(2, routine, "Response=" + responseJson);
				}
				variableList = mapper.readValue(responseJson, new TypeReference<List<Variable>>(){});
				Message.printStatus(2, routine, "Read " + variableList.size() + " variables.");
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'stations' using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
		}

		// Sort on the name.
		//Collections.sort(variableList, new VariableComparator());
		return variableList;
	}

    /**
     * Read the version from the web service, used when processing #@require commands in TSTool.
     * TODO smalers 2023-01-03 need to figure out if a version is available.
     */
    private String readVersion () {
		checkTokenExpiration();
    	return "";
    }

    /**
     * Set the time series properties from the TimeSeriesCatalog.
     * @param ts the time series to update
     * @param tscatalog the time series catalog matching the time series
     */
    private void setTimeSeriesProperties ( TS ts, TimeSeriesCatalog tscatalog ) {
    	// Set all the Campbell Cloud properties that are known for the time series:
    	// - use names that match the Campbell Cloud API to allow using the API documentation

    	ts.setProperty("station.description", tscatalog.getStationDescription());
    	ts.setProperty("station.elevation", tscatalog.getStationElevation());
    	ts.setProperty("station.id", tscatalog.getStationId());
    	ts.setProperty("station.latitude", tscatalog.getStationLatitude());
    	ts.setProperty("station.longitude", tscatalog.getStationLongitude());
    	ts.setProperty("station.name", tscatalog.getStationName());

    	ts.setProperty("asset.description", tscatalog.getAssetDescription());
    	ts.setProperty("asset.id", tscatalog.getAssetId());
    	ts.setProperty("asset.manufacturer", tscatalog.getAssetManufacturer());
    	ts.setProperty("asset.model", tscatalog.getAssetModel());
    	ts.setProperty("asset.name", tscatalog.getAssetName());
    	ts.setProperty("asset.status", tscatalog.getAssetStatus());
    	ts.setProperty("asset.maint_mode", tscatalog.getAssetMaintMode());

    	ts.setProperty("datastream.alias", tscatalog.getDatastreamAlias());
    	ts.setProperty("datastream.field", tscatalog.getDatastreamField());
    	ts.setProperty("datastream.id", tscatalog.getDatastreamId());
    	ts.setProperty("datastream.status", tscatalog.getDatastreamStatus());
    }

}