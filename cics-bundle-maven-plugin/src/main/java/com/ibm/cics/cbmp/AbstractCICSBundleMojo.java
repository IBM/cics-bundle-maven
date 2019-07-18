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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.sonatype.plexus.build.incremental.BuildContext;

public abstract class AbstractCICSBundleMojo extends AbstractMojo {

	protected static final String EAR = "ear";
	protected static final String WAR = "war";
	protected static final String JAR = "jar";

	private static final Set<String> BUNDLEABLE_TYPES = new HashSet<>(Arrays.asList(WAR, EAR, JAR));

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
	protected File buildDir;

	@Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}", required = true, readonly = true)
	protected File workDir;

	@Parameter(required = false, readonly = false)
	protected String classifier;

	@Parameter(required = false)
	protected List<BundlePart> bundleParts = Collections.emptyList();

	@Parameter(defaultValue = "MYJVMS", required = false, readonly = false)
	protected String defaultjvmserver;

	@Component
	protected BuildContext buildContext;

	@Component
	protected MavenProjectHelper projectHelper;

	public static boolean isArtifactBundleable(Artifact a) {
		return BUNDLEABLE_TYPES.contains(a.getType());
	}

	abstract String getDefaultJVMServer();

	protected BundlePart getDefaultBundlePart(Artifact a) {
		getLog().info("Building bundle part for " + a.getId() + "(" + a.getType() + ")");
		switch (a.getType()) {
			default:
				throw new RuntimeException("Unsupported bundle part type:" + a.getType());
			case WAR: {
				Warbundle warbundle = new Warbundle(getLog());
				warbundle.setJvmserver(getDefaultJVMServer());
				warbundle.setArtifact(new com.ibm.cics.cbmp.Artifact(a));
				return warbundle;
			}
			case EAR: {
				Earbundle earbundle = new Earbundle(getLog());
				earbundle.setJvmserver(getDefaultJVMServer());
				earbundle.setArtifact(new com.ibm.cics.cbmp.Artifact(a));
				return earbundle;
			}
			case JAR: {
				Osgibundle osgibundle = new Osgibundle(getLog());
				osgibundle.setJvmserver(getDefaultJVMServer());
				osgibundle.setArtifact(new com.ibm.cics.cbmp.Artifact(a));
				return osgibundle;
			}
		}
	}

}
