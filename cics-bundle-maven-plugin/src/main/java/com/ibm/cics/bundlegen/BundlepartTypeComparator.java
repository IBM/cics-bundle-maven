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

import java.util.Comparator;

import com.ibm.cics.bundlegen.DefineGraph.CycleDetectedException;

public class BundlepartTypeComparator implements Comparator<String> {

	private static final DefineGraph dependencyGraph;
	
	static {
		try {
			dependencyGraph = new DefineGraph.Builder()
					.addDependencies(new String[] { uri("EARBUNDLE"), uri("WARBUNDLE"), uri("URIMAP"), uri("EPADAPTER"), uri("TRANSACTION") }, uri("PROGRAM"))
					.addDependencies(new String[] { uri("EPADAPTER") }, uri("TRANSACTION"))
					.addDependencies(new String[] { uri("EPADAPTERSET") }, uri("EPADAPTER"))
					.addDependencies(uri("EVENTBINDING"), new String[] { uri("EPADAPTER"), uri("EPADAPTERSET") })
					.addDependencies(uri("PROGRAM"), new String[] { uri("OSGIBUNDLE"), uri("LIBRARY") })
					.addDependencies(uri("URIMAP"), new String[] { uri("PIPELINE"), uri("OSGIBUNDLE"), uri("EARBUNDLE"), uri("WARBUNDLE") })
					.addDependencies(uri("POLICY"), new String[] { uri("EPADAPTER"), uri("EPADAPTERSET") })
					.addDependency(uri("PROGRAM"), uri("FILE"))
					.build();
		} catch (CycleDetectedException e) {
			throw new RuntimeException("Cycles detected in define graph", e);
		}
	}
	
	@Override
	public int compare(String o1, String o2) {
		// -1 when o1 < o2
		if (dependencyGraph.testDependsOn(o2, o1)) {
			return -1;
		}
		// +1 when o1 > o2
		if (dependencyGraph.testDependsOn(o1, o2)) {
			return 1;
		}
		// Otherwise 0
		return 0;
	}

}
