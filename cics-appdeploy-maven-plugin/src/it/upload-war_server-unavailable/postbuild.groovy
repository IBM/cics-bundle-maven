/*-
 * #%L
 * CICS AppDeploy Maven Plugin
 * %%
 * Copyright (C) 2026 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
context.get("wireMockServer").shutdownServer()

File buildLog = new File(basedir, 'build.log')

assert buildLog.exists()
assert buildLog.text.contains("[ERROR] Failed to execute goal ${pluginString}:upload-war")
assert buildLog.text.contains("Upload failed with code 503: Rejected by test server")

// A 5xx response can be temporary, so the plugin must try again before it gives up
assert buildLog.text.contains("Upload attempt 1 failed")
assert buildLog.text.contains("Upload attempt 2 failed")
assert buildLog.text.contains("Upload failed after 3 attempts")
