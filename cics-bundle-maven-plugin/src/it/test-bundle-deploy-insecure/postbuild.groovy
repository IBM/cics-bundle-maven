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
// Deployment with <insecure> set
assert buildLog.text.contains("""\
(deploy-with-insecure) @ test-bundle-deploy-insecure ---
[INFO] Deploying bundle to https://localhost:${wiremockPort} into region cicsplex/region
[INFO] Bundle deployed
""")

// Deployment without <insecure> set
assert buildLog.text.contains("""\
(deploy-and-fail-because-self-signed) @ test-bundle-deploy-insecure ---
[INFO] Deploying bundle to https://localhost:${wiremockPort} into region cicsplex/region
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
""")