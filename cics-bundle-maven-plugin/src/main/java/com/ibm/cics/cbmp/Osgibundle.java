package com.ibm.cics.cbmp;

import com.ibm.cics.bundlegen.BundleConstants;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Element;

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

public class Osgibundle extends JavaBasedBundlePart  {
	
	private static final String TYPE = BundleConstants.NS + "/OSGIBUNDLE";
	
	public Osgibundle(Log log) {
		super(log);
	}

	@Override
	protected String getBundlePartFileExtension() {
		return "osgibundle";
	}

	@Override
	protected String getType() {
		return TYPE;
	}
	
	@Override
	protected String getSymbolicName(Artifact a) {
		return a.getArtifactId();
	}

	protected String getContentPath(Artifact a) {
		return getOSGiStyleArtifactFilename(a) + "." + a.getType();
	}
	
	@Override
	protected String getDefinePath(Artifact a) {
		return getDefinePath(getOSGiStyleArtifactFilename(a));
	}
	
	private String getDefinePath(String filename) {
		return filename + "." + getBundlePartFileExtension();
	}

	/**
	 * Given an artifact, creates an OSGi bundle-style filename like artifact.name_1.2.3 from
	 * them, taking the version from the artifact's Bundle-Version manifest header.
	 * @param a
	 * @return
	 */
	private String getOSGiStyleArtifactFilename(Artifact a) {
		return  a.getArtifactId() + "_" + convertMavenVersionToOSGiVersion(getBundleVersion(a));
	}
	
	@Override
	protected void addAdditionalNodes(Element rootElement, Artifact a) {
		rootElement.setAttribute("version", convertMavenVersionToOSGiVersion(getBundleVersion(a)));
	}
	
	/**
	 * Takes a given Maven version and turns it into an OSGi-compatible version.
	 * 
	 * This simple algorithm purely replaces the first hyphen (if found) with a period.
	 * @param mavenVersion The Maven-style version string
	 * @return The converted, OSGi-style version
	 */
	private String convertMavenVersionToOSGiVersion(String mavenVersion) {
		return mavenVersion.replaceFirst("-", ".");
	}
	
	/**
	 * Attempts to retrieve the manifest of the given artifact. Will search inside the artifact, if it's a JAR,
	 * or will search inside a directory, if (as happens during incremental builds in the IDE), the artifact file
	 * is still pointing into the classes directory.
	 * @param a The Artifact to retrieve
	 * @return The manifest, or null if none was found
	 */
	private Manifest getManifest(Artifact a) {
		File artifactFile = a.getFile();
		try {
			if (artifactFile.exists()) {
				if (artifactFile.isFile()) {
					try (JarFile jarFile = new JarFile(a.getFile())) {
						return jarFile.getManifest();
					}
				} else {
					File manifestFile = new File(artifactFile, "META-INF/MANIFEST.MF");
					if (manifestFile.exists()) {
						return new Manifest(new BufferedInputStream(new FileInputStream(manifestFile)));
					}
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionRuntimeException("Error reading OSGi bundle manifest", e);
		}
		return null;
	}
	
	/**
	 * Gets a specific header from the given manifest.
	 * @param manifest The manifest to search in
	 * @param headerName The name of the header to find
	 * @return The header, or null if the header is not present.
	 */
	private static String getManifestHeader(Manifest manifest, String headerName) {
		return manifest.getMainAttributes().getValue(headerName);
	}

	/**
	 * Gets the Bundle-Version header inside the given artifact's manifest.
	 * @param a The Artifact to find the Bundle-Version of 
	 * @return The version or null if the manifest, or the header in the manifest, is not present
	 */
	private String getBundleVersion(Artifact a) {
		Manifest manifest = getManifest(a);
		if (manifest != null) {
			String bundleVersion = getManifestHeader(manifest, "Bundle-Version");
			if (bundleVersion != null) {
				return bundleVersion;
			}
		}
		
		return a.getVersion();
	}

}
