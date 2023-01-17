package com.ibm.cics.cbmp;

import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.nio.file.Path;

import static com.ibm.cics.cbmp.BundleValidator.*;
import static org.junit.Assert.assertThat;

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

public class PostBuildTestBundleOSGiVersionRange {
	
	static void assertOutput(File root) throws Exception {
		Path cicsBundle = root.toPath().resolve("bundle/target/bundle-1.0.zip");

		assertBundleContents(
			cicsBundle,
			manifestValidator(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
						"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"1\" bundleMicroVer=\"0\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"bundle\">\n" +
						"  <meta_directives>\n" +
						"    <timestamp>2023-01-17T15:00:32.038Z</timestamp>\n" +
						"  </meta_directives>\n" +
						"  <define name=\"osgi-1.0\" path=\"osgi-1.0.osgibundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>\n" +
						"</manifest>\n"
			),
			bfv(
				"/osgi-1.0.osgibundle",
				is -> assertThat(
					is,
					CompareMatcher.isIdenticalTo(
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
						"<osgibundle jvmserver=\"MYJVMS\" symbolicname=\"com.ibm.cics.test-bundle-osgi-versionrange.osgi\" versionrange=\"[1.0,2.0)\"/>"
					).withDifferenceEvaluator(
						DifferenceEvaluators.chain(
							DifferenceEvaluators.Default,
							OSGI_VERSION_EVALUATOR
						)
					)
				)
			),
			bfv(
				"/osgi-1.0.jar",
				is -> {}
			)
		);
	}

}
