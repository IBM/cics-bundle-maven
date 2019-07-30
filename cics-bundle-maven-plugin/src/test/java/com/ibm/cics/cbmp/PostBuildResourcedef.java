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

import static org.hamcrest.collection.ArrayMatching.arrayContainingInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;

public class PostBuildResourcedef {

	private static final String CICS_XML = "cics.xml";
	private static final String META_INF = "META-INF";
	private static final String EVBIND_BUNDLEPART = "EventBinding.evbind";
	private static final String URIMAP_BUNDLEPART = "mymap.urimap";
	private static final String PROGRAM_BUNDLEPART = "PROG1.program";
	private static final String NOEXTENSION_FILE = "noextension";

	static void assertOutput(File root) throws Exception {
		File bundleArchive = new File(root, "test-bundle/target/test-bundle-0.0.1-SNAPSHOT.zip");
		
		File tempDir = Files.createTempDirectory("cbmp").toFile();
		
		ZipUnArchiver unArchiver = new ZipUnArchiver(bundleArchive);
		unArchiver.setDestDirectory(tempDir);
		unArchiver.enableLogging(new ConsoleLogger());
		unArchiver.extract();
		
		String[] files = tempDir.list();
		assertThat(files, arrayContainingInAnyOrder(META_INF, EVBIND_BUNDLEPART, URIMAP_BUNDLEPART, PROGRAM_BUNDLEPART, NOEXTENSION_FILE));
		
		List<String> wbpLines = FileUtils.readLines(new File(tempDir, EVBIND_BUNDLEPART));
		assertEquals(6, wbpLines.size());
		assertTrue(wbpLines.get(0).startsWith("<?xml"));
		assertTrue(wbpLines.get(0).endsWith("?>"));
		assertEquals("<ns2:eventBinding CICSEPSchemaVersion=\"2\" CICSEPSchemaRelease=\"0\" xsi:schemaLocation=\"http://www.ibm.com/xmlns/prod/cics/eventprocessing/eventbinding CicsEventBinding.xsd \" xmlns:ns2=\"http://www.ibm.com/xmlns/prod/cics/eventprocessing/eventbinding\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">", wbpLines.get(1));
		
		File metaInf = new File(tempDir, META_INF);
		files = metaInf.list();
		assertEquals(1, files.length);
		assertEquals(CICS_XML, files[0]);
		
		List<String> cxLines = FileUtils.readLines(new File(metaInf, CICS_XML));
		System.out.println(cxLines);
		assertEquals(9, cxLines.size());
		assertTrue(cxLines.get(0).startsWith("<?xml"));
		assertTrue(cxLines.get(0).endsWith("?>"));
		assertEquals("<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle\">", cxLines.get(1));
		assertEquals("  <meta_directives>", cxLines.get(2));
		assertTrue(cxLines.get(3).matches("    <timestamp>\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d.\\d\\d\\dZ</timestamp>"));
		assertEquals("  </meta_directives>", cxLines.get(4));
		assertEquals("  <define name=\"PROG1\" path=\"PROG1.program\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/PROGRAM\"/>", cxLines.get(5));
		assertEquals("  <define name=\"EventBinding\" path=\"EventBinding.evbind\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/EVENTBINDING\"/>", cxLines.get(6));
		assertEquals("  <define name=\"mymap\" path=\"mymap.urimap\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/URIMAP\"/>", cxLines.get(7));
		assertEquals("</manifest>", cxLines.get(8));
	}


}
