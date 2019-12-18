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
import static com.ibm.cics.cbmp.BundleValidator.bfmv;
import static com.ibm.cics.cbmp.BundleValidator.bfv;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.w3c.dom.Attr;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Comparison.Detail;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.matchers.CompareMatcher;

import com.ibm.cics.cbmp.BundleValidator.BundleFileValidator;

public class PostBuildOsgi {

	private static final String BND_SYMBOLIC_NAME = "test-osgi";
	private static final String TYCHO_SYMBOLIC_NAME = "test-tycho";
	private static final String VERSION_REGEX = "0\\.0\\.1\\.[0-9]{12}";
	private static final String BUNDLE_PART_EXT_REGEX = "\\.osgibundle";
	private static final String BUNDLE_EXT_REGEX = "\\.jar";
	private static final String BND_BUNDLE_PART_REGEX = "\\/" + BND_SYMBOLIC_NAME + "_" + VERSION_REGEX + BUNDLE_PART_EXT_REGEX;
	private static final String TYCHO_BUNDLE_PART_REGEX = "\\/" + TYCHO_SYMBOLIC_NAME + "_" + VERSION_REGEX + BUNDLE_PART_EXT_REGEX;
	private static final String BND_BUNDLE_REGEX = "\\/" + BND_SYMBOLIC_NAME + "_" + VERSION_REGEX + BUNDLE_EXT_REGEX;
	private static final String TYCHO_BUNDLE_REGEX = "\\/" + TYCHO_SYMBOLIC_NAME + "_" + VERSION_REGEX + BUNDLE_EXT_REGEX;

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
					"  <define name=\"test-osgi_0.0.1.201912132301\" path=\"test-osgi_0.0.1.201912132301.osgibundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>\n" +
					"  <define name=\"test-tycho_0.0.1.201912132301\" path=\"test-tycho_0.0.1.201912132301.osgibundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>\n" +
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
					BND_BUNDLE_REGEX,
					is -> {}
				),
				bfmv(
					TYCHO_BUNDLE_REGEX,
					is -> {}
				)
			);
		
	}
	
	private static final DifferenceEvaluator OSGI_VERSION_EVALUATOR = new DifferenceEvaluator() {
		
		private static final String OSGI_VERSION_PATTERN = "0\\.0\\.1\\.[0-9]{12}";
		
		@Override
		public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
			if (outcome == ComparisonResult.EQUAL) return outcome; //Only evaluate differences

			if (isDefineName(comparison.getControlDetails()) && isDefineName(comparison.getTestDetails())) {
				//return EQUAL if the values both look like OSGi versions
				Attr control = (Attr) comparison.getControlDetails().getTarget();
				Attr test = (Attr) comparison.getTestDetails().getTarget();
				if (control.getValue().matches(OSGI_VERSION_PATTERN) && test.getValue().matches(OSGI_VERSION_PATTERN)) {
					return ComparisonResult.EQUAL;
				}
			}
			
			return outcome;
		}

		protected boolean isDefineName(Detail details) {
			return "/osgibundle[1]/@version".equals(details.getXPath());
		}
	};
	
	private static final DifferenceEvaluator OSGI_DEFINE_NAME_EVALUATOR = new DifferenceEvaluator() {
		
		private static final String OSGI_VERSION_PATTERN = "[a-zA-Z0-9_.-]+_0\\.0\\.1\\.[0-9]{12}";
		
		@Override
		public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
			if (outcome == ComparisonResult.EQUAL) return outcome; //Only evaluate differences

			//return EQUAL if the values both look like OSGi symbolicName_versions
			Attr control = (Attr) comparison.getControlDetails().getTarget();
			Attr test = (Attr) comparison.getTestDetails().getTarget();
			if (control.getValue().matches(OSGI_VERSION_PATTERN) && test.getValue().matches(OSGI_VERSION_PATTERN)) {
				return ComparisonResult.EQUAL;
			}
			
			return outcome;
		}

	};
	
	private static final DifferenceEvaluator OSGI_DEFINE_PATH_EVALUATOR = new DifferenceEvaluator() {
		
		private static final String OSGI_PATH_PATTERN = "[a-zA-Z0-9_.-]+_0\\.0\\.1\\.[0-9]{12}\\.osgibundle";
		
		@Override
		public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
			if (outcome == ComparisonResult.EQUAL) return outcome; //Only evaluate differences

			//return EQUAL if the values both look like OSGi bundlepart paths
			Attr control = (Attr) comparison.getControlDetails().getTarget();
			Attr test = (Attr) comparison.getTestDetails().getTarget();
			if (control.getValue().matches(OSGI_PATH_PATTERN) && test.getValue().matches(OSGI_PATH_PATTERN)) {
				return ComparisonResult.EQUAL;
			}
			
			return outcome;
		}

	};

	
	public static BundleFileValidator manifestWithOSGiVersionsValidator(String expectedManifest) {
		return bfv(
			"/META-INF/cics.xml",
			is -> assertThat(is, CompareMatcher
				.isIdenticalTo(
					expectedManifest
				).withDifferenceEvaluator(
					DifferenceEvaluators.chain(
						DifferenceEvaluators.Default,
						BundleValidator.TIMESTAMP_EVALUATOR,
						OSGI_DEFINE_NAME_EVALUATOR,
						OSGI_DEFINE_PATH_EVALUATOR
					)
				)
			)
		);
	}
	
}
