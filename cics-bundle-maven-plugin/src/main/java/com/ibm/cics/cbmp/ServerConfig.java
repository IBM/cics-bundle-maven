package com.ibm.cics.cbmp;

public class ServerConfig {
	
	private String cicsplexName;
	private String regionName;
	private String enpointUrl;
	
	public void setCicsplexName(String cicsplexName) {
		this.cicsplexName = cicsplexName;
	}
	
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
	public void setEndpointUrl(String enpointUrl) {
		this.enpointUrl = enpointUrl;
	}

	public String getCicsplexName() {
		return cicsplexName;
	}
	
	public String getRegionName() {
		return regionName;
	}
	
	public String getEndpointUrl() {
		return enpointUrl;
	}

}
