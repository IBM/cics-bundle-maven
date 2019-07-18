package com.ibm.cics.cbmp;

import org.apache.maven.plugin.logging.Log;

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

public class Earbundle extends JavaBasedBundlePart {
	
	private static final String TYPE = BundleConstants.NS + "/EARBUNDLE";
	
	public Earbundle(Log log) {
		super(log);
	}
	
	@Override
	protected String getBundlePartFileExtension() {
		return "earbundle";
	}

	@Override
	protected String getType() {
		return TYPE;
	}
	
}
