package com.ibm.cics.cbmp;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

public abstract class AbstractNameableJavaBundlePartBinding extends AbstractJavaBundlePartBinding {
	
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	protected void applyDefaults(Artifact artifact, AbstractCICSBundleMojo mojo) throws MojoExecutionException {
		if (StringUtils.isEmpty(name)) {
			name = artifact.getArtifactId() + "-" + artifact.getVersion();
		}
		super.applyDefaults(artifact, mojo);
	}
	
}
