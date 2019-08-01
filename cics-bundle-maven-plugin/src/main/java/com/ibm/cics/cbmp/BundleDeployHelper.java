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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.stream.Collectors;

import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BundleDeployHelper {
	
	public static void deployBundle(URI endpointURL, File bundleArchive, String bundleName, String csdGroup, String cicsplexName, String regionName, String username, String password) throws BundleDeployException, IOException {
		MultipartEntityBuilder mpeb = MultipartEntityBuilder.create();
		mpeb.addPart("bundleArchive", new FileBody(bundleArchive, ContentType.create("application/cics-bundle")));
		mpeb.addPart("bundleName", new StringBody(bundleName, ContentType.TEXT_PLAIN));
		mpeb.addPart("csdGroup", new StringBody(csdGroup, ContentType.TEXT_PLAIN));
		mpeb.addPart("cicsplexName", new StringBody(cicsplexName, ContentType.TEXT_PLAIN));
		mpeb.addPart("regionName", new StringBody(regionName, ContentType.TEXT_PLAIN));
		
		
		String path = endpointURL.getPath();
		if (path == null) {
			path = "";
		} else if (!path.endsWith("/")) {
			path = path + "/";
		}
		
		path = path + "deploy";
		
		URI target;
		try {
			target = new URI(
				endpointURL.getScheme(),
				endpointURL.getUserInfo(),
				endpointURL.getHost(),
				endpointURL.getPort(),
				path,
				endpointURL.getQuery(),
				endpointURL.getFragment()
			);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		
		HttpPost httpPost = new HttpPost(target);
		HttpEntity httpEntity = mpeb.build();
		httpPost.setEntity(httpEntity);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		String credentials = username + ":" + password;
		String encoding = Base64.getEncoder().encodeToString(credentials.getBytes());
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
		
		if (!bundleArchive.exists()) {
			throw new BundleDeployException("Bundle does not exist: '" + bundleArchive + "'");
		}
		
		HttpResponse response = httpClient.execute(httpPost);
		StatusLine responseStatus = response.getStatusLine();
		if (responseStatus.getStatusCode() != 200) {
			String contentType = response.getHeaders("Content-Type")[0].getValue();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String responseContent = bufferedReader.lines().collect(Collectors.joining());
			if (contentType == null) {
				throw new BundleDeployException("Http response: " + responseStatus);					
			} else if (contentType.equals("application/xml")) {
				//liberty level error
				throw new BundleDeployException(responseContent);
			} else if (contentType.equals("application/json")) {
				//error from deploy endpoint
				ObjectMapper objectMapper = new ObjectMapper();
				String responseMessage = objectMapper.readTree(responseContent).get("message").asText();
				throw new BundleDeployException(responseMessage);
			} else {
				//CICS level error
				throw new BundleDeployException(responseContent);
			}
		}
	}

}
