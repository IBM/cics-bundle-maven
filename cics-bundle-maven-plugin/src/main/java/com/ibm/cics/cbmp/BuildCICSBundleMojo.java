package com.ibm.cics.cbmp;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.ibm.cics.bundle.parts.BundlePublisher;
import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;

@Mojo(name = "build", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.COMPILE)
public class BuildCICSBundleMojo extends AbstractCICSBundleMojo {
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Running CICS Bundle build");
		
		getLog().debug(buildContext.isIncremental() ? "Build is incremental" : "Build is full");
		
		if (!shouldBuild()) {
			getLog().debug("No changes detected. Will not continue build.");
			return;
		}
		
		if (workDir.exists()) {
			getLog().debug("Deleting " + workDir);
			try {
				FileUtils.deleteDirectory(workDir);
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to delete CICS bundle output directory " + workDir, e);
			}
		}
		
		workDir.mkdirs();
		
		BundlePublisher bundlePublisher = initBundlePublisher();
		
		try {
			bundlePublisher.publishResources();
		} catch (PublishException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
		
		getLog().info("Refreshing " + workDir);
		buildContext.refresh(workDir);
	}

	private boolean shouldBuild() {
		return !buildContext.isIncremental() || 
				// Expand this list of locations as we become interested in them
				buildContext.hasDelta("pom.xml");
	}

}
