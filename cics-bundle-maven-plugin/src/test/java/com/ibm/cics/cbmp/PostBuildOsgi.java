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

import static org.hamcrest.collection.ArrayMatching.hasItemInArray;
import static org.hamcrest.collection.IsIn.in;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;

public class PostBuildOsgi {

	private static final String CICS_XML = "cics.xml";
	private static final String META_INF = "META-INF";
	private static final String BND_SYMBOLIC_NAME = "test-osgi";
	private static final String TYCHO_SYMBOLIC_NAME = "test-tycho";
	private static final String VERSION_REGEX = "0\\.0\\.1\\.[0-9]{12}";
	private static final String BUNDLE_PART_EXT_REGEX = "\\.osgibundle";
	private static final String BUNDLE_EXT_REGEX = "\\.jar";
	private static final String BND_BUNDLE_PART_REGEX = BND_SYMBOLIC_NAME + "_" + VERSION_REGEX + BUNDLE_PART_EXT_REGEX;
	private static final String TYCHO_BUNDLE_PART_REGEX = TYCHO_SYMBOLIC_NAME + "_" + VERSION_REGEX + BUNDLE_PART_EXT_REGEX;
	private static final String BND_BUNDLE_REGEX = BND_SYMBOLIC_NAME + "_" + VERSION_REGEX + BUNDLE_EXT_REGEX;
	private static final String TYCHO_BUNDLE_REGEX = TYCHO_SYMBOLIC_NAME + "_" + VERSION_REGEX + BUNDLE_EXT_REGEX;

	static void assertOutput(File root) throws Exception {
		File bundleArchive = new File(root, "test-bundle/target/test-bundle-0.0.1-SNAPSHOT.zip");
		
		File tempDir = Files.createTempDirectory("cbmp").toFile();
		
		ZipUnArchiver unArchiver = new ZipUnArchiver(bundleArchive);
		unArchiver.setDestDirectory(tempDir);
		unArchiver.enableLogging(new ConsoleLogger());
		unArchiver.extract();
		
		String[] files = tempDir.list();
		assertThat(META_INF, is(in(files)));
		assertThat(files, hasItemInArray(matchesPattern(BND_BUNDLE_PART_REGEX)));
		assertThat(files, hasItemInArray(matchesPattern(BND_BUNDLE_REGEX)));
		assertThat(files, hasItemInArray(matchesPattern(TYCHO_BUNDLE_PART_REGEX)));
		assertThat(files, hasItemInArray(matchesPattern(TYCHO_BUNDLE_REGEX)));
		assertEquals(5, files.length);
		
		{
			String bundlePartFile = Arrays.stream(files).filter(f -> f.matches(BND_BUNDLE_PART_REGEX)).findFirst().get();
			List<String> wbpLines = FileUtils.readLines(new File(tempDir, bundlePartFile));
			assertEquals(2, wbpLines.size());
			assertTrue(wbpLines.get(0).startsWith("<?xml"));
			assertTrue(wbpLines.get(0).endsWith("?>"));
			assertThat(wbpLines.get(1), matchesPattern("<osgibundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-osgi\" version=\"0\\.0\\.1\\.[0-9]{12}\"/>"));
		}
		
		{
			String bundlePartFile = Arrays.stream(files).filter(f -> f.matches(TYCHO_BUNDLE_PART_REGEX)).findFirst().get();
			List<String> wbpLines = FileUtils.readLines(new File(tempDir, bundlePartFile));
			assertEquals(2, wbpLines.size());
			assertTrue(wbpLines.get(0).startsWith("<?xml"));
			assertTrue(wbpLines.get(0).endsWith("?>"));
			assertThat(wbpLines.get(1), matchesPattern("<osgibundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-tycho\" version=\"0\\.0\\.1\\.[0-9]{12}\"/>"));
		}
		
		File metaInf = new File(tempDir, META_INF);
		files = metaInf.list();
		assertEquals(1, files.length);
		assertEquals(CICS_XML, files[0]);
		
		List<String> cxLines = FileUtils.readLines(new File(metaInf, CICS_XML));
		System.out.println(cxLines);
		assertEquals(8, cxLines.size());
		assertTrue(cxLines.get(0).startsWith("<?xml"));
		assertTrue(cxLines.get(0).endsWith("?>"));
		assertEquals("<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle\">", cxLines.get(1));
		assertEquals("  <meta_directives>", cxLines.get(2));
		assertTrue(cxLines.get(3).matches("    <timestamp>\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d.\\d\\d\\dZ</timestamp>"));
		assertEquals("  </meta_directives>", cxLines.get(4));
		assertThat(cxLines.get(5), matchesPattern("  <define name=\"test-osgi-0\\.0\\.1-SNAPSHOT\" path=\"test-osgi_0\\.0\\.1\\.[0-9]{12}\\.osgibundle\" type=\"http://www\\.ibm\\.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>"));
		assertThat(cxLines.get(6), matchesPattern("  <define name=\"test-tycho-0\\.0\\.1-SNAPSHOT\" path=\"test-tycho_0\\.0\\.1\\.[0-9]{12}\\.osgibundle\" type=\"http://www\\.ibm\\.com/xmlns/prod/cics/bundle/OSGIBUNDLE\"/>"));
		assertEquals("</manifest>", cxLines.get(7));
	}
	
}
