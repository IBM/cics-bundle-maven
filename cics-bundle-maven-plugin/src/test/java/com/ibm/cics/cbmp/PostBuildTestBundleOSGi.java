package com.ibm.cics.cbmp;

import static com.ibm.cics.cbmp.BundleValidator.assertBundleContents;
import static com.ibm.cics.cbmp.BundleValidator.bfv;
import static com.ibm.cics.cbmp.BundleValidator.manifestValidator;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Pattern;

import org.xmlunit.matchers.CompareMatcher;

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

public class PostBuildTestBundleOSGi {
	
	static void assertOutput(File root) throws Exception {
		Path cicsBundle = root.toPath().resolve("target/test-bundle-osgi-0.0.1-SNAPSHOT-cics-bundle.zip");

		String qualifier;
		try (FileSystem bundleFS = FileSystems.newFileSystem(new URI("jar:" + cicsBundle.toUri().toString()), Collections.emptyMap())) {
			String pattern = "/test-bundle-osgi_0\\.0\\.1\\.([0-9]*)\\.jar";
			qualifier = Files
					.walk(bundleFS.getRootDirectories().iterator().next())
					.map(p -> Pattern.compile(pattern).matcher(p.toString()))
					.filter(m -> m.matches())
					.findFirst()
					.map(m -> m.group(1))
					.orElseThrow(() -> new RuntimeException("Couldn't determine qualifier"));
		}
		assertBundleContents(
			cicsBundle,
			manifestValidator(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
				"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle-osgi\">\n" + 
				"  <meta_directives>\n" + 
				"    <timestamp>2019-09-11T21:12:17.023Z</timestamp>\n" + 
				"  </meta_directives>\n" + 
				"  <define name=\"test-bundle-osgi_0.0.1." + qualifier + "\" path=\"test-bundle-osgi_0.0.1." + qualifier + ".osgibundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>\n" + 
				"</manifest>"
			),
			bfv(
				"/test-bundle-osgi_0.0.1." + qualifier + ".osgibundle",
				is -> assertThat(
					is,
					CompareMatcher.isIdenticalTo(
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
						"<osgibundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-bundle-osgi\" version=\"0.0.1." + qualifier + "\"/>\n"
					)
				)
			),
			bfv(
				"/test-bundle-osgi_0.0.1." + qualifier + ".jar",
				is -> {}
			)
		);
	}

}
