package com.ibm.cics.cbmp;

import static com.ibm.cics.cbmp.BundleValidator.assertBundleContents;
import static com.ibm.cics.cbmp.BundleValidator.bfv;
import static com.ibm.cics.cbmp.BundleValidator.manifestValidator;

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

import static org.junit.Assert.assertThat;

import java.io.File;

import org.xmlunit.matchers.CompareMatcher;

public class PostBuildTestBundleEba {
	
	private static final String EXPECTED_MANIFEST = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
		"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle-eba\">\n" + 
		"  <meta_directives>\n" + 
		"    <timestamp>2019-09-10T20:24:32.893Z</timestamp>\n" + 
		"  </meta_directives>\n" + 
		"  <define name=\"test-bundle-eba-0.0.1-SNAPSHOT\" path=\"test-bundle-eba-0.0.1-SNAPSHOT.ebabundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/EBABUNDLE\"/>\n" + 
		"</manifest>";
	
	private static final String EXPECTED_BUNDLE_PART = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
		"<ebabundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-bundle-eba-0.0.1-SNAPSHOT\"/>";
	
	static void assertOutput(File root) throws Exception {
		assertBundleContents(
			root.toPath().resolve("target/test-bundle-eba-0.0.1-SNAPSHOT-cics-bundle.zip"),
			manifestValidator(EXPECTED_MANIFEST),
			bfv(
				"/test-bundle-eba-0.0.1-SNAPSHOT.ebabundle",
				is -> assertThat(is, CompareMatcher.isIdenticalTo(EXPECTED_BUNDLE_PART))
			),
			bfv(
				"/test-bundle-eba-0.0.1-SNAPSHOT.eba",
				is -> {}
			)
		);
	}
}
