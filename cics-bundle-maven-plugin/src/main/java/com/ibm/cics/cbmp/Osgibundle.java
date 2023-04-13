package com.ibm.cics.cbmp;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.cics.bundle.parts.OsgiBundlePart;

/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2019 - 2023 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

public class Osgibundle extends AbstractJavaBundlePartBinding {
	
	protected String symbolicName;
	protected String osgiVersion;
	
	@Override
	protected void applyDefaults(DefaultsProvider defaults) throws MojoExecutionException {
		super.applyDefaults(defaults);
		
		try {
			/**
			 * For other bundle parts, symbolic name can be anything, but osgi bundle parts must use the symbolic name
			 * that is in the manifest. This is mandatory so fail if not found in manifest.
			 */
			symbolicName = OsgiBundlePart.getBundleSymbolicName(resolvedArtifact.getFile());
			if (symbolicName == null) {
				throw new MojoExecutionException("Error reading Bundle-SymbolicName from OSGi manifest file");
			}

			/**
			 * OSGi version is optional, so use default value if not found in manifest.
			 */
			osgiVersion = OsgiBundlePart.getBundleVersion(resolvedArtifact.getFile());
			if (osgiVersion == null) {
				osgiVersion = "0.0.0";
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Error reading headers from OSGi manifest file", e);
		}
	}
	
	@Override
	public OsgiBundlePart toBundlePartImpl() throws MojoExecutionException {
		OsgiBundlePart bundlePart = new OsgiBundlePart(
			getName(),
			symbolicName,
			osgiVersion,
			getJvmserver(),
			resolvedArtifact.getFile()
		);
		bundlePart.setVersionRange(getVersionRange());
		return bundlePart;
	}

	public String getVersionRange() {
		if(resolvedArtifact.getVersionRange() != null && resolvedArtifact.getVersionRange().toString().contains(",")) {
			return resolvedArtifact.getVersionRange().toString();
		} else {
			return "";
		}
	}

}
