package com.ibm.cics.cbmp;
/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2020 IBM Corp.
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

public class PostBuildBundlePartDirectory {

	private static final String EVBIND_BUNDLEPART = "/EventBinding.evbind";
	private static final String URIMAP_BUNDLEPART = "/mymap.urimap";
	private static final String PROGRAM_BUNDLEPART = "/PROG1.program";
	private static final String NOEXTENSION_FILE = "/noextension";

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
				"  <define name=\"PROG1\" path=\"PROG1.program\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/PROGRAM\"/>\n" +
				"  <define name=\"EventBinding\" path=\"EventBinding.evbind\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/EVENTBINDING\"/>\n" +
				"  <define name=\"mymap\" path=\"mymap.urimap\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/URIMAP\"/>\n" +
				"</manifest>"
			),
			bfv(
				EVBIND_BUNDLEPART,
				is -> assertThat(
					is,
					CompareMatcher.isIdenticalTo(
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
						"<ns2:eventBinding CICSEPSchemaVersion=\"2\" CICSEPSchemaRelease=\"0\" " +
						"xsi:schemaLocation=\"http://www.ibm.com/xmlns/prod/cics/eventprocessing/eventbinding CicsEventBinding.xsd \" " +
						"xmlns:ns2=\"http://www.ibm.com/xmlns/prod/cics/eventprocessing/eventbinding\" " +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"    <description></description>\n" + 
						"    <userTag></userTag>\n" + 
						"    <eventAdapterName></eventAdapterName>\n" + 
						"</ns2:eventBinding>"
					)
				)
			),
			bfv(
				URIMAP_BUNDLEPART,
				is -> {}
			),
			bfv(
				PROGRAM_BUNDLEPART,
				is -> {}
			),
			bfv(
				NOEXTENSION_FILE,
				is -> {}
			)
		);
		
	}


}
