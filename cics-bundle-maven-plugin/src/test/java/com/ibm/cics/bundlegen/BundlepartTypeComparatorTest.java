package com.ibm.cics.bundlegen;

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

import static com.ibm.cics.bundlegen.DefineFactory.uri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class BundlepartTypeComparatorTest {
	
	BundlepartTypeComparator comparator = new BundlepartTypeComparator();
	
	@Test
	public void uriMapAfterWARBundle() {
		assertSame(1, comparator.compare(uri("URIMAP"), uri("WARBUNDLE")));
	}
	
	@Test
	public void osgiBundleBeforeWarBundle() {
		assertSame(-1, comparator.compare(uri("OSGIBUNDLE"), uri("WARBUNDLE")));
	}
	@Test
	public void warBundleBeforeURImap() {
		assertSame(-1, comparator.compare(uri("WARBUNDLE"), uri("URIMAP")));
	}
	
	@Test
	public void sameEqual() {
		assertSame(0, comparator.compare(uri("OSGIBUNDLE"), uri("OSGIBUNDLE")));
	}
	
	@Test
	public void peersEqual() {
		assertSame(0, comparator.compare(uri("EARBUNDLE"), uri("WARBUNDLE")));
	}
	
	@Test
	public void sortOSGiBundleAndURIMap() {
		List<String> defines = Stream.of(uri("URIMAP"), uri("OSGIBUNDLE")).collect(Collectors.toList());
		List<String> expected = Stream.of(uri("OSGIBUNDLE"), uri("URIMAP")).collect(Collectors.toList());
		
		defines.sort(comparator);
		
		assertEquals(expected, defines);
	}
	
}
