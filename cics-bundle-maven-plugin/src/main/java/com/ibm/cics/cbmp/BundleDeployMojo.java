package com.ibm.cics.cbmp;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
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
	private String bundleName;
	
	@Parameter(required = true)
	private String csdGroup;
	
	@Parameter(required = true)
	private String serverId;

	@Parameter(required = true)
	private String bundleFilePath;
	
    @Component
    private SettingsDecrypter settingsDecrypter;
    
    /**
     * For unit test to pass through the parameters
     */
    public BundleDeployMojo(Settings settings, String bundleName, String csdGroup, String serverId, String bundleFilePath, SettingsDecrypter settingsDecrypter) {
		this.settings = settings;
		this.bundleName = bundleName;
		this.csdGroup = csdGroup;
		this.serverId = serverId;
		this.bundleFilePath = bundleFilePath;
		this.settingsDecrypter = settingsDecrypter;
	}

	@Override
	public void execute() throws MojoExecutionException {
		Server encryptedServer = settings.getServer(serverId);
		Server server;
		if (encryptedServer != null) {
			server = settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(encryptedServer)).getServer();
			if (server == null) {
				throw new MojoExecutionException("Server '" + serverId + "' is null");
			}
		} else {
			throw new MojoExecutionException("Server '" + serverId + "' does not exist");
		}
		
		AuthenticationInfo authenticationInfo = getAuthenticationInfo(server);
		ServerConfig serverConfig = getServerConfig(server);
		
		try {
			BundleDeployHelper.deployBundle(serverConfig.getEndpointUrl(), new File(bundleFilePath), bundleName, csdGroup, serverConfig.getCicsplexName(), serverConfig.getRegionName(), authenticationInfo.getUsername(), authenticationInfo.getPassword());
		} catch (BundleDeployException e) {
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
		if (configuration instanceof Xpp3Dom) {
			Xpp3Dom c = (Xpp3Dom) configuration;
			
			Xpp3Dom endpointUrl = c.getChild("url");
			if (endpointUrl != null) {
				serverConfig.setEndpointUrl(endpointUrl.getValue());
			} else {
				throw new MojoExecutionException("No endpoint URL set");
			}
			
			Xpp3Dom cicsplexName = c.getChild("cicsplexName");
			if (cicsplexName != null) {
				serverConfig.setCicsplexName(cicsplexName.getValue());
			} else {
				throw new MojoExecutionException("No CICSplex name set");
			}
			
			Xpp3Dom regionName = c.getChild("regionName");
			if (regionName != null) {
				serverConfig.setRegionName(regionName.getValue());
			} else {
				throw new MojoExecutionException("No region name set");
			}
		} else {
			throw new MojoExecutionException("Unknown server configuration format: " + configuration.getClass());
		}
		return serverConfig;
    }

}
