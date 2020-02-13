package com.ibm.cics.cbmp;

import static com.ibm.cics.cbmp.BundleValidator.OSGI_VERSION_EVALUATOR;
import static com.ibm.cics.cbmp.BundleValidator.assertBundleContents;
import static com.ibm.cics.cbmp.BundleValidator.bfv;
import static com.ibm.cics.cbmp.BundleValidator.manifestValidator;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.xmlunit.diff.DifferenceEvaluators;
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

		assertBundleContents(
			cicsBundle,
			manifestValidator(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
				"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle-osgi\">\n" + 
				"  <meta_directives>\n" + 
				"    <timestamp>2019-09-11T21:12:17.023Z</timestamp>\n" + 
				"  </meta_directives>\n" + 
				"  <define name=\"test-bundle-osgi-0.0.1-SNAPSHOT\" path=\"test-bundle-osgi-0.0.1-SNAPSHOT.osgibundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>\n" + 
				"</manifest>"
			),
			bfv(
				"/test-bundle-osgi-0.0.1-SNAPSHOT.osgibundle",
				is -> assertThat(
					is,
					CompareMatcher.isIdenticalTo(
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
						"<osgibundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-bundle-osgi\" version=\"0.0.1.201912132301\"/>"
					).withDifferenceEvaluator(
						DifferenceEvaluators.chain(
							DifferenceEvaluators.Default,
							OSGI_VERSION_EVALUATOR
						)
					)
				)
			),
			bfv(
				"/test-bundle-osgi-0.0.1-SNAPSHOT.jar",
				is -> {}
			)
		);
	}

}
