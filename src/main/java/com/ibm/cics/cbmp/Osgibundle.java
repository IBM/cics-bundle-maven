package com.ibm.cics.cbmp;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Osgibundle extends BundlePart {
	
	private static final String TYPE = BundleConstants.NS + "/OSGIBUNDLE";
	
	private String name;
	private String jvmserver;
	private Artifact artifact;
	
	public Osgibundle() {
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
	public Define writeContent(File workDir, org.apache.maven.artifact.Artifact a) throws MojoExecutionRuntimeException {
		if (name == null) name = a.getArtifactId() + "-" + a.getVersion();
		try {
			FileUtils.copyFile(a.getFile(), new File(workDir, name + "." + a.getType()));
		} catch (IOException e) {
			throw new MojoExecutionRuntimeException("Error copying osgi", e);
		}
		
		//write define
		if (jvmserver == null || "".equals(jvmserver)) throw new MojoExecutionRuntimeException("JVM server was not supplied");
		
		Document document = BuildCICSBundleMojo.DOCUMENT_BUILDER.newDocument();
		Element rootElement = document.createElement("osgibundle");
		rootElement.setAttribute("symbolicname", a.getArtifactId());
		rootElement.setAttribute("jvmserver", jvmserver);
		document.appendChild(rootElement);
		
		String definePath = name + ".osgibundle";
		File define = new File(workDir, definePath);
		
		try {
			BuildCICSBundleMojo.TRANSFORMER.transform(
				new DOMSource(document),
				new StreamResult(define)
			);
		} catch (TransformerException e) {
			throw new MojoExecutionRuntimeException("Error writing define", e);
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