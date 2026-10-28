package com.ibm.cics.appdeploy;

/*-
 * #%L
 * CICS AppDeploy Maven Plugin
 * %%
 * Copyright (C) 2026 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Starts a WireMock server that stands in for the Liberty appDeploy endpoint. These methods are called
 * from the prebuild scripts of the integration tests in src/it.
 */
public class UploadPreBuild {

	private static final String UPLOAD_PATH = "/appdeploy/uploadApp";

	/**
	 * Accepts an upload only if it has the expected Authorization header (or none, when expectedAuthorization
	 * is null) and both the applicationXml and warFile parts. Any other request gets a 404, so the build
	 * under test fails.
	 */
	public static WireMockServer setupWiremock(int port, String expectedAuthorization) {
		WireMockServer server = startServer(port, false);

		MappingBuilder upload = post(urlEqualTo(UPLOAD_PATH))
			.withHeader("Authorization", expectedAuthorization == null ? absent() : equalTo(expectedAuthorization))
			.withMultipartRequestBody(
				aMultipart()
					.withName("applicationXml")
					.withHeader("Content-Type", containing("text/xml"))
					.withBody(containing("<application id=\"it-app\"")))
			.withMultipartRequestBody(
				aMultipart()
					.withName("warFile")
					.withHeader("Content-Disposition", containing("filename=\""))
					.withHeader("Content-Type", equalTo("application/octet-stream")))
			.willReturn(aResponse().withStatus(200).withBody("Application deployed"));

		server.stubFor(upload);
		return server;
	}

	/**
	 * Answers every upload with the given status, to test how the plugin reports a rejected upload.
	 */
	public static WireMockServer setupWiremockStatus(int port, int status) {
		WireMockServer server = startServer(port, false);
		server.stubFor(post(urlEqualTo(UPLOAD_PATH)).willReturn(aResponse().withStatus(status).withBody("Rejected by test server")));
		return server;
	}

	/**
	 * Starts an HTTPS-only server that uses WireMock's built-in self-signed certificate, which the plugin does not trust.
	 */
	public static WireMockServer setupWiremockHttps(int port) {
		return startServer(port, true);
	}

	private static WireMockServer startServer(int port, boolean https) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		WireMockServer server;

		try {
			Thread.currentThread().setContextClassLoader(WireMock.class.getClassLoader());
			WireMockConfiguration config = https
				? WireMockConfiguration.options().httpDisabled(true).httpsPort(port)
				: WireMockConfiguration.options().port(port);
			server = new WireMockServer(config);
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}

		server.start();
		return server;
	}
}
