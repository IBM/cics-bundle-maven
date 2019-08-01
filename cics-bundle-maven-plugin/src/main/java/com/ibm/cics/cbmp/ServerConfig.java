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
	
	private String cicsplexName;
	private String regionName;
	private URI endpointUrl;
	
	public void setCicsplexName(String cicsplexName) {
		this.cicsplexName = cicsplexName;
	}
	
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
	public void setEndpointUrl(URI endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	public String getCicsplexName() {
		return cicsplexName;
	}
	
	public String getRegionName() {
		return regionName;
	}
	
	public URI getEndpointUrl() {
		return endpointUrl;
	}

}
