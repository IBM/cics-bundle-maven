package com.ibm.cics.cbmp;

public class TestEarbundle extends AbstractNameableJavaBundlePartBindingTestCase {

	@Override
	protected AbstractNameableJavaBundlePartBinding createBinding() {
		return new Earbundle();
	}

	@Override
	protected String getRootElementName() {
		return "earbundle";
	}

}
