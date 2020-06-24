package com.ibm.cics.cbmp;

/*-
 * #%L
 * CICS Bundle Maven Plugin
 * %%
 * Copyright (C) 2019 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.ibm.cics.bundle.deploy.BundleDeployException;
import com.ibm.cics.bundle.deploy.BundleDeployHelper;

/**
 * <p>This mojo deploys a CICS bundle to the specified CICS region using the CICS bundle deployment API. A matching bundle definition must be provided in the CSD in advance.</p>
 * <p>The <code>deploy</code> goal is not bound by default, so will not run unless specifically configured. You might choose to configure the <code>deploy</code> goal to run inside a specific profile, so that
 * a developer can choose whether to deploy their bundle with a command-line parameter to switch Maven profiles.</p>
 */
@Mojo(name = "deploy", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.VERIFY)
public class BundleDeployMojo extends AbstractMojo {
	
	/**
	 * The current user system settings for use in Maven.
	 */
	@Parameter( defaultValue = "${settings}", readonly = true )
	protected Settings settings;
	
	/**
	 * The name of the bundle definition that will install this bundle. Must be present already in the CSD relating
	 * to the configured cicsplex/region, and must be configured with the correct bundle directory according to the 
	 * bundle deployment API configuration and the name of the bundle the user is deploying.
	 */
	@Parameter(property="cicsbundle.bunddef", required = true)
	private String bunddef;
	
	/**
	 * The CSD group containing the bundle definition to be installed.
	 */
	@Parameter(property="cicsbundle.csdgroup", required = true)
	private String csdgroup;
	
	/**
	 * The ID of a server configured in your Maven settings
	 */
	@Parameter
	private String serverId;
	
	/**
	 * The name of the CICSplex the bundle should be installed into.
	 * Specifying this parameter overrides any value provided within a Maven settings server entry.
	 */
	@Parameter(property="cicsbundle.cicsplex")
	private String cicsplex;
	
	/**
	 * The name of the region the bundle should be installed into.
	 * Specifying this parameter overrides any value provided within a Maven settings server entry.
	 */
	@Parameter(property="cicsbundle.region")
	private String region;

	/**
	 * The filename of the bundle archive file to be deployed.
	 */
	@Parameter(property="cicsbundle.bundle")
	private String bundle;
	
	/**
	 * The classifier of a bundle attached to this project which is to be deployed.
	 * If a value for the @bundle parameter is supplied, this classifier is ignored.
	 */
	@Parameter(property="cicsbundle.classifier")
	private String classifier;
	
	/**
	 * The CMCI URL that the CICS bundle deployment API is available on. For example `https://yourcicshost.com:9080`.
	 * Specifying this parameter overrides any value provided within a Maven settings server entry.
	 */
	@Parameter(property="cicsbundle.url")
	private String url;
	
	/**
	 * The username to authenticate with.
	 * Specifying this parameter overrides any value provided within a Maven settings server entry.
	 */
	@Parameter(property="cicsbundle.username")
	private String username;
	
	/**
	 * When connecting to the CICS bundle deployment API, do not check that the trust chain of the host certificate is valid, 
	 * do not verify that the host name of certificate matches host name of server.
	 */
	@Parameter(property="cicsbundle.insecure", defaultValue = "false")
	private boolean insecure;
	
	/**
	 * The password to authenticate with.
	 * Specifying this parameter overrides any value provided within a Maven settings server entry.
	 */
	@Parameter(property="cicsbundle.password") 
	private String password;
	
	@Parameter(property = "project", readonly = true)
	private MavenProject project;

	@Component
	private SettingsDecrypter settingsDecrypter;

