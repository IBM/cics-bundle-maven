package com.ibm.cics.cbmp;

/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2018 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.ibm.cics.bundle.parts.BundlePublisher;
import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;

@Mojo(name = "package", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.PACKAGE)
public class PackageCICSBundleMojo extends AbstractCICSBundleMojo {	
	
	private static final String CICS_BUNDLE_EXTENSION = "zip";
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Running CICS Bundle package");

		BundlePublisher bundlePublisher = initBundlePublisher();
		
		try {
			bundlePublisher.publishDynamicResources();
			
			File cicsBundleArchive = new File(buildDir, project.getArtifactId() + "-" + project.getVersion() + (classifier != null ? "-" + classifier : "") + "." + CICS_BUNDLE_EXTENSION);

			bundlePublisher.createArchive(cicsBundleArchive.toPath());
			
			if (classifier != null) {
				projectHelper.attachArtifact(project, CICS_BUNDLE_EXTENSION, classifier, cicsBundleArchive);
			} else {
				File artifactFile = project.getArtifact().getFile();
				if (artifactFile != null && artifactFile.isFile()) {
					//We already attached an artifact to this project in another mojo, don't override it!
					throw new MojoExecutionException("Set classifier when there's already an artifact attached, to prevent overwriting the main artifact");
				} else {
					project.getArtifact().setFile(cicsBundleArchive);
				}
			}
			
			getLog().info("Refreshing " + cicsBundleArchive);
			buildContext.refresh(cicsBundleArchive);
		} catch (PublishException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	@Override
	String getDefaultJVMServer() {
		return defaultjvmserver;
	}

}
