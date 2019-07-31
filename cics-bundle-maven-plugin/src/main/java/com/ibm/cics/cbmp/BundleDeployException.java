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
