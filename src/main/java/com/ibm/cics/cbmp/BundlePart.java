package com.ibm.cics.cbmp;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public abstract class BundlePart {

	public abstract Define writeContent(File workDir, MavenProject project) throws MojoExecutionException;

}
