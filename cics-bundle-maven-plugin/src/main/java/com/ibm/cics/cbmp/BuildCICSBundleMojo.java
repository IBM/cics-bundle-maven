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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.buildhelper.versioning.DefaultVersioning;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Mojo(name = "build", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.COMPILE)
public class BuildCICSBundleMojo extends AbstractMojo {
	
	private static final String EAR = "ear";
	private static final String WAR = "war";
	private static final String JAR = "jar";
    
    private static final Set<String> BUNDLEABLE_TYPES = new HashSet<>(Arrays.asList(WAR, EAR, JAR));
	
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
	
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;
	
	@Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    private File buildDir;
    
    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}", required = true, readonly = true)
    private File workDir;
    
    @Parameter(required = false, readonly = false)
    private String classifier;
    
    @Parameter(required = false)
    private List<BundlePart> bundleParts = Collections.emptyList();
    
    @Parameter(defaultValue = "MYJVMS", required = false, readonly = false)
	private String defaultjvmserver;
    
    @Component
	private MavenProjectHelper projectHelper;
    
    private static boolean isArtifactBundleable(Artifact a) {
    	return BUNDLEABLE_TYPES.contains(a.getType());
    }
    
    private BundlePart getDefaultBundlePart(Artifact a) {
		getLog().info("Building bundle part for " + a.getId() + "(" + a.getType() + ")");
		switch (a.getType()) {
	    	default: throw new RuntimeException("Unsupported bundle part type:" + a.getType());
	    	case WAR: {
	    		Warbundle warbundle = new Warbundle();
	    		warbundle.setJvmserver(defaultjvmserver);
	    		warbundle.setArtifact(new com.ibm.cics.cbmp.Artifact(a));
	    		return warbundle;
	    	}
	    	case EAR: {
	    		Earbundle earbundle = new Earbundle();
	    		earbundle.setJvmserver(defaultjvmserver);
	    		earbundle.setArtifact(new com.ibm.cics.cbmp.Artifact(a));
	    		return earbundle;
	    	}
	    	case JAR: {
	    		Osgibundle osgibundle = new Osgibundle();
	    		osgibundle.setJvmserver(defaultjvmserver);
	    		osgibundle.setArtifact(new com.ibm.cics.cbmp.Artifact(a));
	    		return osgibundle;
	    	}
    	}
    }
    
    private Define writeBundlePart(Artifact a) {
    	return bundleParts
    		.stream()
    		.filter(bp -> bp.matches(a))
    		.findFirst()
    		.orElse(getDefaultBundlePart(a))
    		.writeContent(workDir, a);
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
    	getLog().info("Running CICS Bundle build");
    	if (workDir.exists()) workDir.delete();
    	
    	workDir.mkdirs();
    	
    	try {
	    	List<Define> defines = project
				.getArtifacts()
				.stream()
				.filter(BuildCICSBundleMojo::isArtifactBundleable)
				.map(this::writeBundlePart)
				.collect(Collectors.toList());
	    	
	    	DefaultVersioning v = new DefaultVersioning(project.getVersion());
	    	
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
    	
    	try {
    		ZipArchiver zipArchiver = new ZipArchiver();
    		zipArchiver.addDirectory(workDir);
    		File cicsBundle = new File(buildDir, project.getArtifactId() + "-" + project.getVersion() + (classifier != null ? "-" + classifier : "") + ".cics-bundle");
    		zipArchiver.setDestFile(cicsBundle);
			zipArchiver.createArchive();
    	
	        if (classifier != null) {
	            projectHelper.attachArtifact(project, "cics-bundle", classifier, cicsBundle);
	        } else {
	        	File artifactFile = project.getArtifact().getFile();
				if (artifactFile != null && artifactFile.isFile()) {
					//We already attached an artifact to this project in another mojo, don't override it!
					throw new MojoExecutionException("Set classifier when there's already an artifact attached, to prevent overwriting the main artifact");
	            } else {            	
	            	project.getArtifact().setFile(cicsBundle);
	            }
	        }
    	} catch (ArchiverException | IOException e) {
    		throw new MojoExecutionException("Failed to create cics bundle archive", e);
    	}
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
