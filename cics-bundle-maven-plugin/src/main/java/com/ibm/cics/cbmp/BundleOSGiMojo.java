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

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.ibm.cics.bundle.parts.BundleResource;
import com.ibm.cics.bundle.parts.OsgiBundlePart;

@Mojo(name = "bundle-osgi", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.VERIFY)
public class BundleOSGiMojo extends AbstractBundleJavaMojo {

	@Override
	protected BundleResource getBundlePart(Artifact artifact) throws MojoExecutionException {
		File osgiBundle = artifact.getFile();
		
		String osgiVersion;
		try {
			osgiVersion = OsgiBundlePart.getBundleVersion(osgiBundle);
		} catch (IOException e) {
			throw new MojoExecutionException("Error reading OSGi bundle version", e);
		}
		if (osgiVersion == null) {
			osgiVersion = OsgiBundlePart.convertMavenVersionToOSGiVersion(artifact.getVersion());
		}
		
		return new OsgiBundlePart(
			artifact.getArtifactId(),
			osgiVersion,
			jvmserver,
			osgiBundle
		);
	}

}
