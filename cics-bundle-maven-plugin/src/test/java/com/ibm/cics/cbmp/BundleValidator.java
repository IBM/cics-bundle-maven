package com.ibm.cics.cbmp;

import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import org.w3c.dom.Attr;
import org.w3c.dom.Text;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Comparison.Detail;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.matchers.CompareMatcher;

public class BundleValidator {
	
	public static final DifferenceEvaluator TIMESTAMP_EVALUATOR = new DifferenceEvaluator() {
		
		private static final String TIMESTAMP_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3,9}Z";
		
		@Override
		public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
			if (outcome == ComparisonResult.EQUAL) return outcome; //Only evaluate differences

			if (isTimestampContent(comparison.getControlDetails()) && isTimestampContent(comparison.getTestDetails())) {
				//return EQUAL if the values both look like timestamps
				Text control = (Text) comparison.getControlDetails().getTarget();
				Text test = (Text) comparison.getTestDetails().getTarget();
				if (control.getData().matches(TIMESTAMP_PATTERN) && test.getData().matches(TIMESTAMP_PATTERN)) {
					return ComparisonResult.EQUAL;
				}
			}
			
			return outcome;
		}

		protected boolean isTimestampContent(Detail details) {
			return "/manifest[1]/meta_directives[1]/timestamp[1]/text()[1]".equals(details.getXPath());
		}
	};
	
	public static void assertBundleContents(
			Path bundle,
			BundleFileValidator... validators) {
		
		URI uri;
		try {
			uri = new URI("jar:" + bundle.toUri().toString());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		System.out.println("Validating bundle: " + uri.toString());
		
		try (FileSystem bundleFS = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
			Path root = bundleFS.getRootDirectories().iterator().next();
			
			Files
				.walk(root)
				.filter(Files::isRegularFile)
				.forEach(p -> {
					BundleFileValidator validator = Arrays
						.stream(validators)
						.filter(v -> v.consumesPath(p.toString()))
						.findFirst()
						.orElseThrow(() -> new RuntimeException("Unexpected bundle file: " + p));
					
					try (InputStream is = Files.newInputStream(p)) {
						validator.validate(is);
					} catch (IOException e) {
						throw new RuntimeException(e);
					};
				});
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Bundle not found: " + uri);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static interface BundleFileValidator {
		
		public boolean consumesPath(String p);
		
		public void validate(InputStream is) throws RuntimeException;
	}
	
	public static BundleFileValidator bfv(String path, Consumer<InputStream> validator) {
		return new BundleFileValidator() {
			@Override
			public void validate(InputStream is) throws RuntimeException {
				validator.accept(is);
			}
			
			@Override
			public boolean consumesPath(String p) {
				return path.equals(p.toString());
			}
		};
	}
	
	public static BundleFileValidator bfmv(String regex, Consumer<InputStream> validator) {
		return new BundleFileValidator() {
			@Override
			public void validate(InputStream is) throws RuntimeException {
				validator.accept(is);
			}
			
			@Override
			public boolean consumesPath(String p) {
				return p.toString().matches(regex);
			}
		};
	}
	
	public static BundleFileValidator manifestValidator(String expectedManifest) {
		return bfv(
			"/META-INF/cics.xml",
			is -> assertThat(is, CompareMatcher
				.isIdenticalTo(
					expectedManifest
				).withDifferenceEvaluator(
					DifferenceEvaluators.chain(
						DifferenceEvaluators.Default,
						TIMESTAMP_EVALUATOR
					)
				)
			)
		);
	}

	public static final DifferenceEvaluator OSGI_VERSION_EVALUATOR = new DifferenceEvaluator() {
		
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
}
