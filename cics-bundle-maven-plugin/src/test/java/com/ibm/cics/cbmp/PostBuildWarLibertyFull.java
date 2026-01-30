package com.ibm.cics.cbmp;
/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2026 IBM Corp.
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

public class PostBuildWarLibertyFull {

	private static final String BASE_NAME = "/test-war-1.0.0";
	private static final String WAR_BUNDLE_PART = BASE_NAME + ".warbundle";
	private static final String WAR_BUNDLE = BASE_NAME + ".war";
	private static final String SERVER_XML = "/server.xml";

	public static void assertOutput(File root) throws Exception {
		Path cicsBundle = root.toPath().resolve("target/test-bundle-0.0.1-SNAPSHOT.zip");
		
		assertBundleContents(
				cicsBundle,
				manifestValidator(
					"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
					"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle\">\n" +
					"  <meta_directives>\n" +
					"    <timestamp>2019-09-11T21:12:17.023Z</timestamp>\n" +
					"  </meta_directives>\n" +
					"  <define name=\"test-war-1.0.0\" path=\"test-war-1.0.0.warbundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/WARBUNDLE\"/>\n" +
					"</manifest>"
				),
				bfv(
					WAR_BUNDLE_PART,
					is -> assertThat(
						is,
						CompareMatcher.isIdenticalTo(
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
							"<warbundle addCICSAllAuth=\"false\" appConfigFile=\"server.xml\" jvmserver=\"EYUCMCIJ\" symbolicname=\"test-war-1.0.0\"/>"
						)
					)
				),
				bfv(
					WAR_BUNDLE,
					is -> {}
				),
				bfv(
					SERVER_XML,
					is -> assertThat(
						is,
						CompareMatcher.isIdenticalTo(
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
							"<application id=\"test-war\" name=\"test-war\" type=\"war\" location=\"test-war-1.0.0.war\">\n" +
							"    <application-bnd>\n" +
							"        <security-role name=\"Administrator\">\n" +
							"            <user name=\"admin\" />\n" +
							"        </security-role>\n" +
							"    </application-bnd>\n" +
							"</application>"
						)
					)
				)
			);
	}
	
}