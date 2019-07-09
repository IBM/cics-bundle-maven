package com.ibm.cics.cbmp;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class DeployPreBuild {
	
	private static String bundleBinary = "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";
	
	static void setupWiremock() {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		WireMockServer wireMockServer = null;
		try {
			Thread.currentThread().setContextClassLoader(WireMock.class.getClassLoader());
			wireMockServer = new WireMockServer(); //No-args constructor will start on port 8080, no HTTPS
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
	}
	
	
	static void teardownWiremock() {
		WireMock.shutdownServer();
	}

}
