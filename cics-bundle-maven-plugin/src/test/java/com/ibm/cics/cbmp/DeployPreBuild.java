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

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.IOException;
import java.io.InputStream;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.common.io.ByteStreams;

public class DeployPreBuild {
	
	private static byte[] bundleBinary = getBundleBinary();
	private static WireMockServer wireMockServer;
	
	public enum Protocol {
		HTTP, HTTPS
	}
	
	static WireMockServer setupWiremock(int port) {
		
		return setupWiremock(port, Protocol.HTTP);
	}
	
	static WireMockConfiguration withProtocol(WireMockConfiguration config, Protocol protocol, int port) {
		switch (protocol) {
		case HTTP:
			return config.port(port);
		case HTTPS:
			return config.httpsPort(port);
		default:
			throw new RuntimeException("Unsupported protocol " + protocol + " when starting Wiremock");
		}
	}
	
	static WireMockServer setupWiremock(int port, Protocol protocol) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		
		try {
			Thread.currentThread().setContextClassLoader(WireMock.class.getClassLoader());
			wireMockServer = new WireMockServer(withProtocol(WireMockConfiguration.options(), protocol, port));
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}
		
		wireMockServer.start();
		
		wireMockServer
			.stubFor(
				post(urlEqualTo("/managedcicsbundles"))
					.withMultipartRequestBody(
						aMultipart()
							.withName("cicsplex")
							.withBody(equalTo("cicsplex")))
					.withMultipartRequestBody(
						aMultipart()
							.withName("region")
							.withBody(equalTo("region")))
					.withMultipartRequestBody(
						aMultipart()
							.withName("bunddef")
							.withBody(equalTo("bundle")))
					.withMultipartRequestBody(
						aMultipart()
							.withName("csdgroup")
							.withBody(equalTo("BAR")))
					.withMultipartRequestBody(
						aMultipart()
							.withName("bundle")
							.withBody(WireMock.binaryEqualTo(bundleBinary)))
					.willReturn(
						aResponse()
							.withStatus(200)
							.withHeader("Content-Type", "text/plain")
							.withBody("Some content")
					)
			);

		return wireMockServer;
	}

	static WireMockServer setupWiremockNoCICSplexRegion(int port) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(WireMock.class.getClassLoader());
			wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}

		wireMockServer.start();

		wireMockServer
			.stubFor(
				post(urlEqualTo("/managedcicsbundles"))
					.withMultipartRequestBody(
						aMultipart()
							.withName("bunddef")
							.withBody(equalTo("bundle")))
					.withMultipartRequestBody(
						aMultipart()
							.withName("csdgroup")
							.withBody(equalTo("BAR")))
					.withMultipartRequestBody(
						aMultipart()
							.withName("bundle")
							.withBody(WireMock.binaryEqualTo(bundleBinary)))
					.willReturn(
						aResponse()
							.withStatus(400)
							.withHeader("Content-Type", "text/plain")
							.withBody("com.ibm.cics.bundle.deploy.BundleDeployException: Some of the supplied parameters were invalid:\n" +
									"   - cicsplex: CICSplex  could not be found")
					)
		);

		return wireMockServer;
	}


	static WireMockServer setupSMSSWiremock(int port) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(WireMock.class.getClassLoader());
			wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}

		wireMockServer.start();

		wireMockServer
			.stubFor(
				post(urlEqualTo("/managedcicsbundles"))
					.withMultipartRequestBody(
						aMultipart()
							.withName("bunddef")
							.withBody(equalTo("bundle")))
				.withMultipartRequestBody(
						aMultipart()
							.withName("csdgroup")
							.withBody(equalTo("BAR")))
				.withMultipartRequestBody(
						aMultipart()
							.withName("bundle")
							.withBody(WireMock.binaryEqualTo(bundleBinary)))
				.willReturn(
						aResponse()
							.withStatus(200)
							.withHeader("Content-Type", "text/plain")
							.withBody("Some content")
					)
			);
		return wireMockServer;
	}


	/*
	 * Used by test-reactor-war-deploy which builds the bundle and then tests deploying it, so we can't check for a specific binary for the bundle
	 */
	static WireMockServer setupWiremockWithoutBinary(int port) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		
		try {
			Thread.currentThread().setContextClassLoader(WireMock.class.getClassLoader());
			wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}
		
		wireMockServer.start();
		
		wireMockServer
			.stubFor(
				post(urlEqualTo("/managedcicsbundles"))
					.withMultipartRequestBody(
						aMultipart()
							.withName("cicsplex")
							.withBody(equalTo("cicsplex")))
					.withMultipartRequestBody(
						aMultipart()
							.withName("region")
							.withBody(equalTo("region")))
					.withMultipartRequestBody(
							aMultipart()
								.withName("bunddef")
								.withBody(equalTo("bundle")))
					.withMultipartRequestBody(
							aMultipart()
								.withName("csdgroup")
								.withBody(equalTo("BAR")))
					.withMultipartRequestBody(
							aMultipart()
								.withName("bundle"))
					.willReturn(
						aResponse()
							.withStatus(200)
							.withHeader("Content-Type", "text/plain")
							.withBody("Some content")
					)
			);
		return wireMockServer;
	}
	
	private static byte[] getBundleBinary() {
		try {
			InputStream fileInputStream = DeployPreBuild.class.getClassLoader().getResourceAsStream("test-app-bundle-0.0.1-SNAPSHOT.zip");
			byte[] byteArray = ByteStreams.toByteArray(fileInputStream);
			fileInputStream.close();
			return byteArray;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
