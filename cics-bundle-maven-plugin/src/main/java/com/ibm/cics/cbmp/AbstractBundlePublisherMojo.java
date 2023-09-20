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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.buildhelper.versioning.VersionInformation;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.ibm.cics.bundle.parts.BundlePublisher;
import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;

public abstract class AbstractBundlePublisherMojo extends AbstractMojo {

	protected static final String EAR = "ear";
	protected static final String WAR = "war";
	protected static final String JAR = "jar";
	protected static final String EBA = "eba";

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
	protected File baseDir;
	
	@Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
	protected File buildDir;
	
	/**
	 * The directory containing bundle parts to be included in the CICS bundle. 
	 * This path is relative to `src/main/`
	 * Specifying this parameter overrides the default path of `bundleParts`.
	 */
	@Parameter(property = "cicsbundle.bundlePartsDirectory", defaultValue = "bundleParts", required = false)
	private String bundlePartsDirectory;

	
	@Component
	protected BuildContext buildContext;
	
	protected BundlePublisher getBundlePublisher() throws MojoExecutionException {
		BundlePublisher bundlePublisher = (BundlePublisher) project.getContextValue(BundlePublisher.class.getName());
		if (bundlePublisher == null) {
			bundlePublisher = createBundlePublisher();
			project.setContextValue(BundlePublisher.class.getName(), bundlePublisher);
		}
		
		return bundlePublisher;
	}

	private BundlePublisher createBundlePublisher() throws MojoExecutionException {
		VersionInformation v = new VersionInformation(project.getVersion());
		BundlePublisher bundlePublisher = new BundlePublisher(
			getWorkDir(),
			project.getArtifactId(),
			v.getMajor(),
			v.getMinor(),
			v.getPatch()
		);
		
		//Notify the build context of file changes
		bundlePublisher.setFileChangeListener(path -> buildContext.refresh(path.toFile()));
		
		initBundlePublisher(bundlePublisher);
		return bundlePublisher;
	}
	
	protected abstract Path getWorkDir();
	
	protected void initBundlePublisher(BundlePublisher bundlePublisher) throws MojoExecutionException {
		addStaticBundleResources(bundlePublisher);
	};

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

}
