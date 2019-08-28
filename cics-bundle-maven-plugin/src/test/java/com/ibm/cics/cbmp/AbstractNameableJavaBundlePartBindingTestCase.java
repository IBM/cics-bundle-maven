package com.ibm.cics.cbmp;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractNameableJavaBundlePartBindingTestCase extends AbstractJavaBundlePartBindingTestCase {
	
	@Before
	public void defaultName() {
		setExpectedSymbolicName(artifact.getArtifactId() + "-" + artifact.getVersion());
	}
	
	@Test
	public void nameOverride() throws Exception {
		((AbstractNameableJavaBundlePartBinding) binding).setName("bananas");
		
		setExpectedSymbolicName("bananas");
		assertBundleResources();
	}
	
	@Override
	protected abstract AbstractNameableJavaBundlePartBinding createBinding();
	
}
