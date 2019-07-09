package com.ibm.cics.cbmp;

public class BundleDeployException extends Exception {

	private static final long serialVersionUID = 1L;
	private String fullJsonInfo;
	
	public BundleDeployException(String message) {
		super(message);
	}
	
	public BundleDeployException(String message, Throwable e) {
		super(message, e);
	}
	
	public BundleDeployException(String message, String json) {
		super(message);
		this.fullJsonInfo = json;
	}
	
	

}
