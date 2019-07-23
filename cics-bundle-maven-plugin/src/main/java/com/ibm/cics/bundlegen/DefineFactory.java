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

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefineFactory {
	
	public interface Log {
		public void log(String string);
	}
	
	private static Map<String, String> fileExtensionToType = Stream.of(new String[][] {
		  { "pipeline", uri("PIPELINE") }, 
		  { "packageset", uri("PACKAGESET") }, 
		  { "epadapter", uri("EPADAPTER") }, 
		  { "epadapterset", uri("EPADAPTERSET") }, 
		  { "file", uri("FILE") }, 
		  { "library", uri("LIBRARY") }, 
		  { "policy", uri("POLICY") }, 
		  { "program", uri("PROGRAM") }, 
		  { "tcpipservice", uri("TCPIPSERVICE") }, 
		  { "transaction", uri("TRANSACTION") }, 
		  { "urimap", uri("URIMAP") }, 
		  { "evbind", uri("EVENTBINDING") }, 
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
	
	public static Optional<Define> createDefine(File file, Log log) {
		Optional<String> type = getBundlePartTypeUri(file, log);
		String fileName = file.getName();

		// only create defines for the cics.xml for bundle parts that we recognise and support
		int dot = fileName.lastIndexOf(".");
		String fileNameRoot = dot > 0 ? fileName.substring(0, dot) : fileName;
		return (type.isPresent()) ? Optional.of(new Define(fileNameRoot, type.get(), fileName)) : Optional.empty();
	}
	
	private static Optional<String> getBundlePartTypeUri(File f, Log log) {
		// not supported - atom config (xml file)
		// not supported - JVMSERVER (jvmprofile has to be in EBCDIC...?)
		// not supported - jsonapp
		// not supported - web service (didn't yet test but wsbind is binary and WSDL should be transferred as-is)
		String fileName = f.getName();
		int dot = fileName.lastIndexOf(".");
		if (dot > 0) {
			String extension = fileName.substring(dot + 1).toLowerCase();
			boolean contained = fileExtensionToType.containsKey(extension);
			if(contained) {
				return Optional.of(fileExtensionToType.get(extension));
			} else {
				// likely an unsupported type or some other arbitrary file
				log.log("File " + fileName + " not being treated as a bundle part.");
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}
	
	public static String uri(String typeSuffix) {
		return BundleConstants.NS + "/" + typeSuffix;
	}
	
}
