package com.ibm.cics.cbmp;
/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2019 - 2023 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static com.ibm.cics.cbmp.BundleValidator.OSGI_VERSION_EVALUATOR;
import static com.ibm.cics.cbmp.BundleValidator.assertBundleContents;
import static com.ibm.cics.cbmp.BundleValidator.bfmv;
import static com.ibm.cics.cbmp.BundleValidator.bfv;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.matchers.CompareMatcher;

import com.ibm.cics.cbmp.BundleValidator.BundleFileValidator;

public class PostBuildOsgi {

	private static final String BND_SYMBOLIC_NAME = "test-osgi";
	private static final String TYCHO_SYMBOLIC_NAME = "test-tycho";
	private static final String BND_SYMBOLIC_NAME_VR= "test-osgi-versionrange";
	private static final String VERSION = "0.0.1-SNAPSHOT";
	private static final String BUNDLE_PART_EXT_REGEX = "\\.osgibundle";
	private static final String BUNDLE_EXT_REGEX = "\\.jar";
	private static final String BND_VR_BUNDLE_REGEX = "\\/" + BND_SYMBOLIC_NAME_VR + "-" + VERSION + BUNDLE_PART_EXT_REGEX;
	private static final String BND_BUNDLE_PART_REGEX = "\\/" + BND_SYMBOLIC_NAME + "-" + VERSION + BUNDLE_PART_EXT_REGEX;
	private static final String TYCHO_BUNDLE_PART_REGEX = "\\/" + TYCHO_SYMBOLIC_NAME + "-" + VERSION + BUNDLE_PART_EXT_REGEX;
	private static final String BND_BUNDLE_REGEX = "\\/" + BND_SYMBOLIC_NAME + "-" + VERSION + BUNDLE_EXT_REGEX;
	private static final String TYCHO_BUNDLE_REGEX = "\\/" + TYCHO_SYMBOLIC_NAME + "-" + VERSION + BUNDLE_EXT_REGEX;

	static void assertOutput(File root) throws Exception {
		Path cicsBundle = root.toPath().resolve("test-bundle/target/test-bundle-0.0.1-SNAPSHOT.zip");

		assertBundleContents(
				cicsBundle,
				manifestWithOSGiVersionsValidator(
					"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
					"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle\">\n" +
					"  <meta_directives>\n" +
					"    <timestamp>2019-09-11T21:12:17.023Z</timestamp>\n" +
					"  </meta_directives>\n" +
					"  <define name=\"test-osgi-0.0.1-SNAPSHOT\" path=\"test-osgi-0.0.1-SNAPSHOT.osgibundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>\n" +
					"  <define name=\"test-tycho-0.0.1-SNAPSHOT\" path=\"test-tycho-0.0.1-SNAPSHOT.osgibundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>\n" +
					"</manifest>"
				),
				bfmv(
					BND_BUNDLE_PART_REGEX,
					is -> assertThat(
						is,
						CompareMatcher.isIdenticalTo(
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
							"<osgibundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-osgi\" version=\"0.0.1.201912132301\"/>"
						).withDifferenceEvaluator(
							DifferenceEvaluators.chain(
								DifferenceEvaluators.Default,
								OSGI_VERSION_EVALUATOR
							)
						)
					)
				),
				bfmv(
					TYCHO_BUNDLE_PART_REGEX,
					is -> assertThat(
						is,
						CompareMatcher.isIdenticalTo(
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
							"<osgibundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-tycho\" version=\"0.0.1.201912132301\"/>"
						).withDifferenceEvaluator(
							DifferenceEvaluators.chain(
								DifferenceEvaluators.Default,
								OSGI_VERSION_EVALUATOR
							)
						)
					)
				),
				bfmv(
						BND_VR_BUNDLE_REGEX,
						is -> assertThat(
								is,
								CompareMatcher.isIdenticalTo(
										"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
												"<osgibundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-osgi-versionrange\" version=\"0.0.1.201912132301\"/>"
								).withDifferenceEvaluator(
										DifferenceEvaluators.chain(
												DifferenceEvaluators.Default,
												OSGI_VERSION_EVALUATOR
										)
								)
						)
				),
				bfmv(
					BND_BUNDLE_REGEX,
					is -> {}
				),
				bfmv(
					TYCHO_BUNDLE_REGEX,
					is -> {}
				),
				bfmv(
					BND_VR_BUNDLE_REGEX,
					is -> {}
				)
			);
		
	}
	
	public static BundleFileValidator manifestWithOSGiVersionsValidator(String expectedManifest) {
		return bfv(
			"/META-INF/cics.xml",
			is -> assertThat(is, CompareMatcher
				.isIdenticalTo(
					expectedManifest
				).withDifferenceEvaluator(
					DifferenceEvaluators.chain(
						DifferenceEvaluators.Default,
						BundleValidator.TIMESTAMP_EVALUATOR
					)
				)
			)
		);
	}
}
