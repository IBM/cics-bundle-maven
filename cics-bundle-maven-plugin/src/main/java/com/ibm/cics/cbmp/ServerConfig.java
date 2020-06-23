package com.ibm.cics.cbmp;

import java.net.URI;

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

public class ServerConfig {
	
	private String cicsplex;
	private String region;
	private URI endpointUrl;
	private String username;
	private char[] password;
	private boolean allowSelfSignedCertificate;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	public void setCicsplex(String cicsplex) {
		this.cicsplex = cicsplex;
	}
	
	public void setRegion(String region) {
		this.region = region;
	}
	
	public void setEndpointUrl(URI endpointUrl) {
		this.endpointUrl = endpointUrl;
	}
	
	public void setAllowSelfSignedCertificate(boolean allowSelfSignedCertificate) {
		this.allowSelfSignedCertificate = allowSelfSignedCertificate;
	}
	public String getCicsplex() {
		return cicsplex;
	}
	
	public String getRegion() {
		return region;
	}
	
	public URI getEndpointUrl() {
		return endpointUrl;
	}
	
	public boolean isAllowSelfSignedCertificate() {
		return allowSelfSignedCertificate;
	}

}
