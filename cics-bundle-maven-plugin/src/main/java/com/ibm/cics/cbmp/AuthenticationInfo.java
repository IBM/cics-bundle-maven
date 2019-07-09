package com.ibm.cics.cbmp;

public class AuthenticationInfo {
	
	private String username;
	private String password;
	private String privateKey;
	private String passphrase;
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getPrivateKey() {
		return privateKey;
	}
	
	public String getPassphrase() {
		return passphrase;
	}

}
