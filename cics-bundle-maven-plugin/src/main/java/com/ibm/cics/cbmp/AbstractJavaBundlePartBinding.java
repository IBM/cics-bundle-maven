package com.ibm.cics.cbmp;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

public abstract class AbstractJavaBundlePartBinding extends BundlePartBinding {

	private String jvmserver;
	
	public AbstractJavaBundlePartBinding() {
	}
	
	public String getJvmserver() {
		return jvmserver;
	}
	
	public void setJvmserver(String jvmserver) {
		this.jvmserver = jvmserver;
	}
	
	protected void applyDefaults(Artifact artifact, AbstractCICSBundleMojo mojo) throws MojoExecutionException {
		if (StringUtils.isEmpty(jvmserver)) {
			String defaultJVMServer = mojo.getDefaultJVMServer();
			if (StringUtils.isEmpty(defaultJVMServer)) {
				throw new MojoExecutionException("Bundle part for artifact " + artifact + " did not specify a JVM server explicitly, and no default was configured");
			} else {
				jvmserver = defaultJVMServer;
			}
		}
	}
}
