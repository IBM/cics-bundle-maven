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

import org.apache.maven.artifact.Artifact;

import com.ibm.cics.bundlegen.Define;

public abstract class BundlePart {

	private com.ibm.cics.cbmp.Artifact artifact;

	private String name;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setArtifact(com.ibm.cics.cbmp.Artifact artifact) {
		this.artifact = artifact;	
	}
	
	public boolean matches(Artifact a) {
		return artifact.matches(a);
	}

	protected static String getMavenArtifactName(Artifact a) {
		return a.getArtifactId() + "-" + a.getVersion();
	}

	protected abstract String getBundlePartFileExtension();
	
	protected abstract String getType();

	public abstract void collectContent(File workDir, Artifact a, FileChangeListener l) throws MojoExecutionRuntimeException;

	public abstract Define writeBundlePart(File workDir, Artifact a, FileChangeListener l) throws MojoExecutionRuntimeException;
	
}

@FunctionalInterface
interface FileChangeListener {
	public void notifyFileChange(File file);
}