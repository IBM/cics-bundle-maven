package com.ibm.cics.cbmp;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;

public class PostBuildWar {

	private static final String CICS_XML = "cics.xml";
	private static final String META_INF = "META-INF";
	private static final String BASE_NAME = "test-war-0.0.1-SNAPSHOT";
	private static final String WAR_BUNDLE_PART = BASE_NAME + ".warbundle";

	static void assertOutput(File root) throws Exception {
		File bundleArchive = new File(root, "test-bundle/target/test-bundle-0.0.1-SNAPSHOT.cicsbundle");
		
		File tempDir = Files.createTempDirectory("cbmp").toFile();
		
		ZipUnArchiver unArchiver = new ZipUnArchiver(bundleArchive);
		unArchiver.setDestDirectory(tempDir);
		unArchiver.enableLogging(new ConsoleLogger());
		unArchiver.extract();
		
		String[] files = tempDir.list();
		assertEquals(3, files.length);
		assertEquals(META_INF, files[0]);
		assertEquals(WAR_BUNDLE_PART, files[1]);
		
		List<String> wbpLines = FileUtils.readLines(new File(tempDir, WAR_BUNDLE_PART));
		assertEquals(2, wbpLines.size());
		assertTrue(wbpLines.get(0).startsWith("<?xml"));
		assertTrue(wbpLines.get(0).endsWith("?>"));
		assertEquals("<warbundle jvmserver=\"EYUCMCIJ\" symbolicname=\"test-war\"/>", wbpLines.get(1));
		
		assertEquals(BASE_NAME + ".war", files[2]);
		
		File metaInf = new File(tempDir, META_INF);
		files = metaInf.list();
		assertEquals(1, files.length);
		assertEquals(CICS_XML, files[0]);
		
		List<String> cxLines = FileUtils.readLines(new File(metaInf, CICS_XML));
		System.out.println(cxLines);
		assertEquals(7, cxLines.size());
		assertTrue(cxLines.get(0).startsWith("<?xml"));
		assertTrue(cxLines.get(0).endsWith("?>"));
		assertEquals("<manifest xmlns=\"http://www.ibm.com/xmlns/prod/cics/bundle\" bundleMajorVer=\"0\" bundleMicroVer=\"1\" bundleMinorVer=\"0\" bundleRelease=\"0\" bundleVersion=\"1\" id=\"test-bundle\">", cxLines.get(1));
		assertEquals("  <meta_directives>", cxLines.get(2));
		assertTrue(cxLines.get(3).matches("    <timestamp>\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d.\\d\\d\\dZ</timestamp>"));
		assertEquals("  </meta_directives>", cxLines.get(4));
		assertEquals("  <define name=\"test-war-0.0.1-SNAPSHOT\" path=\"test-war-0.0.1-SNAPSHOT.warbundle\" type=\"http://www.ibm.com/xmlns/prod/cics/bundle/WARBUNDLE\"/>", cxLines.get(5));
		assertEquals("</manifest>", cxLines.get(6));
	}
	
}
