package com.ibm.cics.cbmp;

/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2026 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import com.ibm.cics.bundle.parts.BundlePublisher.PublishException;

/**
 * Abstract base class for bundle mojos that support Liberty web applications (WAR and EAR).
 * Adds support for Liberty-specific configuration properties.
 */
public abstract class AbstractBundleWebAppMojo extends AbstractBundleJavaMojo {

	/**
	 * Whether to add the <code>cicsAllAuthenticated</code> role to the application.
	 * Defaults to true.
	 */
	@Parameter(property = "cicsbundle.addCicsAllAuthenticatedRole", defaultValue = "true")
	protected boolean addCicsAllAuthenticatedRole;

	/**
	 * Path to a Liberty server.xml snippet containing a single <code>>application<</code> element
     * to add to the application when defined to Liberty.
	 * The path is relative to the project base directory.
	 */
	@Parameter(property = "cicsbundle.libertyAppConfigFile")
	protected File libertyAppConfigFile;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// Validate libertyAppConfigFile if specified
		if (libertyAppConfigFile != null && !libertyAppConfigFile.exists()) {
			throw new MojoExecutionException("Liberty app config file does not exist: " + libertyAppConfigFile);
		}

		// Call parent execute which will call getBundlePartBinding()
		super.execute();
	}

	/**
	 * Configure the bundle part binding with Liberty-specific properties.
	 * Subclasses must implement this to return the appropriate binding type.
	 */
	@Override
	protected abstract AbstractJavaBundlePartBinding getBundlePartBinding();

	/**
	 * Get the configured value for addCicsAllAuthenticatedRole.
	 */
	protected boolean isAddCicsAllAuthenticatedRole() {
		return addCicsAllAuthenticatedRole;
	}

	/**
	 * Get the configured Liberty app config file.
	 */
	protected File getLibertyAppConfigFile() {
		return libertyAppConfigFile;
	}

}