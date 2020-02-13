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

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.cics.bundle.parts.BundleResource;

public abstract class BundlePartBinding {

	/**
	 * This object is instantiated by Maven using the values in the pom that
	 * are provided by the user when they explicitly configure a bundle part
	 * using <bundleParts>.
	 */
	private com.ibm.cics.cbmp.Artifact artifact;
	
	/**
	 * This is the actual Maven artifact. If using an explicitly configured
	 * bundle part, this will be set by matching to the values in the above
	 * artifact. Otherwise it will just be set directly.
	 */
	protected org.apache.maven.artifact.Artifact resolvedArtifact;
	
	public void setArtifact(com.ibm.cics.cbmp.Artifact artifact) {
		this.artifact = artifact;
	}
	
	public void setResolvedArtifact(org.apache.maven.artifact.Artifact resolvedArtifact) {
		this.resolvedArtifact = resolvedArtifact;
	}
	
	public boolean matches(org.apache.maven.artifact.Artifact target) {
		return artifact.matches(target);
	}

	public final BundleResource toBundlePart(DefaultsProvider mojo) throws MojoExecutionException {
		applyDefaults(mojo);
		return toBundlePartImpl();
	}
	
	protected abstract void applyDefaults(DefaultsProvider mojo) throws MojoExecutionException;
	
	protected abstract BundleResource toBundlePartImpl() throws MojoExecutionException;
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + artifact;
	}
}
