// DatastreamDatapointRange - results from datastream/datastreamID/datapoints for "range" part

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Campbell Cloud API results from datastream/datastreamID/datapoints for the "range" part
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
public class DatastreamDatapointRange {
	// List in the order of the object.
	/**
	 * "count"
	 */
	private Integer count = null;

	/**
	 * "exceeded_request"
	 */
	private Boolean exceededRequest = null;
	
	/**
	 * "end" - timestamp of the earliest value as UNIX Epoch milliseconds, UTC
	 */
	private Long end = null;

	/**
	 * "start" - timestamp of the latest value as UNIX Epoch milliseconds, UTC
	 */
	private Long start = null;
	
	/**
	 * Default constructor used by Jackson.
	 */
	public DatastreamDatapointRange () {
	}

	/**
	 * Return the value.
	 * @return the value. 
	 */
	public Integer getCount () {
		return this.count;
	}

	/**
	 * Return whether the limit on data points has been exceeded (if true, multiple requests will need to be used).
	 * @return whether the limit on data points has been exceeded
	 */
	public Boolean getExceededRequest () {
		return this.exceededRequest;
	}

	/**
	 * Return the end timestamp.
	 * @return the end timestamp. 
	 */
	public Long getEnd () {
		return this.end;
	}

	/**
	 * Return the start timestamp.
	 * @return the start timestamp. 
	 */
	public Long getStart () {
		return this.start;
	}
	
}