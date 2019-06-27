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
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

@Mojo(name = "package", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.PACKAGE)
public class PackageCICSBundleMojo extends AbstractCICSBundleMojo {	
	
	private void copyContent(Artifact a) {
		bundleParts
			.stream()
			.filter(bp -> bp.matches(a))
			.findFirst()
			.orElse(getDefaultBundlePart(a))
			.collectContent(workDir, a, f -> buildContext.refresh(f));
	}
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Running CICS Bundle package");

		project
		.getArtifacts()
		.stream()
		.filter(BuildCICSBundleMojo::isArtifactBundleable)
		.forEach(this::copyContent);
		

		File cicsBundle = null;
		try {
			ZipArchiver zipArchiver = new ZipArchiver();
			zipArchiver.addDirectory(workDir);
			cicsBundle = new File(buildDir, project.getArtifactId() + "-" + project.getVersion() + (classifier != null ? "-" + classifier : "") + ".cics-bundle");
			zipArchiver.setDestFile(cicsBundle);
			zipArchiver.createArchive();
		
			if (classifier != null) {
				projectHelper.attachArtifact(project, "cics-bundle", classifier, cicsBundle);
			} else {
				File artifactFile = project.getArtifact().getFile();
				if (artifactFile != null && artifactFile.isFile()) {
					//We already attached an artifact to this project in another mojo, don't override it!
					throw new MojoExecutionException("Set classifier when there's already an artifact attached, to prevent overwriting the main artifact");
				} else {				
					project.getArtifact().setFile(cicsBundle);
				}
			}
		} catch (ArchiverException | IOException e) {
			throw new MojoExecutionException("Failed to create cics bundle archive", e);
		}
		getLog().info("Refreshing "+cicsBundle);
		buildContext.refresh(cicsBundle);
	}

	@Override
	String getDefaultJVMServer() {
		return defaultjvmserver;
	}

}
