package com.ibm.cics.cbmp;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class OsgibundleTest extends AbstractJavaBundlePartBindingTestCase {
	
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
	public void defaultArtifact() throws IOException {
		//conversion from Maven version
		setExpectedSymbolicName(AbstractJavaBundlePartBindingTestCase.ARTIFACT_ID);
		setOtherExpectedAttributes(Collections.singletonMap("version", "1.0.0.SNAPSHOT"));
	}
	
	@Test
	public void jarArtifact() throws Exception {
		File artifactFile = tempFolder.newFile("osgibundle.jar");
		when(artifact.getFile()).thenReturn(artifactFile);
		
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
		manifest.getMainAttributes().putValue("Bundle-Version", "1.0.1.1234567");
		
		try (OutputStream os = new JarOutputStream(new FileOutputStream(artifactFile), manifest)) {
		}
		
		setOtherExpectedAttributes(Collections.singletonMap("version", "1.0.1.1234567"));
		assertBundleResources();
	}
	
	@Test
	public void dirArtifact() throws Exception {
		File artifactFile = tempFolder.newFolder("osgibundle");
		when(artifact.getFile()).thenReturn(artifactFile);
		
		File mi = new File(artifactFile, "META-INF");
		assertTrue(mi.mkdirs());
		
		File manifestFile = new File(mi, "MANIFEST.MF");
		
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
		manifest.getMainAttributes().putValue("Bundle-Version", "1.0.2.7654321");
		
		try (OutputStream os = new FileOutputStream(manifestFile)) {
			manifest.write(os);
		}
		
		setOtherExpectedAttributes(Collections.singletonMap("version", "1.0.2.7654321"));
		assertBundleResources();
	}

}
