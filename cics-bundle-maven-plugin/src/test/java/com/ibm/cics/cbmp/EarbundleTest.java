package com.ibm.cics.cbmp;

public class EarbundleTest extends AbstractNameableJavaBundlePartBindingTestCase {

	@Override
	protected AbstractNameableJavaBundlePartBinding createBinding() {
		return new Earbundle();
	}

	@Override
	protected String getRootElementName() {
		return "earbundle";
	}

}
