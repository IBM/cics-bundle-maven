package com.ibm.cics.cbmp;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.matchers.CompareMatcher;

import com.ibm.cics.bundle.parts.BundleResource;

public abstract class AbstractJavaBundlePartBindingTestCase {

	public static final String COM_EXAMPLE = "com.example";
	public static final String ARTIFACT_ID = "the-artifact-id";
	public static final String GROUP_ID = "1.0.0-SNAPSHOT";

	private AbstractCICSBundleMojo mojo;

	private Map<String, String> defineAttributes = new HashMap<>();
	
	protected org.apache.maven.artifact.Artifact artifact;
	protected AbstractJavaBundlePartBinding binding;
	
	protected abstract AbstractJavaBundlePartBinding createBinding();
	protected abstract String getRootElementName();
	
	@Before
	public void setUp() {
		binding = createBinding();
		mojo = mock(AbstractCICSBundleMojo.class);
		
		artifact = mock(org.apache.maven.artifact.Artifact.class);
		
		when(artifact.getArtifactId()).thenReturn(ARTIFACT_ID);
		when(artifact.getVersion()).thenReturn(GROUP_ID);
		when(artifact.getGroupId()).thenReturn(COM_EXAMPLE);
	}
	
	@Before
	public void defaultJvmServer() {
		when(mojo.getDefaultJVMServer()).thenReturn("MYJVMS");
		setExpectedJVMServer("MYJVMS");
	}
	
	protected void setExpectedJVMServer(String jvmServer) {
		this.defineAttributes.put("jvmserver", jvmServer);
	}
	
	protected void setExpectedSymbolicName(String symbolicname) {
		this.defineAttributes.put("symbolicname", symbolicname);
	}
	
	protected void setOtherExpectedAttributes(Map<String, String> otherAttributes) {
		this.defineAttributes.putAll(otherAttributes);
	}
	
	protected void setArtifact(org.apache.maven.artifact.Artifact artifact) {
		this.artifact = artifact;
	}
	
	protected void assertBundleResources() throws Exception {
		//assert bundle part
		BundleResource br = binding.toBundlePart(artifact, mojo);
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document actualBundlePart = documentBuilder.parse(br.getContent());
		
		Document expectedBundlePart = documentBuilder.newDocument();
		Element element = expectedBundlePart.createElement(getRootElementName());
		expectedBundlePart.appendChild(element);
		this.defineAttributes.forEach(element::setAttribute);
		
		assertThat(
			actualBundlePart,
			CompareMatcher
				.isIdenticalTo(
					expectedBundlePart
				).withDifferenceEvaluator(
					DifferenceEvaluators.chain(
						DifferenceEvaluators.ignorePrologDifferencesExceptDoctype(),
						DifferenceEvaluators.Default
					)
				)
			);
		
		//assert dynamic resources
		br.getDynamicResources();
	}
	
	@Test
	public void defaults() throws Exception {
		assertBundleResources();
	}
	
	@Test
	public void jvmServerOverride() throws Exception {
		binding.setJvmserver("OJVMS");
		setExpectedJVMServer("OJVMS");
		
		assertBundleResources();
	}
}
