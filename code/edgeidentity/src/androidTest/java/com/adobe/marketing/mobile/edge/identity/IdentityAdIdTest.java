/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.identity;

import static com.adobe.marketing.mobile.TestHelper.getDispatchedEventsWith;
import static com.adobe.marketing.mobile.TestHelper.getXDMSharedStateFor;
import static com.adobe.marketing.mobile.edge.identity.IdentityFunctionalTestUtil.registerEdgeIdentityExtension;
import static com.adobe.marketing.mobile.edge.identity.IdentityFunctionalTestUtil.setEdgeIdentityPersistence;
import static com.adobe.marketing.mobile.edge.identity.IdentityTestUtil.createXDMIdentityMap;
import static com.adobe.marketing.mobile.edge.identity.IdentityTestUtil.flattenMap;
import static org.junit.Assert.assertEquals;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.TestHelper;
import com.adobe.marketing.mobile.TestPersistenceHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class IdentityAdIdTest {

	@Rule
	public RuleChain rule = RuleChain
		.outerRule(new TestHelper.SetupCoreRule())
		.around(new TestHelper.RegisterMonitorExtensionRule());

	@Test
	public void testGenericIdentityRequest_whenValidAdId_thenNewValidAdId() throws Exception {
		// Test
		// Randomly generated valid UUID values (tests both value and exact format to be used in production)
		String initialAdId = "fa181743-2520-4ebc-b125-626baf1e3db8";
		String newAdId = "8d9ca5ff-7e74-44ac-bbcd-7aee7baf4f6c";
		setEdgeIdentityPersistence(
			createXDMIdentityMap(
				new IdentityTestUtil.TestItem("ECID", "primaryECID"),
				new IdentityTestUtil.TestItem("GAID", initialAdId)
			)
		);
		registerEdgeIdentityExtension();

		MobileCore.setAdvertisingIdentifier(newAdId);
		// After sending mobile core event, give a wait time to allow for processing
		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Edge Consent event should not be dispatched; valid -> valid does not signal change in consent
		verifyDispatchedEvents(true, null);

		// Verify XDM shared state
		Map<String, String> xdmSharedState = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState, newAdId);

		// Verify persisted data
		final String persistedJson = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson)));
		verifyFlatIdentityMap(persistedMap, newAdId);
	}

	@Test
	public void testGenericIdentityRequest_whenValidAdId_thenNonAdId() throws Exception {
		// Test
		String initialAdId = "fa181743-2520-4ebc-b125-626baf1e3db8";
		setEdgeIdentityPersistence(
			createXDMIdentityMap(
				new IdentityTestUtil.TestItem("ECID", "primaryECID"),
				new IdentityTestUtil.TestItem("GAID", initialAdId)
			)
		);
		registerEdgeIdentityExtension();

		dispatchGenericIdentityNonAdIdEvent();

		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Edge Consent event should not be dispatched; valid -> (unchanged) valid does not signal change in consent
		verifyDispatchedEvents(false, null);

		// Verify XDM shared state
		Map<String, String> xdmSharedState = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState, initialAdId);

		// Verify persisted data
		final String persistedJson = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson)));
		verifyFlatIdentityMap(persistedMap, initialAdId);
	}

	@Test
	public void testGenericIdentityRequest_whenValidAdId_thenSameValidAdId() throws Exception {
		// Test
		String initialAdId = "fa181743-2520-4ebc-b125-626baf1e3db8";
		String newAdId = "fa181743-2520-4ebc-b125-626baf1e3db8";
		setEdgeIdentityPersistence(
			createXDMIdentityMap(
				new IdentityTestUtil.TestItem("ECID", "primaryECID"),
				new IdentityTestUtil.TestItem("GAID", initialAdId)
			)
		);
		registerEdgeIdentityExtension();

		MobileCore.setAdvertisingIdentifier(newAdId);
		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Edge Consent event should not be dispatched; valid -> valid does not signal change in consent
		verifyDispatchedEvents(true, null);

		// Verify XDM shared state
		Map<String, String> xdmSharedState = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState, newAdId);

		// Verify persisted data
		final String persistedJson = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson)));
		verifyFlatIdentityMap(persistedMap, newAdId);
	}

	@Test
	public void testGenericIdentityRequest_whenValidAdId_thenEmptyAdId() throws Exception {
		// Test
		String initialAdId = "fa181743-2520-4ebc-b125-626baf1e3db8";
		String newAdId = "";
		setEdgeIdentityPersistence(
			createXDMIdentityMap(
				new IdentityTestUtil.TestItem("ECID", "primaryECID"),
				new IdentityTestUtil.TestItem("GAID", initialAdId)
			)
		);
		registerEdgeIdentityExtension();

		MobileCore.setAdvertisingIdentifier(newAdId);
		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Edge Consent event should be dispatched; valid -> invalid signals change in consent
		verifyDispatchedEvents(true, "n");

		// Verify XDM shared state
		Map<String, String> xdmSharedState = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState, null);

		// Verify persisted data
		final String persistedJson = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson)));
		verifyFlatIdentityMap(persistedMap, null);
	}

	@Test
	public void testGenericIdentityRequest_whenValidAdId_thenAllZerosAdId() throws Exception {
		// Test
		String initialAdId = "fa181743-2520-4ebc-b125-626baf1e3db8";
		String newAdId = "00000000-0000-0000-0000-000000000000";
		setEdgeIdentityPersistence(
			createXDMIdentityMap(
				new IdentityTestUtil.TestItem("ECID", "primaryECID"),
				new IdentityTestUtil.TestItem("GAID", initialAdId)
			)
		);
		registerEdgeIdentityExtension();

		MobileCore.setAdvertisingIdentifier(newAdId);
		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Edge Consent event should be dispatched; valid -> invalid signals change in consent
		verifyDispatchedEvents(true, "n");

		// Verify XDM shared state
		Map<String, String> xdmSharedState = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState, null);

		// Verify persisted data
		final String persistedJson = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson)));
		verifyFlatIdentityMap(persistedMap, null);
	}

	@Test
	public void testGenericIdentityRequest_whenNoAdId_thenNewValidAdId() throws Exception {
		// Test
		String newAdId = "8d9ca5ff-7e74-44ac-bbcd-7aee7baf4f6c";
		setEdgeIdentityPersistence(createXDMIdentityMap(new IdentityTestUtil.TestItem("ECID", "primaryECID")));
		registerEdgeIdentityExtension();

		MobileCore.setAdvertisingIdentifier(newAdId);
		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Generic Identity event containing advertisingIdentifier should be dispatched
		// Edge Consent event should not be dispatched; valid -> valid does not signal change in consent
		verifyDispatchedEvents(true, "y");

		// Verify XDM shared state
		Map<String, String> xdmSharedState = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState, newAdId);

		// Verify persisted data
		final String persistedJson = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson)));
		verifyFlatIdentityMap(persistedMap, newAdId);
	}

	@Test
	public void testGenericIdentityRequest_whenNoAdId_thenNonAdId() throws Exception {
		// Test
		setEdgeIdentityPersistence(createXDMIdentityMap(new IdentityTestUtil.TestItem("ECID", "primaryECID")));
		registerEdgeIdentityExtension();

		dispatchGenericIdentityNonAdIdEvent();

		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Edge Consent event should not be dispatched; valid -> valid does not signal change in consent
		verifyDispatchedEvents(false, null);

		// Verify XDM shared state
		Map<String, String> xdmSharedState = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState, null);

		// Verify persisted data
		final String persistedJson = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson)));
		verifyFlatIdentityMap(persistedMap, null);
	}

	@Test
	public void testGenericIdentityRequest_whenNoAdId_thenEmptyAdId() throws Exception {
		// Test
		String newAdId = "";
		setEdgeIdentityPersistence(createXDMIdentityMap(new IdentityTestUtil.TestItem("ECID", "primaryECID")));
		registerEdgeIdentityExtension();

		MobileCore.setAdvertisingIdentifier(newAdId);
		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Edge Consent event should not be dispatched; invalid -> invalid does not signal change in consent
		verifyDispatchedEvents(true, null);

		// Verify XDM shared state
		Map<String, String> xdmSharedState = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState, null);

		// Verify persisted data
		final String persistedJson = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson)));
		verifyFlatIdentityMap(persistedMap, null);
	}

	@Test
	public void testGenericIdentityRequest_whenNoAdId_thenAllZerosAdIdTwice() throws Exception {
		// Test
		String newAdId = "00000000-0000-0000-0000-000000000000";
		setEdgeIdentityPersistence(createXDMIdentityMap(new IdentityTestUtil.TestItem("ECID", "primaryECID")));
		registerEdgeIdentityExtension();

		MobileCore.setAdvertisingIdentifier(newAdId);
		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Edge Consent event should not be dispatched; invalid -> invalid does not signal change in consent
		verifyDispatchedEvents(true, null);

		// Verify XDM shared state
		Map<String, String> xdmSharedState = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState, null);

		// Verify persisted data
		final String persistedJson = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson)));
		verifyFlatIdentityMap(persistedMap, null);

		// Reset wildcard listener
		TestHelper.resetTestExpectations();
		// Test all zeros sent again
		MobileCore.setAdvertisingIdentifier(newAdId);
		TestHelper.waitForThreads(2000);
		// Verify dispatched events
		// Edge Consent event should not be dispatched; invalid -> invalid does not signal change in consent
		verifyDispatchedEvents(true, null);

		// Verify XDM shared state
		Map<String, String> xdmSharedState2 = flattenMap(getXDMSharedStateFor(IdentityConstants.EXTENSION_NAME, 1000));
		verifyFlatIdentityMap(xdmSharedState2, null);

		// Verify persisted data
		final String persistedJson2 = TestPersistenceHelper.readPersistedData(
			IdentityConstants.DataStoreKey.DATASTORE_NAME,
			IdentityConstants.DataStoreKey.IDENTITY_PROPERTIES
		);
		Map<String, String> persistedMap2 = flattenMap(IdentityTestUtil.toMap(new JSONObject(persistedJson2)));
		verifyFlatIdentityMap(persistedMap2, null);
	}

	/**
	 * Verifies that the expected events from the {@link MobileCore#setAdvertisingIdentifier(String)} or {@link MobileCore#dispatchEvent(Event, ExtensionErrorCallback)}
	 * APIs are properly dispatched. Verifies:
	 * 1. Event type and source
	 * 2. Event data/properties as required for proper ad ID functionality
	 * @param isGenericIdentityEventAdIdEvent true if the expected {@link com.adobe.marketing.mobile.edge.identity.IdentityConstants.EventType#GENERIC_IDENTITY}
	 *                                           event should be an ad ID event, false otherwise
	 * @param expectedConsentValue the expected consent value in the format {@link IdentityConstants.XDMKeys.Consent#YES}
	 *                                or {@link IdentityConstants.XDMKeys.Consent#NO}; however, if consent event should not be dispatched, use null
	 * @throws Exception
	 */
	private void verifyDispatchedEvents(boolean isGenericIdentityEventAdIdEvent, String expectedConsentValue)
		throws Exception {
		// Check the event type and source
		List<Event> dispatchedGenericIdentityEvents = getDispatchedEventsWith(
			IdentityConstants.EventType.GENERIC_IDENTITY,
			IdentityConstants.EventSource.REQUEST_CONTENT
		);
		// Verify Generic Identity event
		assertEquals(1, dispatchedGenericIdentityEvents.size());
		Event genericIdentityEvent = dispatchedGenericIdentityEvents.get(0);
		assertEquals(isGenericIdentityEventAdIdEvent ? true : false, EventUtils.isAdIdEvent(genericIdentityEvent));
		// Verify Edge Consent event
		List<Event> dispatchedConsentEvents = getDispatchedEventsWith(
			IdentityConstants.EventType.EDGE_CONSENT,
			IdentityConstants.EventSource.UPDATE_CONSENT
		);
		assertEquals(Utils.isNullOrEmpty(expectedConsentValue) ? 0 : 1, dispatchedConsentEvents.size());
		if (!Utils.isNullOrEmpty(expectedConsentValue)) {
			Map<String, String> consentDataMap = flattenMap(dispatchedConsentEvents.get(0).getEventData());
			assertEquals("GAID", consentDataMap.get("consents.adID.idType"));
			assertEquals(expectedConsentValue, consentDataMap.get("consents.adID.val"));
		}
	}

	/**
	 * Verifies the flat map contains the required ad ID and ECID
	 * Valid ECID string and flat identity map is always required
	 * @param flatIdentityMap the flat identity map to check
	 * @param expectedAdId the ad ID to check, should be null if no ad ID should be present; then the absence of ad ID will be verified
	 * @return true if identity map contains the required identity properties, false otherwise
	 */
	private void verifyFlatIdentityMap(
		@NonNull final Map<String, String> flatIdentityMap,
		@Nullable final String expectedAdId
	) {
		if (expectedAdId != null) {
			assertEquals(6, flatIdentityMap.size()); // updated ad ID + ECID
			assertEquals("false", flatIdentityMap.get("identityMap.GAID[0].primary"));
			assertEquals(expectedAdId, flatIdentityMap.get("identityMap.GAID[0].id"));
			assertEquals("ambiguous", flatIdentityMap.get("identityMap.GAID[0].authenticatedState"));
		} else {
			assertEquals(3, flatIdentityMap.size()); // ECID
		}
		String expectedECID = "primaryECID";
		assertEquals("false", flatIdentityMap.get("identityMap.ECID[0].primary"));
		assertEquals(expectedECID, flatIdentityMap.get("identityMap.ECID[0].id"));
		assertEquals("ambiguous", flatIdentityMap.get("identityMap.ECID[0].authenticatedState"));
		return;
	}

	/**
	 * Dispatches an event using the MobileCore dispatchEvent API. Event has type {@link com.adobe.marketing.mobile.edge.identity.IdentityConstants.EventType#GENERIC_IDENTITY}
	 * and source {@link com.adobe.marketing.mobile.edge.identity.IdentityConstants.EventSource#REQUEST_CONTENT}.
	 * This is the combination of event type and source that the ad ID listener will capture, and this
	 * method helps set up test cases that verify the ad ID is not modified if the advertisingIdentifier
	 * property is not present in the correct format
	 */
	private void dispatchGenericIdentityNonAdIdEvent() {
		Event genericIdentityNonAdIdEvent = new Event.Builder(
			"Test event",
			IdentityConstants.EventType.GENERIC_IDENTITY,
			IdentityConstants.EventSource.REQUEST_CONTENT
		)
			.setEventData(
				new HashMap<String, Object>() {
					{
						put("somekey", "somevalue");
					}
				}
			)
			.build();
		MobileCore.dispatchEvent(
			genericIdentityNonAdIdEvent,
			new ExtensionErrorCallback<ExtensionError>() {
				@Override
				public void error(ExtensionError extensionError) {
					Log.e("IdentityAdIdTest", "Failed to dispatch event." + extensionError.toString());
				}
			}
		);
	}
}
