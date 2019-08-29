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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.buildhelper.versioning.VersionInformation;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.ibm.cics.bundle.parts.BundlePublisher;
import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;

public abstract class AbstractCICSBundleMojo extends AbstractMojo {

	protected static final String EAR = "ear";
	protected static final String WAR = "war";
	protected static final String JAR = "jar";

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
	protected File baseDir;
	
	@Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
	protected File buildDir;

	@Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}", required = true, readonly = true)
	protected File workDir;

	@Parameter(required = false, readonly = false)
	protected String classifier;

	@Parameter(required = false)
	protected List<BundlePartBinding> bundleParts = Collections.emptyList();

	@Parameter(defaultValue = "MYJVMS", required = false, readonly = false)
	private String defaultjvmserver;

	@Component
	protected BuildContext buildContext;

	@Component
	protected MavenProjectHelper projectHelper;

	String getDefaultJVMServer() {
		return defaultjvmserver;
	}
	
	protected BundlePublisher initBundlePublisher() throws MojoExecutionException {
		VersionInformation v = new VersionInformation(project.getVersion());
		BundlePublisher bundlePublisher = new BundlePublisher(
			workDir.toPath(),
			project.getArtifactId(),
			v.getMajor(),
			v.getMinor(),
			v.getPatch(),
			v.getBuildNumber()
		);
		
		//Notify the build context of file changes
		bundlePublisher.setFileChangeListener(path -> buildContext.refresh(path.toFile()));
		
		ArrayList<Artifact> artifacts = new ArrayList<>(project.getArtifacts());
		addExplicitBundleParts(bundlePublisher, artifacts);
		addAutoBundleParts(bundlePublisher, artifacts);
		addStaticBundleResources(bundlePublisher);
		return bundlePublisher;
	}

	private void addStaticBundleResources(BundlePublisher bundlePublisher) throws MojoExecutionException {
		//Add bundle parts for any resources
		Path basePath = baseDir.toPath();
		Path bundlePartSource = basePath.resolve("src/main/resources");
		getLog().info("Gathering bundle parts from " + basePath.relativize(bundlePartSource));
		
		if (Files.exists(bundlePartSource)) {
			if (Files.isDirectory(bundlePartSource)) {
				try {
					List<Path> paths = Files
						.walk(bundlePartSource)
						.filter(Files::isRegularFile)
						.collect(Collectors.toList());
					
					for (Path toAdd : paths) {
						try {
							bundlePublisher.addStaticResource(
								bundlePartSource.relativize(toAdd),
								() -> Files.newInputStream(toAdd)
							);
						} catch (PublishException e) {
							throw new MojoExecutionException("Failure adding static resource " + toAdd + ": " + e.getMessage(), e);
						}
					}
				} catch (IOException e) {
					throw new MojoExecutionException("Failure adding static resources", e);
				}
			} else {
				throw new MojoExecutionException("Static bundle resources directory " + bundlePartSource + "wasn't a directory");
			}
		} else {
			//Ignore if it doesn't exist
		}
	}

	private void addAutoBundleParts(BundlePublisher bundlePublisher, ArrayList<Artifact> artifacts)
			throws MojoExecutionException {
		//For remaining artifacts, find any that are auto-bundle-able
		getLog().info("Finding project dependencies that can be automatically included in the bundle");
		for (Artifact artifact : artifacts) {
			BundlePartBinding bundlePartBinding = getDefaultBundlePartBinding(artifact);
			if (bundlePartBinding != null) {
				addBundleResourceForArtifact(bundlePublisher, artifact, bundlePartBinding);
			}
		}
	}

	private void addExplicitBundleParts(BundlePublisher bundlePublisher, ArrayList<Artifact> artifacts)
			throws MojoExecutionException {
		//From all artifacts, find any that are explicitly bound to specific bundle parts
		getLog().info("Adding explicitly configured bundle parts");
		ArrayList<BundlePartBinding> bindings = new ArrayList<>(bundleParts);
		Iterator<Artifact> artifactsIterator = artifacts.iterator();
		artifactsLoop: while (artifactsIterator.hasNext()) {
			Artifact artifact = artifactsIterator.next();
			Iterator<BundlePartBinding> bindingsIterator = bindings.iterator();
			while (bindingsIterator.hasNext()) {
				BundlePartBinding binding = bindingsIterator.next();
				if (binding.matches(artifact)) {
					artifactsIterator.remove();
					bindingsIterator.remove();
					addBundleResourceForArtifact(bundlePublisher, artifact, binding);
					continue artifactsLoop;
				}
			}
		}
		
		//If there are any bindings that didn't match artifacts, then error!
		if (!bindings.isEmpty()) {
			throw new MojoExecutionException("Some bundle part overrides did not correspond to any of the project artifacts: " + bindings.toString());
		}
	}

	private void addBundleResourceForArtifact(BundlePublisher bundlePublisher, Artifact artifact,
			BundlePartBinding binding) throws MojoExecutionException {
		try {
			bundlePublisher.addResource(binding.toBundlePart(artifact, this));
			logBundlePartAdded(artifact, binding);
		} catch (PublishException e) {
			throw new MojoExecutionException("Error adding bundle resource for artifact " + artifact.toString() + ": " + e.getMessage());
		}
	}

	private void logBundlePartAdded(Artifact artifact, BundlePartBinding bundlePartBinding) {
		getLog().info("Adding " + bundlePartBinding.getClass().getSimpleName() + " bundle part for " + artifact.getId() + "(" + artifact.getType() + ")");
	}

	private BundlePartBinding getDefaultBundlePartBinding(Artifact artifact) {
		switch (artifact.getType()) {
			case WAR: return new Warbundle();
			case EAR: return new Earbundle();
			case JAR: return new Osgibundle();
			default: return null;
		}
	}

}
