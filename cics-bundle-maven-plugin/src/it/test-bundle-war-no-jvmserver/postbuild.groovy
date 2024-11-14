/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2024 IBM Corp.
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
assert buildLog.text.contains("The parameters 'jvmserver' for goal ${pluginString}:bundle-war are missing or invalid")