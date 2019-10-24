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
import com.ibm.cics.cbmp.DeployPreBuild

context.get("wireMockServer").shutdownServer()

File warBundle = new File(basedir, 'target/test-bundle-war-0.0.1-SNAPSHOT-cics-bundle/test-bundle-war-0.0.1-SNAPSHOT.warbundle')

assert warBundle.exists()
assert warBundle.text.contains("jvmserver=\"UPROPJVM\"")