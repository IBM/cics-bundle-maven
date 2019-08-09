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

@Mojo(name = "deploy", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.VERIFY)
public class BundleDeployMojo extends AbstractMojo {
	
	/**
	 * The current user system settings for use in Maven.
	 */
	@Parameter( defaultValue = "${settings}", readonly = true )
	protected Settings settings;
	
	@Parameter(required = true)
	private String bunddef;
	
	@Parameter(required = true)
	private String csdgroup;
	
	@Parameter(required = true)
	private String serverId;
	
	@Parameter
	private String cicsplex;
	
	@Parameter
	private String region;

	@Parameter
	private String bundle;
	
    @Parameter(property = "project", readonly = true)
    private MavenProject project;

    @Component
    private SettingsDecrypter settingsDecrypter;


	@Override
	public void execute() throws MojoExecutionException {
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
		ServerConfig serverConfig = getServerConfig(server);
		
		String cicsplexResolved = cicsplex != null ? cicsplex : serverConfig.getCicsplex();
		if (cicsplexResolved == null || cicsplexResolved.isEmpty()) throw new MojoExecutionException("cicsplex must be specified either in plugin configuration or server configuration");
		
		String regionResolved = region != null ? region : serverConfig.getRegion();
		if (regionResolved == null || regionResolved.isEmpty()) throw new MojoExecutionException("region must be specified either in plugin configuration or server configuration");
		
		try {
			BundleDeployHelper.deployBundle(
				serverConfig.getEndpointUrl(),
				getBundle(),
				bunddef,
				csdgroup,
				cicsplexResolved,
				regionResolved,
				authenticationInfo.getUsername(),
				authenticationInfo.getPassword()
			);
		} catch (BundleDeployException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
	
    private AuthenticationInfo getAuthenticationInfo(Server server) {
		AuthenticationInfo authInfo = new AuthenticationInfo();
		authInfo.setUsername(server.getUsername());
		authInfo.setPassword(server.getPassword());
		authInfo.setPrivateKey(server.getPrivateKey());
		authInfo.setPassphrase(server.getPassphrase());
		return authInfo;
    }
    
    private ServerConfig getServerConfig(Server server) throws MojoExecutionException {
		ServerConfig serverConfig = new ServerConfig();
		Object configuration = server.getConfiguration();
		
		if (configuration == null) {
			throw new MojoExecutionException("Server didn't specify any configuration.  URL is mandatory");
		}
		
		if (configuration instanceof Xpp3Dom) {
			Xpp3Dom c = (Xpp3Dom) configuration;
			
			Xpp3Dom endpointUrl = c.getChild("url");
			if (endpointUrl != null) {
				try {
					serverConfig.setEndpointUrl(new URI(endpointUrl.getValue()));
				} catch (URISyntaxException e) {
					throw new MojoExecutionException("Endpoint URL is invalid", e);
				}
			} else {
				throw new MojoExecutionException("No endpoint URL set");
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
		return serverConfig;
    }
    
    private File getBundle() throws MojoExecutionException {
		if (bundle != null) {
			return new File(bundle);
		} else {
			if (project != null && project.getArtifact() != null) {
				if ("cics-bundle".equals(project.getPackaging())) {
					File file = project.getArtifact().getFile();
					if (file != null) {
						return file;
					} else {
						throw new MojoExecutionException("CICS bundle not found");
					}
		        } else {
		        		throw new MojoExecutionException("Unsupported packaging type: " + project.getPackaging());
		        }
			} else {
				throw new MojoExecutionException("Project artifact not found");
			}
		}
    }

}
