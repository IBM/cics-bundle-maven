package com.ibm.cics.cbmp;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

import org.apache.maven.MavenExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.buildhelper.versioning.DefaultVersioning;
import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Mojo(name = "build", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.COMPILE)
public class CICSBundleMavenPlugin extends MojoSupport {
	
	static final String NS = "http://www.ibm.com/xmlns/prod/cics/bundle";
	
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
    
    @Parameter(required = false)
    private List<BundlePart> bundleParts;
    
    @Component
	private MavenProjectHelper projectHelper;
    
    @Override
    protected void doExecute() throws Exception {
    	if (workDir.exists()) workDir.delete();
    	
    	workDir.mkdirs();
    	
    	List<Define> defines = new ArrayList<>();
    	if (bundleParts != null && !bundleParts.isEmpty()) {
	    	for (BundlePart bundlePart : bundleParts) {
	    		defines.add(bundlePart.writeContent(workDir, project));
			}
    	} else {
    		log.info("No bundle parts defined");
    	}
    	
    	DefaultVersioning v = new DefaultVersioning(project.getVersion());
    	
    	writeManifest(
    		defines,
    		project.getArtifactId(),
    		v.getMajor(),
    		v.getMinor(),
    		v.getPatch(),
    		v.getBuildNumber()
    	);
    	
    	ZipArchiver zipArchiver = new ZipArchiver();
    	zipArchiver.addDirectory(workDir);
    	File cicsBundle = new File(buildDir, project.getArtifactId() + "-" + project.getVersion() + ".cicsbundle");
		zipArchiver.setDestFile(cicsBundle);
    	zipArchiver.createArchive();
    	
    	projectHelper.attachArtifact(project, "cicsbundle", cicsBundle);
    }

	private void writeManifest(List<Define> defines, String id, int major, int minor, int micro, int release) throws MavenExecutionException {
		Document d = DOCUMENT_BUILDER.newDocument();
		Element root = d.createElementNS(NS, "manifest");
		root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", NS);
		root.setAttribute("id", id);
		
		root.setAttribute("bundleMajorVer", String.valueOf(major));
		root.setAttribute("bundleMinorVer", String.valueOf(minor));
		root.setAttribute("bundleMicroVer", String.valueOf(micro));
		root.setAttribute("bundleRelease", String.valueOf(release));
		root.setAttribute("bundleVersion", "1");
		
		d.appendChild(root);
		
		Element metaDirectives = d.createElementNS(NS, "meta_directives");
		root.appendChild(metaDirectives);
		
		Element timestamp = d.createElementNS(NS, "timestamp");
		metaDirectives.appendChild(timestamp);
		timestamp.setTextContent(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
		
		for (Define define : defines) {
			Element defineElement = d.createElementNS(NS, "define");
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
			throw new MavenExecutionException("Error writing cics.xml", e);
		}
		
	}

}
