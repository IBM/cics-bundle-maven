/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2022 IBM Corp.
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
assert buildLog.text.contains("[ERROR] Failed to execute goal com.ibm.cics:cics-bundle-maven-plugin:")
assert buildLog.text.contains("(deploy) on project test-bundle-deploy-noCICSplex: Specify both or neither of cicsplex and region in plugin configuration or server configuration")
