package com.ibm.cics.cbmp;

import java.io.File;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import com.ibm.cics.bundle.parts.BundlePublisher;
import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;

public abstract class AbstractAutoConfigureBundlePublisherMojo extends AbstractBundlePublisherMojo implements DefaultsProvider {

	/**
	 * The default fallback value for the CICS JVM server that will be used for any Java-based artifacts that don't have a JVM server specified.
	 */
	@Parameter(property="cicsbundle.defaultjvmserver", defaultValue = "MYJVMS", required = false, readonly = false)
	private String defaultjvmserver;
	
	@Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}", required = true, readonly = true)
	protected File workDir;

	/**
	 * Specify that a declared dependency should be treated as an EAR, WAR, or JAR bundle part.
	 */
	@Parameter(required = false)
	protected List<BundlePartBinding> bundleParts = Collections.emptyList();
	
	/**
	 * The directory containing bundle parts to be included in the CICS bundle. 
	 * This path is relative to `src/main/`
	 * Specifying this parameter overrides the default path of `bundleParts`.
	 */
	@Parameter(property = "cicsbundle.bundlePartsDirectory", defaultValue = "bundleParts", required = false)
	private String bundlePartsDirectory;

	@Override
	public String getJVMServer() {
		return defaultjvmserver;
	}
	
	@Override
	protected void initBundlePublisher(BundlePublisher bundlePublisher) throws MojoExecutionException {
		ArrayList<Artifact> artifacts = new ArrayList<>(project.getDependencyArtifacts());
		addExplicitBundleParts(bundlePublisher, artifacts);
		addAutoBundleParts(bundlePublisher, artifacts);
		addStaticBundleResources(bundlePublisher);
	}

	private void addStaticBundleResources(BundlePublisher bundlePublisher) throws MojoExecutionException {
		//Add bundle parts for any resources
		Path basePath = baseDir.toPath();
		Path bundlePartSource = basePath.resolve("src/main/" + bundlePartsDirectory);
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
							throw new MojoExecutionException("Failure adding static bundle parts " + toAdd + ": " + e.getMessage(), e);
						}
					}
				} catch (IOException e) {
					throw new MojoExecutionException("Failure adding static bundle parts", e);
				}
			} else {
				throw new MojoExecutionException("Static bundle parts directory " + bundlePartSource + "wasn't a directory");
			}
		} else {
			//Ignore if it doesn't exist
			getLog().info("No non-Java-based bundle parts to add, because bundle parts directory '" + bundlePartsDirectory + "' does not exist");
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

	private void addBundleResourceForArtifact(BundlePublisher bundlePublisher, Artifact artifact, BundlePartBinding binding) throws MojoExecutionException {
		try {
			binding.setResolvedArtifact(artifact);
			bundlePublisher.addResource(binding.toBundlePart(this));
			logBundlePartAdded(artifact, binding);
		} catch (PublishException e) {
			throw new MojoExecutionException("Error adding bundle resource for artifact " + artifact.toString() + ": " + e.getMessage());
		}
	}

	private void logBundlePartAdded(Artifact artifact, BundlePartBinding bundlePartBinding) {
		getLog().info("Adding " + bundlePartBinding.getClass().getSimpleName() + " bundle part for " + artifact.getId() + "(" + artifact.getType() + ")");
	}

	private static BundlePartBinding getDefaultBundlePartBinding(Artifact artifact) {
		switch (artifact.getType()) {
			case WAR: return new Warbundle();
			case EAR: return new Earbundle();
			case JAR: return new Osgibundle();
			case EBA: return new Ebabundle();
			default: return null;
		}
	}
	
	@Override
	protected Path getWorkDir() {
		return workDir.toPath();
	}

}
