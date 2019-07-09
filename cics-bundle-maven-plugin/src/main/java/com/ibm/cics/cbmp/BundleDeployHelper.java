package com.ibm.cics.cbmp;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

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

public class BundleDeployHelper {
	
	public static void deployBundle(String endpointURL, File bundleArchive, String bundleName, String csdGroup, String cicsplexName, String regionName, String username, String password) throws BundleDeployException {
		MultipartEntityBuilder mpeb = MultipartEntityBuilder.create();
		mpeb.addPart("bundleArchive", new FileBody(bundleArchive, ContentType.MULTIPART_FORM_DATA));
		mpeb.addPart("bundleName", new StringBody(bundleName, ContentType.MULTIPART_FORM_DATA));
		mpeb.addPart("csdGroup", new StringBody(csdGroup, ContentType.MULTIPART_FORM_DATA));
		mpeb.addPart("cicsplexName", new StringBody(cicsplexName, ContentType.MULTIPART_FORM_DATA));
		mpeb.addPart("regionName", new StringBody(regionName, ContentType.MULTIPART_FORM_DATA));
		
		HttpPost httpPost = new HttpPost(endpointURL);
		HttpEntity httpEntity = mpeb.build();
		httpPost.setEntity(httpEntity);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		String credentials = username + ":" + password;
		String encoding = Base64.getEncoder().encodeToString(credentials.getBytes());
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			StatusLine responseStatus = response.getStatusLine();
			if (responseStatus.getStatusCode() != 200) {
				throw new BundleDeployException("Http response: " + responseStatus);
			}
		} catch (IOException e) {
			throw new BundleDeployException(e.getMessage(), e);
		}
	}

}