	@Override
	public void execute() throws MojoExecutionException {
		ServerConfig serverConfig = getServerConfig();

		//Override settings.xml with pom configuration
		if (url != null) serverConfig.setEndpointUrl(parseURL(url));
		if (cicsplex != null) serverConfig.setCicsplex(cicsplex);
		if (region != null) serverConfig.setRegion(region);
		if (username != null) serverConfig.setUsername(username);
		if (password != null) serverConfig.setPassword(getPasswordAsChars(password));
		serverConfig.setAllowSelfSignedCertificate(insecure);
		
		//Validate mandatory configuration
		if (serverConfig.getEndpointUrl() == null) throw new MojoExecutionException("url must be specified either in plugin configuration or server configuration");
		if (StringUtils.isEmpty(serverConfig.getCicsplex())) throw new MojoExecutionException("cicsplex must be specified either in plugin configuration or server configuration");
		if (StringUtils.isEmpty(serverConfig.getRegion())) throw new MojoExecutionException("region must be specified either in plugin configuration or server configuration");
		
		getLog().info("Deploying bundle to " + serverConfig.getEndpointUrl().toASCIIString() + " into region " + serverConfig.getCicsplex() + "/" + serverConfig.getRegion());
		try {
			BundleDeployHelper.deployBundle(
				serverConfig.getEndpointUrl(),
				getBundle(),
				bunddef,
				csdgroup,
				serverConfig.getCicsplex(),
				serverConfig.getRegion(),
				serverConfig.getUsername(),
				serverConfig.getPassword(),
				serverConfig.isAllowSelfSignedCertificate()
			);
		} catch (BundleDeployException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
		getLog().info("Bundle deployed");
	}
	
	private AuthenticationInfo getAuthenticationInfo(Server server) throws MojoExecutionException {
		AuthenticationInfo authInfo = new AuthenticationInfo();
		authInfo.setUsername(server.getUsername());
		authInfo.setPassword(getPasswordAsChars(server.getPassword()));
		authInfo.setPrivateKey(server.getPrivateKey());
		authInfo.setPassphrase(server.getPassphrase());
		return authInfo;
	}
	
	private ServerConfig getServerConfig() throws MojoExecutionException {
		ServerConfig serverConfig = new ServerConfig();
		if (serverId != null) {
			Server encryptedServer = settings.getServer(serverId);
			Server server;
			if (encryptedServer != null) {
				server = settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(encryptedServer)).getServer();
				if (server == null) {
					throw new MojoExecutionException("Server ID is null");
				}
			} else {
				throw new MojoExecutionException("Server '" + serverId + "' does not exist");
			}
			
			AuthenticationInfo authenticationInfo = getAuthenticationInfo(server);
			serverConfig.setUsername(authenticationInfo.getUsername());
			serverConfig.setPassword(authenticationInfo.getPassword());
			
			Object configuration = server.getConfiguration();
			
			if (configuration instanceof Xpp3Dom) {
				Xpp3Dom c = (Xpp3Dom) configuration;
				
				Xpp3Dom endpointUrl = c.getChild("url");
				if (endpointUrl != null) {
					serverConfig.setEndpointUrl(parseURL(endpointUrl.getValue()));
				}
				
				Xpp3Dom cicsplex = c.getChild("cicsplex");
				if (cicsplex != null) {
					serverConfig.setCicsplex(cicsplex.getValue());
				}
				
				Xpp3Dom region = c.getChild("region");
				if (region != null) {
					serverConfig.setRegion(region.getValue());
				}
			} else {
				throw new MojoExecutionException("Unknown server configuration format: " + configuration.getClass());
			}
		}
		return serverConfig;
	}

	private static URI parseURL(String x) throws MojoExecutionException {
		try {
			return new URI(x);
		} catch (URISyntaxException e) {
			throw new MojoExecutionException("URL is invalid", e);
		}
	}
	
    private File getBundle() throws MojoExecutionException {
		if (bundle != null) {
			return new File(bundle);
		} else {
			Artifact artifact;
			if (classifier != null) {
				artifact = project
					.getAttachedArtifacts()
					.stream()
					.filter(a -> classifier.equals(a.getClassifier()))
					.findFirst()
					.orElse(null);
			} else {
				artifact = project.getArtifact();
			}
			
			if (artifact == null) {
				throw new MojoExecutionException("Artifact not found");
			}

			//TODO: have a look inside the artifact to see if it contains a cics.xml instead of validating the packaging type
			File file = artifact.getFile();
			if (file != null) {
				return file;
			} else {
				throw new MojoExecutionException("CICS bundle not found");
			}
		}
	}

	/*
	 * Current best practice is to avoid putting strings containing passwords onto
	 *  the heap or interning the strings containing them, hence using char[] where possible
	 */
	public char[] getPasswordAsChars(String passwordString) {
		char[] password = new char[0];
		
		if (passwordString != null && !passwordString.isEmpty()) {
			return passwordString.toCharArray();
		}
		return password;
	}
	
}
