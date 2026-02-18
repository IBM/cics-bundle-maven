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

import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EarbundleTest extends AbstractJavaBundlePartBindingTestCase {

	@Override
	protected AbstractJavaBundlePartBinding createBinding() {
		return new Earbundle();
	}

	@Override
	protected String getRootElementName() {
		return "earbundle";
	}

	@Test
	public void addCicsAllAuthenticatedRoleFalse() throws Exception {
		((Earbundle) binding).setAddCicsAllAuthenticatedRole(false);
		Map<String, String> attrs = new HashMap<>();
		attrs.put("addCICSAllAuth", "false");
		setOtherExpectedAttributes(attrs);
		
		assertBundleResources();
	}

	@Test
	public void libertyAppConfigFile() throws Exception {
		File configFile = new File("server.xml");
		((Earbundle) binding).setLibertyAppConfigFile(configFile);
		Map<String, String> attrs = new HashMap<>();
		attrs.put("appConfigFile", "server.xml");
		setOtherExpectedAttributes(attrs);
		
		assertBundleResources();
	}

	@Test
	public void bothNewProperties() throws Exception {
		((Earbundle) binding).setAddCicsAllAuthenticatedRole(false);
		File configFile = new File("server.xml");
		((Earbundle) binding).setLibertyAppConfigFile(configFile);
		Map<String, String> attrs = new HashMap<>();
		attrs.put("addCICSAllAuth", "false");
		attrs.put("appConfigFile", "server.xml");
		setOtherExpectedAttributes(attrs);
		
		assertBundleResources();
	}

}
