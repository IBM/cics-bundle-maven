package com.ibm.cics.cbmp;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.cics.bundle.parts.BundleResource;

public abstract class BundlePartBinding {

	private com.ibm.cics.cbmp.Artifact artifact;
	
	public void setArtifact(com.ibm.cics.cbmp.Artifact artifact) {
		this.artifact = artifact;	
	}
	
	public boolean matches(Artifact a) {
		return artifact.matches(a);
	}

	public final BundleResource toBundlePart(Artifact artifact, AbstractCICSBundleMojo mojo) throws MojoExecutionException {
		applyDefaults(artifact, mojo);
		return toBundlePartImpl(artifact);
	}
	
	protected abstract void applyDefaults(Artifact artifact, AbstractCICSBundleMojo mojo) throws MojoExecutionException;
	
	protected abstract BundleResource toBundlePartImpl(Artifact artifact) throws MojoExecutionException;
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + artifact;
	}
}