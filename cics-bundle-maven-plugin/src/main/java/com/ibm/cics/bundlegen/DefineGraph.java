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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefineGraph {

	public Map<String, Set<String>> typesDependedOnByTypeGraph = new HashMap<>();

	private DefineGraph(Map<String, Set<String>> sourceMap) throws CycleDetectedException {
		// Clone to avoid subsequent manipulation via the builder
		sourceMap.forEach((type, typesDependedOnByType) -> {
			typesDependedOnByTypeGraph.put(type, new HashSet<>(typesDependedOnByType));
		});
		checkForCycles();
	}

	private void checkForCycles() throws CycleDetectedException {
		Set<String> completedTypes = new HashSet<>(typesDependedOnByTypeGraph.size());
		for (String type : typesDependedOnByTypeGraph.keySet()) {
			List<String> alreadyVisited = new ArrayList<>();
			visitAndCheckPath(type, alreadyVisited, completedTypes);
			completedTypes.add(type);
		}
	}

	private void visitAndCheckPath(String type, List<String> alreadyVisited, Set<String> completedTypes)
			throws CycleDetectedException {
		boolean newVisit = !alreadyVisited.contains(type);
		if (!newVisit) {
			throw new CycleDetectedException("Cycle detected in define graph:\n\n" + alreadyVisited.stream().collect(Collectors.joining(" depends on\n")) + " depends on\n" + type);
		}
		alreadyVisited.add(type);
		Set<String> typesDependedOnByType = typesDependedOnByTypeGraph.get(type);
		if (typesDependedOnByType != null) {
			for (String typeDependedOn : typesDependedOnByType) {
				if (!completedTypes.contains(typeDependedOn)) {
					visitAndCheckPath(typeDependedOn, new ArrayList<>(alreadyVisited), completedTypes);
				}
			}
		}

	}

	public boolean testDependsOn(String type, String dependsOnType) {
		Set<String> typesDependedOnByType = typesDependedOnByTypeGraph.get(type);
		if (typesDependedOnByType != null) {
			for (String typeDependedOnByType : typesDependedOnByType) {
				if (dependsOnType.equals(typeDependedOnByType)) {
					return true;
				}
				boolean indirectResult = testDependsOn(typeDependedOnByType, dependsOnType);
				if (indirectResult) {
					return true;
				}
			}
		}
		return false;

	}

	public static class Builder {

		private Map<String, Set<String>> typesDependedOnByTypeGraph = new HashMap<>();

		public Builder addDependencies(String type, String[] dependsOnTypes) {
			Arrays.asList(dependsOnTypes).forEach(t -> addDependency(type, t));
			return this;
		}

		public Builder addDependencies(String[] types, String dependOnType) {
			Arrays.asList(types).forEach(t -> addDependency(t, dependOnType));
			return this;
		}

		public Builder addDependency(String type, String dependsOnType) {
			Set<String> typesDependedOnByThisType = typesDependedOnByTypeGraph.get(type);
			if (typesDependedOnByThisType == null) {
				typesDependedOnByTypeGraph.put(type, typesDependedOnByThisType = new HashSet<>());
			}
			typesDependedOnByThisType.add(dependsOnType);
			return this;
		}

		public DefineGraph build() throws CycleDetectedException {
			return new DefineGraph(typesDependedOnByTypeGraph);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typesDependedOnByTypeGraph == null) ? 0 : typesDependedOnByTypeGraph.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefineGraph other = (DefineGraph) obj;
		if (typesDependedOnByTypeGraph == null) {
			if (other.typesDependedOnByTypeGraph != null)
				return false;
		} else if (!typesDependedOnByTypeGraph.equals(other.typesDependedOnByTypeGraph))
			return false;
		return true;
	}

	public class CycleDetectedException extends Exception {

		private static final long serialVersionUID = 1L;

		private CycleDetectedException(String message) {
			super(message);
		}

	}

}
