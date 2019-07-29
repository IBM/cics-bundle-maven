package com.ibm.cics.cbmp;

/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2019 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class BundleDeployTest {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());
	
	private static String bundleFilePath = "src/test/resources/test-app-bundle-0.0.1-SNAPSHOT.cics-bundle";

	@Test
	public void testBundleDeployHelper_response200() {
		stubFor(post(urlEqualTo("/deploy"))
				.willReturn(aResponse()
					.withStatus(200)
					.withHeader("Content-Type", "text/plain")
					.withBody("Some content")));

		File bundleArchive = new File(bundleFilePath);

		try {
			BundleDeployHelper.deployBundle(new URI(wireMockRule.baseUrl() + "/deploy"), bundleArchive, "bundle", "csdgroup", "cicsplex", "region", "username", "password");
		} catch (BundleDeployException | IOException | URISyntaxException e) {
			fail("Failed with exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testBundleDeployHelper_invalidFile() {
		stubFor(post(urlEqualTo("/deploy"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain")
						.withBody("Some content")));

		File bundleArchive = new File("invalid path");
		
	    try {
	    		 BundleDeployHelper.deployBundle(new URI(wireMockRule.baseUrl() + "/deploy"), bundleArchive, "bundle", "csdgroup", "cicsplex", "region", "username", "password");
	    		 fail("Should have failed with an 'invalid path' exception");
		} catch (BundleDeployException e) {
			Assert.assertEquals("Bundle does not exist: 'invalid path'", e.getMessage());
		} catch (IOException e) {
			fail("Failed with an IOException: " + e.getMessage());
		} catch (URISyntaxException e) {
			fail("Failed with an URISyntaxException: " + e.getMessage());			
		}
	}
	
	@Test
	public void testBundleDeployHelper_invalidBundle() {

		stubFor(post(urlEqualTo("/deploy"))
				.willReturn(aResponse()
						.withStatus(400)
						.withHeader("Content-Type", "application/json")
						.withBody("{\"stage\":\"Validate bundle definition\",\"message\": \"Stage: Validate bundle definition, Cause: Derived bundledir \\\"" + bundleFilePath + "\\\" didn't match the target BUNDDEF's bundle dir \\\"" + bundleFilePath + "\\\"\"}")));

		File bundleArchive = new File(bundleFilePath);
		
	    try {
	    		 BundleDeployHelper.deployBundle(new URI(wireMockRule.baseUrl() + "/deploy"), bundleArchive, "bundle", "csdgroup", "cicsplex", "region", "username", "password");
	    		 fail("Should have failed with an 'invalid path' exception");
		} catch (BundleDeployException e) {
			Assert.assertEquals("Stage: Validate bundle definition, Cause: Derived bundledir \"" + bundleFilePath + "\" didn't match the target BUNDDEF's bundle dir \"" + bundleFilePath + "\"", e.getMessage());
		} catch (IOException e) {
			fail("Failed with an IOException: " + e.getMessage());
		} catch (URISyntaxException e) {
			fail("Failed with an URISyntaxException: " + e.getMessage());			
		}
	}
	
	@Test
	public void testBundleDeployHelper_unauthenticated401() {
	    stubFor(post(urlEqualTo("/deploy"))
	            .willReturn(aResponse()
	                .withStatus(401)
	                .withHeader("Content-Type", "text/plain")
	                .withBody("Http response: HTTP/1.1 401 Unauthorized")));

	    File bundleArchive = new File(bundleFilePath);
	    
	    try {
	    		 BundleDeployHelper.deployBundle(new URI(wireMockRule.baseUrl() + "/deploy"), bundleArchive, "bundle", "csdgroup", "cicsplex", "region", "username", "password");
	    		 fail("Should have failed with an unauthorized exception");
		} catch (BundleDeployException e) {
			Assert.assertEquals("Http response: HTTP/1.1 401 Unauthorized", e.getMessage());
		} catch (IOException e) {
			fail("Failed with an IOException: " + e.getMessage());
		} catch (URISyntaxException e) {
			fail("Failed with an URISyntaxException: " + e.getMessage());			
		}
	}
}
