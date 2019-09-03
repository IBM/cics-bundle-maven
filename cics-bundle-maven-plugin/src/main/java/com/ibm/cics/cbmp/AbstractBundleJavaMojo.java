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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;

import com.ibm.cics.bundle.parts.BundlePublisher;
import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;
import com.ibm.cics.bundle.parts.BundleResource;

public abstract class AbstractBundleJavaMojo extends AbstractBundlePublisherMojo {

	@Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}-cics-bundle.zip", required = true, readonly = true)
	private File cicsBundleArchive;
	@Parameter(required = true)
	protected String jvmserver;
	@Parameter(defaultValue = "cics-bundle")
	private String classifier;
	@Parameter
	private Artifact artifact;
	@Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
	private File buildDir;
	@Component
	private MavenProjectHelper projectHelper;

	public AbstractBundleJavaMojo() {
		super();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		BundlePublisher bundlePublisher = getBundlePublisher();
		
		org.apache.maven.artifact.Artifact artifact = project.getArtifact();
		
		try {
			bundlePublisher.addResource(getBundlePart(artifact));
			bundlePublisher.publishResources();
		} catch (PublishException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
		
		PackageCICSBundleMojo.createCICSBundle(
			workDir,
			buildDir,
			bundlePublisher,
			classifier,
			project,
			projectHelper
		);
	}

	protected abstract BundleResource getBundlePart(org.apache.maven.artifact.Artifact artifact) throws MojoExecutionException;

}
