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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public abstract class TestJavaBasedBundlePart {
	
	protected abstract JavaBasedBundlePart getBundlePart();

	JavaBasedBundlePart b;
	
	@Before
	public void setUp() {
		b = getBundlePart();
	}

	@Test
	public void jvmServer() {
		assertNull(b.getJvmserver());
		b.setJvmserver("hello");
		assertEquals("hello", b.getJvmserver());
	}
	
	@Test
	public void name() {
		Earbundle b = new Earbundle();
		assertNull(b.getName());
		b.setName("hello");
		assertEquals("hello", b.getName());
	}
	
	protected static File createTempDir() throws IOException {
		File tempSourceFile = File.createTempFile("endp", "tmp");
		tempSourceFile.deleteOnExit();
		File workDir = new File(tempSourceFile.getParent(), getRandomName());
		assertTrue(workDir.mkdir());
		return workDir;
	}
	
	@Test
	public void collectContent() throws IOException {
		b.setName("myname");
		b.setJvmserver("myjvms");
		Artifact a = mock(Artifact.class);
		b.setArtifact(a);
		File tempSourceFile = File.createTempFile("endp", "tmp");
		tempSourceFile.deleteOnExit();
		FileWriter writer = new FileWriter(tempSourceFile);
		writer.write(getRandomLengthString());
		writer.close();
		
		File workDir = createTempDir();
		
		org.apache.maven.artifact.Artifact mavenArtifact = mockMavenArtifact("myname", "1.0.0-SNAPSHOT", "type");
		when(mavenArtifact.getFile()).thenReturn(tempSourceFile);

		// call and verify the callback happens
		final boolean[] happened = { false };
		b.collectContent(workDir, mavenArtifact, f -> { happened[0] = true; assertTrue(f.equals(new File(workDir, "myname.type"))); });
		assertTrue(happened[0]);
		
		assertEquals(tempSourceFile.length(), new File(workDir, "myname.type").length());
		
		FileUtils.deleteDirectory(workDir);
	}

	@Test
	public void collectContentException() {
		b.setName("myname");
		b.setJvmserver("myjvms");
		Artifact a = mock(Artifact.class);
		b.setArtifact(a);
		org.apache.maven.artifact.Artifact mavenArtifact = mockMavenArtifact("myname", "1.0.0-SNAPSHOT", "type");
		when(mavenArtifact.getFile()).thenReturn(new File("/blbaaah"));
		try {
			b.collectContent(new File("Asdjkl"), mavenArtifact, f -> fail("Callback shouldn't have been called"));
			fail("Should have received MojoExecutionRuntimeException accessing files and directories that didn't exist");
		} catch (MojoExecutionRuntimeException e) {
			assertTrue(e.getCause() instanceof IOException);
			// pass!
		}
		

	}
	
	protected static org.apache.maven.artifact.Artifact mockMavenArtifact(String name, String version, String type) {
		org.apache.maven.artifact.Artifact mavenArtifact = mock(org.apache.maven.artifact.Artifact.class);
		when(mavenArtifact.getArtifactId()).thenReturn(name);
		when(mavenArtifact.getVersion()).thenReturn(version);
		when(mavenArtifact.getType()).thenReturn(type);

		return mavenArtifact;
	}
	
	@Test
	public void writeBundlePartNullJvmServer() {
		org.apache.maven.artifact.Artifact mavenArtifact = mockMavenArtifact("myname", "1.0.0-SNAPSHOT", "type");
		try {
			b.writeBundlePart(null, mavenArtifact, null);
			fail("Should have thrown exception with null jvmserver");
		} catch (MojoExecutionRuntimeException e) {
			// pass
		}
	}
	
	@Test
	public void writeBundlePartBlankJvmServer() {
		org.apache.maven.artifact.Artifact mavenArtifact = mockMavenArtifact("myname", "1.0.0-SNAPSHOT", "type");
		b.setJvmserver("");
		try {
			b.writeBundlePart(null, mavenArtifact, null);
			fail("Should have thrown exception with blank jvmserver");
		} catch (MojoExecutionRuntimeException e) {
			// pass
		}
	}
	
	@Test
	public void writeBundlePart() throws IOException {
		b.setJvmserver("myjvms");
		org.apache.maven.artifact.Artifact mavenArtifact = mockMavenArtifact("myname", "1.0.0-SNAPSHOT", "type");
		File workDir = createTempDir();
		
		b.writeBundlePart(workDir, mavenArtifact, null);
		
		File bundlePartFile = new File(workDir, "myname-1.0.0-SNAPSHOT."+b.getBundlePartFileExtension());
		String contents = new String(Files.readAllBytes(bundlePartFile.toPath()));

		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<"+b.getBundlePartFileExtension()+" jvmserver=\"myjvms\" symbolicname=\"myname-1.0.0-SNAPSHOT\"/>\n",contents);

		FileUtils.deleteDirectory(workDir);
	}
	
	public static String getRandomLengthString() {
		Random random = new Random();
		StringBuffer buffer = new StringBuffer("random content ");
		int length = random.nextInt(255);
		for (int i = 0; i < length; i++) {
			buffer.append(" ");
		}
		return buffer.toString();
	}
	
	public static String getRandomName()	{
		Random random = new Random();
		StringBuffer buffer = new StringBuffer("J");
		for (int i = 0; i < 7; i++) {
			buffer.append(random.nextInt(10));
		}
		// just to make sure
		buffer.setLength(8);
		return buffer.toString();
	}
	
}
