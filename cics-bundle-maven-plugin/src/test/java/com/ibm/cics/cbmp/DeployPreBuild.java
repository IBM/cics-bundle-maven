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
	
	static WireMockServer setupWiremock(int port) {
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
				post(urlEqualTo("/deploy"))
					.withMultipartRequestBody(
						aMultipart()
							.withName("cicsplexName")
							.withBody(equalTo("cicsplex")))
					.withMultipartRequestBody(
						aMultipart()
							.withName("regionName")
							.withBody(equalTo("region")))
					.withMultipartRequestBody(
							aMultipart()
								.withName("bundleName")
								.withBody(equalTo("bundle")))
					.withMultipartRequestBody(
							aMultipart()
								.withName("csdGroup")
								.withBody(equalTo("BAR")))
					.withMultipartRequestBody(
							aMultipart()
								.withName("bundleArchive")
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
	
	private static byte[] getBundleBinary() {
		try {
			InputStream fileInputStream = DeployPreBuild.class.getClassLoader().getResourceAsStream("test-app-bundle-0.0.1-SNAPSHOT.cics-bundle");
			byte[] byteArray = ByteStreams.toByteArray(fileInputStream);
			fileInputStream.close();
			return byteArray;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
