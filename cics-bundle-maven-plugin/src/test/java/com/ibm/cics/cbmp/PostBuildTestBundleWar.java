package com.ibm.cics.cbmp;

import static com.ibm.cics.cbmp.BundleValidator.assertBundleContents;
import static com.ibm.cics.cbmp.BundleValidator.bfv;

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

public class PostBuildTestBundleWar {
	
	private static final String EXPECTED_MANIFEST = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
		"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle-war\">\n" + 
		"  <meta_directives>\n" + 
		"    <timestamp>2019-09-10T20:24:32.893Z</timestamp>\n" + 
		"  </meta_directives>\n" + 
		"  <define name=\"test-bundle-war-0.0.1-SNAPSHOT\" path=\"test-bundle-war-0.0.1-SNAPSHOT.warbundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/WARBUNDLE\"/>\n" + 
		"</manifest>";
	
	private static final String EXPECTED_BUNDLE_PART = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
		"<warbundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-bundle-war-0.0.1-SNAPSHOT\"/>";
	
	public static void main(String[] args) throws Exception {
		assertOutput(new File("/Users/stewf/repos/cics-bundle-maven/cics-bundle-maven-plugin/target/it/test-bundle-war"));
	}
	
	static void assertOutput(File root) throws Exception {
		assertBundleContents(
			root.toPath().resolve("target/test-bundle-war-0.0.1-SNAPSHOT-cics-bundle.zip"),
			bfv(
				"/META-INF/cics.xml",
				is -> assertThat(is , BundleValidator.manifestMatcher(EXPECTED_MANIFEST))
			),
			bfv(
				"/test-bundle-war-0.0.1-SNAPSHOT.warbundle",
				is -> assertThat(is, CompareMatcher.isIdenticalTo(EXPECTED_BUNDLE_PART))
			),
			bfv(
				"/test-bundle-war-0.0.1-SNAPSHOT.war",
				is -> {}
			)
		);
	}
}
