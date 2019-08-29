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
import java.util.regex.*

File buildLog = new File(basedir, 'build.log')

assert buildLog.exists()

assert buildLog.text.contains("[ERROR] Failed to execute goal com.ibm.cics:cics-bundle-maven-plugin:")
assert buildLog.text.contains("Some bundle part overrides did not correspond to any of the project artifacts: [Earbundle: Artifact [groupId=com.ibm.cics, artifactId=banana, version=1.2.3, type=blob, classifier=apple]]")