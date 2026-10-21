package com.ibm.cics.cbmp;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.cics.bundle.parts.WarBundlePart;

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

public class Warbundle extends AbstractJavaBundlePartBinding {

	protected boolean addCicsAllAuthenticatedRole = true;
	protected File libertyAppConfigFile;
	
	public boolean getAddCicsAllAuthenticatedRole() {
		return addCicsAllAuthenticatedRole;
	}
	
	public void setAddCicsAllAuthenticatedRole(boolean addCicsAllAuthenticatedRole) {
		this.addCicsAllAuthenticatedRole = addCicsAllAuthenticatedRole;
	}
	
	public File getLibertyAppConfigFile() {
		return libertyAppConfigFile;
	}
	
	public void setLibertyAppConfigFile(File libertyAppConfigFile) {
		this.libertyAppConfigFile = libertyAppConfigFile;
	}

	@Override
	public WarBundlePart toBundlePartImpl() throws MojoExecutionException {
		return new WarBundlePart(
			getName(),
			getJvmserver(),
			addCicsAllAuthenticatedRole,
			libertyAppConfigFile,
			resolvedArtifact.getFile()
		);
	}

}
