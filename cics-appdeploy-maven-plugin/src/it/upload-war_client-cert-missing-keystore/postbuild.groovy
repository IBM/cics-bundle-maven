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

File buildLog = new File(basedir, 'build.log')

assert buildLog.exists()
assert buildLog.text.contains("[ERROR] Failed to execute goal ${pluginString}:upload-war")
assert buildLog.text.contains("clientKeyStore does not exist")
