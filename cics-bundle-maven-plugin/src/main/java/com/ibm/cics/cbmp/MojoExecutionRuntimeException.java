package com.ibm.cics.cbmp;

public class MojoExecutionRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public MojoExecutionRuntimeException(String message) {
		super(message);
	}
	
	public MojoExecutionRuntimeException(String message, Throwable e) {
		super(message, e);
	}

}
