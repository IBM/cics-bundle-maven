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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

public abstract class AbstractJavaBundlePartBinding extends BundlePartBinding {

	private String jvmserver;
	
	public AbstractJavaBundlePartBinding() {
	}
	
	public String getJvmserver() {
		return jvmserver;
	}
	
	public void setJvmserver(String jvmserver) {
		this.jvmserver = jvmserver;
	}
	@Override
	protected void applyDefaults(Artifact artifact, DefaultsProvider defaults) throws MojoExecutionException {
		if (StringUtils.isEmpty(jvmserver)) {
			String defaultJVMServer = defaults.getJVMServer();
			if (StringUtils.isEmpty(defaultJVMServer)) {
				throw new MojoExecutionException("Bundle part for artifact " + artifact + " did not specify a JVM server explicitly, and no default was configured");
			} else {
				jvmserver = defaultJVMServer;
			}
		}
	}
}
