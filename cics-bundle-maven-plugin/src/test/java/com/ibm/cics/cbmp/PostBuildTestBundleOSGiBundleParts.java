package com.ibm.cics.cbmp;

import static com.ibm.cics.cbmp.BundleValidator.OSGI_VERSION_EVALUATOR;
import static com.ibm.cics.cbmp.BundleValidator.assertBundleContents;
import static com.ibm.cics.cbmp.BundleValidator.bfv;
import static com.ibm.cics.cbmp.BundleValidator.manifestValidator;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.hamcrest.MatcherAssert;
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

public class PostBuildTestBundleOSGiBundleParts {
	
	static void assertOutput(File root) throws Exception {
		Path cicsBundle = root.toPath().resolve("target/test-bundle-osgi-bundle-parts-0.0.1-SNAPSHOT-cics-bundle.zip");

		assertBundleContents(
			cicsBundle,
			manifestValidator(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
				"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle-osgi-bundle-parts\">\n" + 
				"  <meta_directives>\n" + 
				"    <timestamp>2019-09-11T21:12:17.023Z</timestamp>\n" + 
				"  </meta_directives>\n" + 
				"  <define name=\"test-bundle-osgi-bundle-parts-0.0.1-SNAPSHOT\" path=\"test-bundle-osgi-bundle-parts-0.0.1-SNAPSHOT.osgibundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>\n" +
				"  <define name=\"JAVAVES1\" path=\"JAVAVES1.program\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/PROGRAM\"/>\n" +
				"  <define name=\"JVE1\" path=\"JVE1.transaction\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/TRANSACTION\"/>\n" +
				"</manifest>"
			),
			bfv(
				"/test-bundle-osgi-bundle-parts-0.0.1-SNAPSHOT.osgibundle",
				is -> assertThat(
					is,
					CompareMatcher.isIdenticalTo(
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
						"<osgibundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-bundle-osgi-bundle-parts\" version=\"0.0.1.201912132301\"/>"
					).withDifferenceEvaluator(
						DifferenceEvaluators.chain(
							DifferenceEvaluators.Default,
							OSGI_VERSION_EVALUATOR
						)
					)
				)
			),
			bfv(
				"/test-bundle-osgi-bundle-parts-0.0.1-SNAPSHOT.jar",
				is -> {}
			),
			bfv(
				"/JAVAVES1.program",
				is -> MatcherAssert.assertThat(
					is,
					CompareMatcher.isIdenticalTo(
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<cicsdefinitionprogram xmlns=\"http://www.ibm.com/xmlns/prod/CICS/smw2int\" concurrency=\"REQUIRED\" datalocation=\"ANY\" execkey=\"CICS\" jvm=\"YES\" jvmclass=\"com.ibm.cicsdev.vsam.esds.EsdsExample1\" jvmserver=\"DFHJVMS\" name=\"JAVAVES1\"/>"
					)
				)
			),
			bfv(
				"/JVE1.transaction",
				is -> MatcherAssert.assertThat(
					is,
					CompareMatcher.isIdenticalTo(
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<cicsdefinitiontransaction xmlns=\"http://www.ibm.com/xmlns/prod/CICS/smw2int\" localq=\"N_A\" name=\"JVE1\" program=\"JAVAVES1\" taskdataloc=\"ANY\"/>"
					)
				)
			)
		);
	}

}