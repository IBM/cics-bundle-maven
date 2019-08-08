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

import java.io.File;
import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class BundleDeployHelperTest {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private static String bundleFilePath = "src/test/resources/test-app-bundle-0.0.1-SNAPSHOT.zip";

	@Test
	public void testBundleDeployHelper_response200() throws Exception {
		stubFor(post(urlEqualTo("/deploy"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/plain")
				.withBody("Some content")
			)
		);

		File bundleArchive = new File(bundleFilePath);

		BundleDeployHelper.deployBundle(new URI(wireMockRule.baseUrl()), bundleArchive, "bundle", "csdgroup", "cicsplex", "region", "username", "password");
	}
	
	@Test
	public void testBundleDeployHelper_invalidFile() throws Exception {
		stubFor(post(urlEqualTo("/deploy"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/plain")
				.withBody("Some content")
			)
		);

		File bundleArchive = new File("invalid path");
		
		expectedException.expect(BundleDeployException.class);
		expectedException.expectMessage("Bundle does not exist: 'invalid path'");
		
		BundleDeployHelper.deployBundle(new URI(wireMockRule.baseUrl()), bundleArchive, "bundle", "csdgroup", "cicsplex", "region", "username", "password");
	}
	
	@Test
	public void testBundleDeployHelper_invalidBundle() throws Exception {
		stubFor(post(urlEqualTo("/deploy"))
			.willReturn(aResponse()
				.withStatus(400)
				.withHeader("Content-Type", "application/json")
				.withBody("{\"stage\":\"Validate bundle definition\",\"message\": \"Stage: Validate bundle definition, Cause: Derived bundledir \\\"" + bundleFilePath + "\\\" didn't match the target BUNDDEF's bundle dir \\\"" + bundleFilePath + "\\\"\"}")
			)
		);

		File bundleArchive = new File(bundleFilePath);
		
		expectedException.expect(BundleDeployException.class);
		expectedException.expectMessage("Stage: Validate bundle definition, Cause: Derived bundledir \"" + bundleFilePath + "\" didn't match the target BUNDDEF's bundle dir \"" + bundleFilePath + "\"");
		
		BundleDeployHelper.deployBundle(new URI(wireMockRule.baseUrl()), bundleArchive, "bundle", "csdgroup", "cicsplex", "region", "username", "password");
	}
	
	@Test
	public void testBundleDeployHelper_unauthenticated401() throws Exception {
		stubFor(post(urlEqualTo("/deploy"))
			.willReturn(aResponse()
				.withStatus(401)
				.withHeader("Content-Type", "text/plain")
				.withBody("Http response: HTTP/1.1 401 Unauthorized")
			)
		);

		File bundleArchive = new File(bundleFilePath);
		
		expectedException.expect(BundleDeployException.class);
		expectedException.expectMessage("Http response: HTTP/1.1 401 Unauthorized");

		BundleDeployHelper.deployBundle(new URI(wireMockRule.baseUrl()), bundleArchive, "bundle", "csdgroup", "cicsplex", "region", "username", "password");
	}
	
	@Test
	public void testBundleDeployHelper_unauthenticated401_noContentType() throws Exception {
		stubFor(post(urlEqualTo("/deploy"))
			.willReturn(aResponse()
				.withStatus(401)
				.withBody("Http response: HTTP/1.1 401 Unauthorized")
			)
		);

		File bundleArchive = new File(bundleFilePath);
		
		expectedException.expect(BundleDeployException.class);
		expectedException.expectMessage("Http response: HTTP/1.1 401 Unauthorized");

		BundleDeployHelper.deployBundle(new URI(wireMockRule.baseUrl()), bundleArchive, "bundle", "csdgroup", "cicsplex", "region", "username", "password");
	}
	
	@Test
	public void noPath() throws Exception {
		stubFor(post(urlEqualTo("/deploy"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/plain")
				.withBody("Some content")
			)
		);
		
		File bundleArchive = new File(bundleFilePath);
		
		BundleDeployHelper.deployBundle(
			new URI(wireMockRule.baseUrl()),
			bundleArchive,
			"bundle",
			"csdgroup",
			"cicsplex",
			"region",
			"username",
			"password"
		);
	}
	
	@Test
	public void noPathSlash() throws Exception {
		stubFor(post(urlEqualTo("/deploy"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/plain")
				.withBody("Some content")
			)
		);
		
		File bundleArchive = new File(bundleFilePath);
		
		URI endpointURL = new URI(wireMockRule.baseUrl() + "/");
		BundleDeployHelper.deployBundle(
			endpointURL,
			bundleArchive,
			"bundle",
			"csdgroup",
			"cicsplex",
			"region",
			"username",
			"password"
		);
	}
	
	@Test
	public void pathNoSlash() throws Exception {
		stubFor(post(urlEqualTo("/foo/deploy"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/plain")
				.withBody("Some content")
			)
		);
		
		File bundleArchive = new File(bundleFilePath);
		
		URI endpointURL = new URI(wireMockRule.baseUrl() + "/foo");
		BundleDeployHelper.deployBundle(
			endpointURL,
			bundleArchive,
			"bundle",
			"csdgroup",
			"cicsplex",
			"region",
			"username",
			"password"
		);
	}
	
	@Test
	public void pathEndsWithSlash() throws Exception {
		stubFor(post(urlEqualTo("/foo/deploy"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/plain")
				.withBody("Some content")
			)
		);
		
		File bundleArchive = new File(bundleFilePath);
		
		URI endpointURL = new URI(wireMockRule.baseUrl() + "/foo/");
		BundleDeployHelper.deployBundle(
			endpointURL,
			bundleArchive,
			"bundle",
			"csdgroup",
			"cicsplex",
			"region",
			"username",
			"password"
		);
	}
}
