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

import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.matchers.CompareMatcher;

public class PostBuildTestBundleWar {
	
	private static final String EXPECTED_MANIFEST = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
		"<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-war\">\n" + 
		"  <meta_directives>\n" + 
		"    <timestamp>2019-09-10T20:24:32.893Z</timestamp>\n" + 
		"  </meta_directives>\n" + 
		"  <define name=\"test-war_0.0.1-SNAPSHOT\" path=\"test-war_0.0.1-SNAPSHOT.warbundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/WARBUNDLE\"/>\n" + 
		"</manifest>";
	
	static void assertOutput(File root) throws Exception {
		Path bundle = root.toPath().resolve("test-bundle-war-0.0.1-SNAPSHOT-cics-bundle.zip");
		FileSystem bundleFS = FileSystems.newFileSystem(new URI("jar:" + bundle.toUri().toString()), Collections.emptyMap());
		
		assertThat(
			bundleFS.getPath("META-INF/cics.xml").toUri(),
			CompareMatcher
				.isIdenticalTo(
					EXPECTED_MANIFEST
				).withDifferenceEvaluator(
					DifferenceEvaluators.chain(
						TIMESTAMP_EVALUATOR,
						DifferenceEvaluators.Default
					)
				)
				.withNodeFilter(
					node -> !node.getNodeName().equals("timestamp")
				)
		);
	}
	
	private static final DifferenceEvaluator TIMESTAMP_EVALUATOR = new DifferenceEvaluator() {
		
		@Override
		public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
			if (outcome == ComparisonResult.EQUAL) return outcome; //Only evaluate differences
			
			Node controlNode = comparison.getControlDetails().getTarget();
			if (controlNode instanceof Element) {
				Element controlElement = (Element) controlNode;
				if (isSingleTimestampElement(controlElement)) {
					Node testNode = comparison.getTestDetails().getTarget();
					if (testNode instanceof Element) {
						Element testElement = (Element) testNode;
						if (isSingleTimestampElement(testElement)) {
							return ComparisonResult.EQUAL;
						}
					}
				}
			}
			return outcome;
		}

		protected boolean isSingleTimestampElement(Element controlElement) {
			return "timestamp".equalsIgnoreCase(controlElement.getNodeName()) && controlElement.getChildNodes().getLength() == 0;
		}
	};
}
