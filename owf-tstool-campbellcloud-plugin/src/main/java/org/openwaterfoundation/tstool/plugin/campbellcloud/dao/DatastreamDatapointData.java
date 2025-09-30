// DatastreamDatapointData - results from datastream/datastreamID/datapoints for "data" part

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

import java.time.ZoneId;
import java.time.ZoneOffset;

import org.openwaterfoundation.tstool.plugin.campbellcloud.util.TimeUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import RTi.Util.Time.DateTime;

/**
 * Campbell Cloud API results from datastream/datastreamID/datapoints for the "data" part
 * <pre>
{
  "range": {
    "count": 288,
    "exceeded_request": false,
    "end": 1758237300000,
    "start": 1758151200000
  },
  "data": [
    {
      "value": 3.663831,
      "ts": 1758151200000
    },
    {
      "value": 3.664046,
      "ts": 1758151500000
    },
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class DatastreamDatapointData {
	// List in the order of the object.
	/**
	 * "value"
	 */
	@JsonProperty("value")
	private Double value = null;
	
	/**
	 * "ts" - timestamp as UNIX Epoch milliseconds, UTC
	 */
	@JsonProperty("ts")
	private Long timestamp = null;
	
	/**
	 * Default constructor used by Jackson.
	 */
	public DatastreamDatapointData () {
	}

	/**
	 * Return the value.
	 * @return the value. 
	 */
	public Double getValue () {
		return this.value;
	}

	/**
	 * Return the timestamp as UTC UNIX epoch milliseconds.
	 * @return the timestamp as UTC UNIX epoch milliseconds. 
	 */
	public Long getTimestamp () {
		return this.timestamp;
	}

	/**
	 * Return the timestamp for the requested zone offset.
	 * @param zoneOffset the time zone offset, used with OffsetDateTime
	 * @return the timestamp for the requested zone offsetUTC DateTime.
	 */
	public DateTime getTimestampAsDateTime ( ZoneOffset zoneOffset ) {
		return TimeUtil.epochMilliToDateTime ( this.timestamp, zoneOffset );
	}

	/**
	 * Return the timestamp for the requested zone offset.
	 * @param zoneId the time zone ID, used with ZonedDateTime
	 * @return the timestamp for the requested zone offsetUTC DateTime.
	 */
	public DateTime getTimestampAsDateTime ( ZoneId zoneId ) {
		return TimeUtil.epochMilliToDateTime ( this.timestamp, zoneId );
	}
	
}