package com.ibm.cics.cbmp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

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

public class TestOsgibundle extends TestJavaBasedBundlePart {

	@Override
	protected JavaBasedBundlePart getBundlePart() {
		return new Osgibundle(new SystemStreamLog());
	}
	
	@Override
	protected void assertBundlePartContents(String contents) {
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<"+b.getBundlePartFileExtension()+" jvmserver=\"myjvms\" symbolicname=\"myname\" version=\"1.0.0.somegeneratedqualifier\"/>\n",contents);
	}
	
	@Override
	protected org.apache.maven.artifact.Artifact mockMavenArtifact(String name, String version, String type) throws IOException {
		Artifact mavenArtifact = super.mockMavenArtifact(name, version, type);
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().putValue("Bundle-Version", "1.0.0.somegeneratedqualifier");
		new JarOutputStream(new FileOutputStream(mavenArtifact.getFile()), manifest).close();

		return mavenArtifact;
	}
	
	protected org.apache.maven.artifact.Artifact mockEarlyMavenArtifact(String name, String version, String type) throws IOException {
		org.apache.maven.artifact.Artifact mavenArtifact = mock(org.apache.maven.artifact.Artifact.class);
		when(mavenArtifact.getArtifactId()).thenReturn(name);
		when(mavenArtifact.getVersion()).thenReturn(version);
		when(mavenArtifact.getType()).thenReturn(type);
		

		Path tempSourcePath = Files.createTempDirectory("endp");
		File tempSourceFile = tempSourcePath.toFile();
		tempSourceFile.deleteOnExit();
		new File(tempSourceFile, "META-INF").mkdir();
		when(mavenArtifact.getFile()).thenReturn(tempSourceFile);
		
		
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().putValue("Bundle-Version", "1.0.0.somegeneratedqualifier");
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(tempSourceFile, "META-INF/MANIFEST.MF")))) {
			manifest.write(os);
		};

		return mavenArtifact;
	}
	
	@Override
	protected File getBundlePartFile(File workDir) {
		return new File(workDir, "myname_1.0.0.somegeneratedqualifier."+b.getBundlePartFileExtension());
	}
	
	protected File expectedContentFile(File workDir) {
		return new File(workDir, "myname_1.0.0.somegeneratedqualifier.type");
	}
	

	
	/**
	 * When the bundle part is created, the full dependency JAR may not yet be built. Check that the Bundle-Version
	 * can be picked up from the MANIFEST.MF put in the target/classes/META-INF folder by plugins like bnd-maven-plugin
	 */
	@Test
	public void writeBundlePartBeforeJarCreated() throws IOException {
		b.setJvmserver("myjvms");
		org.apache.maven.artifact.Artifact mavenArtifact = mockEarlyMavenArtifact("myname", "1.0.0-SNAPSHOT", "type");
		File workDir = createTempDir();
		
		b.writeBundlePart(workDir, mavenArtifact, null);
		
		File bundlePartFile = getBundlePartFile(workDir);
		String contents = new String(Files.readAllBytes(bundlePartFile.toPath()));

		assertBundlePartContents(contents);

		FileUtils.deleteDirectory(workDir);
	}

}
