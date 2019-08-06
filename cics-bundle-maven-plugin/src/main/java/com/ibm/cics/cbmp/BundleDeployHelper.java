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

import org.apache.http.Header;
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
	
	public static void deployBundle(URI endpointURL, File bundle, String bunddef, String csdgroup, String cicsplex, String region, String username, String password) throws BundleDeployException, IOException {
		MultipartEntityBuilder mpeb = MultipartEntityBuilder.create();
		mpeb.addPart("bundle", new FileBody(bundle, ContentType.create("application/zip")));
		mpeb.addPart("bunddef", new StringBody(bunddef, ContentType.TEXT_PLAIN));
		mpeb.addPart("csdgroup", new StringBody(csdgroup, ContentType.TEXT_PLAIN));
		mpeb.addPart("cicsplex", new StringBody(cicsplex, ContentType.TEXT_PLAIN));
		mpeb.addPart("region", new StringBody(region, ContentType.TEXT_PLAIN));
		
		
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
		
		if (!bundle.exists()) {
			throw new BundleDeployException("Bundle does not exist: '" + bundle + "'");
		}
		
		
		
		HttpResponse response = httpClient.execute(httpPost);
		StatusLine responseStatus = response.getStatusLine();
		Header[] contentTypeHeaders = response.getHeaders("Content-Type");
		String contentType;
		if (contentTypeHeaders.length != 1) {
			contentType = null;
		} else {
			contentType = contentTypeHeaders[0].getValue();
		}
		
		if (responseStatus.getStatusCode() != 200) {
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
