package com.ibm.cics.cbmp;

/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2018 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.mojo.buildhelper.versioning.VersionInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Mojo(name = "build", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.COMPILE)
public class BuildCICSBundleMojo extends AbstractCICSBundleMojo {
    
	static final DocumentBuilder DOCUMENT_BUILDER;
	static final Transformer TRANSFORMER;

	static {
		try {
			DOCUMENT_BUILDER = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			TRANSFORMER = TransformerFactory.newInstance().newTransformer();
			TRANSFORMER.setOutputProperty(OutputKeys.INDENT, "yes");
			TRANSFORMER.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
		} catch (ParserConfigurationException | TransformerConfigurationException | TransformerFactoryConfigurationError e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
    String getDefaultJVMServer() {
    	return defaultjvmserver;
    }

    private Define writeBundlePart(Artifact a) {
    	return bundleParts
    		.stream()
    		.filter(bp -> bp.matches(a))
    		.findFirst()
    		.orElse(getDefaultBundlePart(a))
    		.writeBundlePart(workDir, a, f -> buildContext.refresh(f));
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
    	getLog().info("Running CICS Bundle build");
    	
    	getLog().debug(buildContext.isIncremental() ? "Build is incremental" : "Build is full");
    	
    	if (!shouldBuild()) {
    		getLog().debug("No changes detected. Will not continue build.");
    		return;
    	}
    	
    	if (workDir.exists()) {
    		getLog().debug("Deleting " + workDir);
    		try {
				FileUtils.deleteDirectory(workDir);
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to delete CICS bundle output directory " + workDir, e);
			}
    	}
    	
    	workDir.mkdirs();
    	
    	try {
	    	List<Define> defines = project
				.getArtifacts()
				.stream()
				.filter(BuildCICSBundleMojo::isArtifactBundleable)
				.map(this::writeBundlePart)
				.collect(Collectors.toList());
	    	
	    	VersionInformation v = new VersionInformation(project.getVersion());
	    	
	    	writeManifest(
	    		defines,
	    		project.getArtifactId(),
	    		v.getMajor(),
	    		v.getMinor(),
	    		v.getPatch(),
	    		v.getBuildNumber()
	    	);
    	} catch (MojoExecutionRuntimeException e) {
    		throw new MojoExecutionException(e.getMessage(), e);
    	}
    	
    	getLog().info("Refreshing "+workDir);
    	buildContext.refresh(workDir);
    }

	private boolean shouldBuild() {
		return !buildContext.isIncremental() || 
				// Expand this list of locations as we become interested in them
				buildContext.hasDelta("pom.xml");
	}

	private void writeManifest(List<Define> defines, String id, int major, int minor, int micro, int release) throws MojoExecutionException {
		Document d = DOCUMENT_BUILDER.newDocument();
		Element root = d.createElementNS(BundleConstants.NS, "manifest");
		root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", BundleConstants.NS);
		root.setAttribute("id", id);
		
		root.setAttribute("bundleMajorVer", String.valueOf(major));
		root.setAttribute("bundleMinorVer", String.valueOf(minor));
		root.setAttribute("bundleMicroVer", String.valueOf(micro));
		root.setAttribute("bundleRelease", String.valueOf(release));
		root.setAttribute("bundleVersion", "1");
		
		d.appendChild(root);
		
		Element metaDirectives = d.createElementNS(BundleConstants.NS, "meta_directives");
		root.appendChild(metaDirectives);
		
		Element timestamp = d.createElementNS(BundleConstants.NS, "timestamp");
		metaDirectives.appendChild(timestamp);
		timestamp.setTextContent(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
		
		for (Define define : defines) {
			Element defineElement = d.createElementNS(BundleConstants.NS, "define");
			defineElement.setAttribute("name", define.getName());
			defineElement.setAttribute("type", define.getType());
			defineElement.setAttribute("path", define.getPath());
			root.appendChild(defineElement);
		}

		File metaInf = new File(workDir, "META-INF");
		metaInf.mkdirs();
		File manifest = new File(metaInf, "cics.xml");
		
		try {
			TRANSFORMER.transform(
				new DOMSource(d),
				new StreamResult(manifest)
			);
		} catch (TransformerException e) {
			throw new MojoExecutionException("Error writing cics.xml", e);
		}
		
	}

}
