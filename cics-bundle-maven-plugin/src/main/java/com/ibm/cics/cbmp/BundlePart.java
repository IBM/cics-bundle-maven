package com.ibm.cics.cbmp;

import java.io.File;

import org.apache.maven.artifact.Artifact;

public abstract class BundlePart {

	private com.ibm.cics.cbmp.Artifact artifact;

	public void setArtifact(com.ibm.cics.cbmp.Artifact artifact) {
		this.artifact = artifact;	
	}
	
	public boolean matches(Artifact a) {
		return artifact.matches(a);
	}

	public abstract Define writeContent(File workDir, Artifact a) throws MojoExecutionRuntimeException;

}
