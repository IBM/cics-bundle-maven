package com.ibm.cics.cbmp;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Warbundle extends BundlePart {
	
	private static final String TYPE = CICSBundleMavenPlugin.NS + "/WARBUNDLE";
	
	private String name;
	private String jvmserver;
	private Artifact artifact;
	
	public Warbundle() {
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getJvmserver() {
		return jvmserver;
	}
	
	public void setJvmserver(String jvmserver) {
		this.jvmserver = jvmserver;
	}
	
	public Artifact getArtifact() {
		return artifact;
	}
	
	public void setArtifact(Artifact artifact) {
		this.artifact = artifact;
	}

	@Override
	public Define writeContent(File workDir, MavenProject project) throws MojoExecutionException {
		//copy war artifact from dependencies
		org.apache.maven.artifact.Artifact a = project
			.getArtifacts()
			.stream()
			.filter(artifact::matches)
			.collect(toSingleton());
		
		if (name == null) name = a.getArtifactId() + "-" + a.getVersion();
		try {
			FileUtils.copyFile(a.getFile(), new File(workDir, name + "." + a.getType()));
		} catch (IOException e) {
			throw new MojoExecutionException("Error copying war", e);
		}
		
		//write define
		if (jvmserver == null || "".equals(jvmserver)) throw new MojoExecutionException("JVM server was not supplied");
		
		Document document = CICSBundleMavenPlugin.DOCUMENT_BUILDER.newDocument();
		Element rootElement = document.createElement("warbundle");
		rootElement.setAttribute("symbolicname", a.getArtifactId());
		rootElement.setAttribute("jvmserver", jvmserver);
		document.appendChild(rootElement);
		
		String definePath = name + ".warbundle";
		File define = new File(workDir, definePath);
		
		try {
			CICSBundleMavenPlugin.TRANSFORMER.transform(
				new DOMSource(document),
				new StreamResult(define)
			);
		} catch (TransformerException e) {
			throw new MojoExecutionException("Error writing define", e);
		}
		
		return new Define(name, TYPE, definePath);
	}
	
	public static <T> Collector<T, ?, T> toSingleton() {
	    return Collectors.collectingAndThen(
            Collectors.toList(),
            list -> {
                if (list.size() != 1) {
                    throw new IllegalStateException();
                }
                return list.get(0);
            }
	    );
	}
	
}