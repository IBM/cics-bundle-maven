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

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class OsgibundleTest extends AbstractJavaBundlePartBindingTestCase {
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Override
	protected AbstractJavaBundlePartBinding createBinding() {
		return new Osgibundle();
	}

	@Override
	protected String getRootElementName() {
		return "osgibundle";
	}
	
	@Before
	public abstract void createArtifactFile() throws IOException;
	
	@Override
	public void nameOverride() throws Exception {
		// OSGi symbolic name can't be overridden, so this name change should have no effect.
		binding.setName("bananas");
		
		assertBundleResources();
	}
}