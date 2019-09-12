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
	protected void applyDefaults(Artifact artifact, DefaultsProvider defaults) throws MojoExecutionException {
		if (StringUtils.isEmpty(name)) {
			name = artifact.getArtifactId() + "-" + artifact.getVersion();
		}
		super.applyDefaults(artifact, defaults);
	}
	
}
