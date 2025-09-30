// DatastreamDatapoints - results from list-datastreams/ID/get-datastream-datapoints service

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
 * Campbell Cloud API "datastreams/ID/datapoints..."  result.
<pre>
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
</pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class DatastreamDatapoint {
	// List in the order of the example above.
	/**
	 * "range"
	 */
	@JsonProperty("range")
	private DatastreamDatapointRange range = null;

	/**
	 * "data"
	 */
	@JsonProperty("data")
	private List<DatastreamDatapointData> data = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public DatastreamDatapoint () {
	}

	/**
	 * Return the datastream data points list range.
	 * @return the datastream data points list range. 
	 */
	public DatastreamDatapointRange getRange () {
		return this.range;
	}

	/**
	 * Return the datastream data points data.
	 * @return the datastream data points data. 
	 */
	public List<DatastreamDatapointData> getData () {
		return this.data;
	}

}