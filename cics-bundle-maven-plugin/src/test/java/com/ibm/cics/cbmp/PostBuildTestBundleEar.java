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

import static com.ibm.cics.cbmp.BundleValidator.assertBundleContents;
import static com.ibm.cics.cbmp.BundleValidator.bfv;
import static com.ibm.cics.cbmp.BundleValidator.manifestValidator;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.xmlunit.matchers.CompareMatcher;

public class PostBuildTestBundleEar {
	
	private static final String EXPECTED_MANIFEST =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
		"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle-ear\">\n" + 
		"  <meta_directives>\n" + 
		"    <timestamp>2019-09-11T20:37:38.144Z</timestamp>\n" + 
		"  </meta_directives>\n" + 
		"  <define name=\"test-bundle-ear-0.0.1-SNAPSHOT\" path=\"test-bundle-ear-0.0.1-SNAPSHOT.earbundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/EARBUNDLE\"/>\n" + 
		"</manifest>";
	
	private static final String EXPECTED_BUNDLE_PART = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
		"<earbundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-bundle-ear-0.0.1-SNAPSHOT\"/>";
	
	static void assertOutput(File root) throws Exception {
		assertBundleContents(
			root.toPath().resolve("target/test-bundle-ear-0.0.1-SNAPSHOT-cics-bundle.zip"),
			manifestValidator(EXPECTED_MANIFEST),
			bfv(
				"/test-bundle-ear-0.0.1-SNAPSHOT.earbundle",
				is -> assertThat(is, CompareMatcher.isIdenticalTo(EXPECTED_BUNDLE_PART))
			),
			bfv(
				"/test-bundle-ear-0.0.1-SNAPSHOT.ear",
				is -> {}
			)
		);
	}
}
