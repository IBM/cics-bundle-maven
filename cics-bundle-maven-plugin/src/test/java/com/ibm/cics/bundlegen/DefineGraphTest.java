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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.junit.Test;

import com.ibm.cics.bundlegen.DefineGraph.CycleDetectedException;

public class DefineGraphTest {

	@Test(expected=CycleDetectedException.class)
	public void selfCycle() throws Exception {
		new DefineGraph.Builder()
				.addDependency("A", "A")
				.build();
	}

	@Test(expected=CycleDetectedException.class)
	public void directCycle() throws Exception {
		new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("B", "A")
				.build();
	}

	@Test(expected=CycleDetectedException.class)
	public void indirectCycle() throws Exception {
		new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("C", "C")
				.addDependency("C", "A")
				.build();
	}

	@Test(expected=CycleDetectedException.class)
	public void branchingIndirectCycle() throws Exception {
		new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("A", "C")
				.addDependency("C", "A")
				.build();
	}

	@Test
	public void equality() throws Exception {
		Callable<DefineGraph> graphSupplier = () -> new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("C", "D")
				.addDependency("B", "E")
				.build();
		
		DefineGraph g1 = graphSupplier.call();
		DefineGraph g2 = graphSupplier.call();
		
		assertEquals(g1, g2);
	}

	@Test
	public void inequality() throws Exception {
		DefineGraph g1 = new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("C", "D")
				.addDependency("B", "E")
				.build();
		DefineGraph g2 = new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("C", "F")
				.addDependency("B", "E")
				.build();
		
		assertNotEquals(g1, g2);
	}

	@Test
	public void multiAddTypes() throws Exception {
		DefineGraph g1 = new DefineGraph.Builder()
				.addDependencies(new String[] { "A", "B" }, "C")
				.addDependency("C", "D")
				.build();
		DefineGraph g2 = new DefineGraph.Builder()
				.addDependency("A", "C")
				.addDependency("B", "C")
				.addDependency("C", "D")
				.build();
		
		assertEquals(g1, g2);
	}

	@Test
	public void multiAddDependsOnTypes() throws Exception {
		DefineGraph g1 = new DefineGraph.Builder()
				.addDependencies("A", new String[] { "B" , "C" })
				.addDependency("C", "D")
				.build();
		DefineGraph g2 = new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("A", "C")
				.addDependency("C", "D")
				.build();
		
		assertEquals(g1, g2);
	}

	@Test
	public void dependsOnDirect() throws Exception {
		DefineGraph g = new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("C", "D")
				.addDependency("B", "E")
				.build();
		
		assertTrue(g.testDependsOn("C", "D"));
	}

	@Test
	public void dependsOnIndirect() throws Exception {
		DefineGraph g = new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("C", "D")
				.addDependency("B", "E")
				.build();
		
		assertTrue(g.testDependsOn("A", "E"));
	}

	@Test
	public void notDependsOn() throws Exception {
		DefineGraph g = new DefineGraph.Builder()
				.addDependency("A", "B")
				.addDependency("C", "D")
				.addDependency("B", "E")
				.build();
		
		assertFalse(g.testDependsOn("C", "E"));
	}

	@Test
	public void notDependsOnReverseDependency() throws Exception {
		DefineGraph g = new DefineGraph.Builder()
				.addDependency("A", "B")
				.build();
		
		assertFalse(g.testDependsOn("B", "A"));
	}

}
