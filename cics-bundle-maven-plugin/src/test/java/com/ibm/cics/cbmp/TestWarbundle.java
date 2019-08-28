package com.ibm.cics.cbmp;

public class TestWarbundle extends AbstractNameableJavaBundlePartBindingTestCase {

	@Override
	protected AbstractNameableJavaBundlePartBinding createBinding() {
		return new Warbundle();
	}

	@Override
	protected String getRootElementName() {
		return "warbundle";
	}

}
