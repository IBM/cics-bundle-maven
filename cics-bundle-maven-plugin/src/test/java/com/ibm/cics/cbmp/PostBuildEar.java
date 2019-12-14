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
import java.nio.file.Path;

import org.xmlunit.matchers.CompareMatcher;

public class PostBuildEar {

	private static final String EAR_BASE_NAME = "/test-ear-0.0.1-SNAPSHOT";
	private static final String EAR_BUNDLE_PART = EAR_BASE_NAME + ".earbundle";
	private static final String EAR_BUNDLE = EAR_BASE_NAME + ".ear";

	static void assertOutput(File root) throws Exception {
		Path cicsBundle = root.toPath().resolve("test-bundle/target/test-bundle-0.0.1-SNAPSHOT.zip");
		
		assertBundleContents(
				cicsBundle,
				manifestValidator(
					"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
					"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle\">\n" +
					"  <meta_directives>\n" +
					"    <timestamp>2019-09-11T21:12:17.023Z</timestamp>\n" +
					"  </meta_directives>\n" +
					"  <define name=\"test-ear-0.0.1-SNAPSHOT\" path=\"test-ear-0.0.1-SNAPSHOT.earbundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/EARBUNDLE\"/>\n" +
					"</manifest>"
				),
				bfv(
					EAR_BUNDLE_PART,
					is -> assertThat(
						is,
						CompareMatcher.isIdenticalTo(
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
							"<earbundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-ear-0.0.1-SNAPSHOT\"/>"
						)
					)
				),
				bfv(
					EAR_BUNDLE,
					is -> {}
				)
			);
	}
	
}
