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

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractNameableJavaBundlePartBindingTestCase extends AbstractJavaBundlePartBindingTestCase {
	
	@Before
	public void defaultName() {
		setExpectedSymbolicName(artifact.getArtifactId() + "-" + artifact.getVersion());
	}
	
	@Test
	public void nameOverride() throws Exception {
		((AbstractNameableJavaBundlePartBinding) binding).setName("bananas");
		
		setExpectedSymbolicName("bananas");
		assertBundleResources();
	}
	
	@Override
	protected abstract AbstractNameableJavaBundlePartBinding createBinding();
	
}
