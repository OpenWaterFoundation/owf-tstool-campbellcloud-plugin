// Token - results from 'token' service

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

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Campbell Cloud API "token" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Token {
	/**
	 * "access_token"
	 */
	@JsonProperty("access_token")
	private String accessToken = "";
	
	/**
	 * "expires_in"
	 */
	@JsonProperty("expires_in")
	private Integer expiresIn = null;

	/**
	 * "refresh_expires_in"
	 */
	@JsonProperty("refresh_expires_in")
	private Integer refreshExpiresIn = null;

	/**
	 * "refresh_token"
	 */
	@JsonProperty("refresh_token")
	private String refreshToken = "";

	/**
	 * "token_type"
	 */
	@JsonProperty("token_typerefresh_token")
	private String tokenType = "";

	/**
	 * "not_before_policy"
	 */
	@JsonProperty("not_before_policy")
	private Integer notBeforePolicy = null;
	
	/**
	 * The time that the token was created.
	 * This is used to check when it expires.
	 */
	@JsonIgnore
	Instant instantCreated = null;
	
	/**
	 * Constructor.
	 */
	public Token ( ) {
		// Set the time when the token was created, which is used to evaluate the expiration time:
		// - it is assumed that the JSON that is decoded from Campbell Cloud happens quickly
		this.instantCreated = Instant.now();
	}

	/**
	 * Return the access token.
	 * @return the access token
	 */
	public String getAccessToken () {
		return this.accessToken;
	}
	
	/**
	 * Determine whether the token is expired,
	 * which will be if the current time is after the token expiration time.
	 */
	@JsonIgnore
	public boolean isExpired () {
		Instant now = Instant.now();
		long nowEpochSeconds = now.getEpochSecond();
		// Calculate the difference between now and when the token was created:
		// - typically expires in 3600 seconds, based on Campbell Cloud documentation?
		// - subtract 100 seconds from the difference to give a bit of cushion
		if ( (nowEpochSeconds - this.instantCreated.getEpochSecond()) > (this.expiresIn - 100) ) {
			// Token is expired.
			return true;
		}
		else {
			// Token is not expired.
			return false;
		}
	}
	
}