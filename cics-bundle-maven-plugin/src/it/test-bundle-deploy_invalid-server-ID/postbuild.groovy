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

File buildLog = new File(basedir, 'build.log')

assert buildLog.exists()
assert buildLog.text.contains("[ERROR] Failed to execute goal com.ibm.cics:cics-bundle-maven-plugin:0.0.1-SNAPSHOT:deploy (deploy) on project test-bundle-deploy: Server 'INVALID' does not exist")
assert buildLog.text.contains("Caused by: org.apache.maven.plugin.MojoExecutionException: Server 'INVALID' does not exist")