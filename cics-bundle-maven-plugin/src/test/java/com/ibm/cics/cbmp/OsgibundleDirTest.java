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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.jar.Manifest;

public class OsgibundleDirTest extends OsgibundleTest {
		
	@Override
	public void createArtifactFile() throws IOException {
		File artifactFile = tempFolder.newFolder("osgibundle");
		when(artifact.getFile()).thenReturn(artifactFile);
		
		File mi = new File(artifactFile, "META-INF");
		assertTrue(mi.mkdirs());
		
		File manifestFile = new File(mi, "MANIFEST.MF");
		
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
		manifest.getMainAttributes().putValue("Bundle-SymbolicName", "some.symbolic.name");
		manifest.getMainAttributes().putValue("Bundle-Version", "1.0.2.7654321");
		
		try (OutputStream os = new FileOutputStream(manifestFile)) {
			manifest.write(os);
		}
		
		setExpectedSymbolicName("some.symbolic.name");
		setOtherExpectedAttributes(Collections.singletonMap("version", "1.0.2.7654321"));
	}
}