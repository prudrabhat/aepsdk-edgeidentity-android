/*
  Copyright 2021 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.identity;

/**
 * Represents the authentication state for an {@link IdentityItem}
 */
public enum AuthenticatedState {
	/**
	 * The state is ambiguous.
	 */
	AMBIGUOUS("ambiguous"),

	/**
	 * User identified by a login or similar action that was valid at the time of the event observation.
	 */
	AUTHENTICATED("authenticated"),

	/**
	 * User was identified by a login action at some point of time previously, but is not currently logged in.
	 */
	LOGGED_OUT("loggedOut");

	private String name;

	private AuthenticatedState(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static AuthenticatedState fromString(final String state) {
		if (AUTHENTICATED.getName().equalsIgnoreCase(state)) {
			return AUTHENTICATED;
		} else if (LOGGED_OUT.getName().equalsIgnoreCase(state)) {
			return LOGGED_OUT;
		} else {
			return AMBIGUOUS;
		}
	}
}
