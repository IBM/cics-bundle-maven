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
assert buildLog.text.contains("[ERROR] Failed to execute goal ${pluginString}:build (default-build) on project test-bundle: Bundle part for artifact com.ibm.cics.test-reactor-osgi-nodefaultjvmserver:test-osgi:jar:0.0.1-SNAPSHOT:compile did not specify a JVM server explicitly, and no default was configured")