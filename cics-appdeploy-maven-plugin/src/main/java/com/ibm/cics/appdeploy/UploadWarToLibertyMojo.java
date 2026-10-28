package com.ibm.cics.appdeploy;

/*-
 * #%L
 * CICS AppDeploy Maven Plugin
 * %%
 * Copyright (C) 2026 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Maven Mojo that uploads a WAR file to Liberty server using multipart/form-data.
 * Sends the WAR binary and applicationXml as separate named parts, streamed without loading into memory.
 */
@Mojo(name = "upload-war", defaultPhase = LifecyclePhase.DEPLOY)
@Execute(phase = LifecyclePhase.PACKAGE)
public class UploadWarToLibertyMojo extends AbstractMojo {

    // Retry configuration
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000L;
    
    // Buffer size for streaming file upload (8KB chunks)
    private static final int BUFFER_SIZE = 8192;
    
    // HTTP success status codes
    private static final int[] SUCCESS_STATUS_CODES = {200, 201};
    
    // Path of the AppDeploy upload endpoint. It is appended to the configured serverUrl.
    private static final String UPLOAD_PATH = "/appdeploy/uploadApp";

    // Validation error messages
    private static final String MISSING_SERVER_URL = "Specify serverUrl for Liberty WAR upload";
    private static final String MISSING_APPLICATION_XML = "Specify either applicationXml or applicationXmlLocation for Liberty WAR upload";

    private static final String UPLOAD_CONFIG_EXCEPTION =
        "Please specify Liberty WAR upload configuration in pom.xml.\n\n" +
        "Example with inline applicationXml and Basic Authentication:\n" +
        "<configuration>\n" +
        "  <cicsAppDeploy>\n" +
        "    <serverUrl>https://localhost:9443</serverUrl>\n" +
        "    <applicationXml><![CDATA[<application id=\"myapp\" location=\"myapp.war\" type=\"war\">...</application>]]></applicationXml>\n" +
        "    <userName>username</userName>\n" +
        "    <password>password</password>\n" +
        "  </cicsAppDeploy>\n" +
        "</configuration>\n\n" +
        "Example with applicationXmlLocation and JWT Token:\n" +
        "<configuration>\n" +
        "  <cicsAppDeploy>\n" +
        "    <serverUrl>https://localhost:9443</serverUrl>\n" +
        "    <applicationXmlLocation>${project.basedir}/src/main/liberty/application.xml</applicationXmlLocation>\n" +
        "    <bearerToken>your-jwt-token</bearerToken>\n" +
        "  </cicsAppDeploy>\n" +
        "</configuration>\n\n" +
        "Example with a client certificate (requires auth=\"client-cert\" on the server):\n" +
        "<configuration>\n" +
        "  <cicsAppDeploy>\n" +
        "    <serverUrl>https://localhost:9443</serverUrl>\n" +
        "    <applicationXmlLocation>${project.basedir}/src/main/liberty/application.xml</applicationXmlLocation>\n" +
        "    <clientKeyStore>${project.basedir}/client.p12</clientKeyStore>\n" +
        "    <clientKeyStorePassword>keystore-password</clientKeyStorePassword>\n" +
        "  </cicsAppDeploy>\n" +
        "</configuration>\n\n" +
        "Example with No Security (requires auth=\"none\" on the server):\n" +
        "<configuration>\n" +
        "  <cicsAppDeploy>\n" +
        "    <serverUrl>https://localhost:9443</serverUrl>\n" +
        "    <applicationXmlLocation>${project.basedir}/src/main/liberty/application.xml</applicationXmlLocation>\n" +
        "    <!-- No userName, password, bearerToken or clientKeyStore - sends request without authentication -->\n" +
        "  </cicsAppDeploy>\n" +
        "</configuration>";

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    private File buildDir;

    /**
     * Configuration for Liberty WAR upload
     */
    @Parameter
    private LibertyWarUploadConfig cicsAppDeploy;

    /**
     * The WAR file to upload. Defaults to the project's built artifact file, which is read after
     * the package phase has run. The extension check catches non-WAR packaging types (e.g. jar, ear).
     */
    @Parameter
    private File warFile;

    /**
     * Socket factory that presents the client certificate. Null unless clientKeyStore is configured.
     */
    private SSLSocketFactory clientSocketFactory;

    /**
     * Thrown for a 3xx or 4xx response. It is not retried.
     */
    private static class RejectedUploadException extends MojoExecutionException {
        RejectedUploadException(String message) {
            super(message);
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("=== Upload WAR to Liberty ===");

        validateConfiguration();
        clientSocketFactory = buildClientSocketFactory();

        File war = validateWarFile();
        logUploadInfo(war);

        try {
            uploadWarWithRetry(war);
            getLog().info("✓ WAR file uploaded successfully!");
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to upload WAR file: " + e.getMessage(), e);
        }
    }

    /**
     * Finds the WAR that the package phase produced. When this goal is run on its own, the package phase
     * runs in a forked copy of the project, so the artifact file is not set on this project. In that case
     * the WAR is at target/&lt;finalName&gt;.war, or is the only .war in the build directory when the WAR
     * plugin has been given another name. If there is no WAR or more than one, returns null.
     */
    private File packagedWar() {
        File artifactFile = project.getArtifact().getFile();
        if (artifactFile != null) {
            return artifactFile;
        }
        File expected = new File(buildDir, project.getBuild().getFinalName() + ".war");
        if (expected.isFile()) {
            return expected;
        }
        File[] wars = buildDir.listFiles((dir, name) -> name.endsWith(".war"));
        return wars != null && wars.length == 1 ? wars[0] : null;
    }

    /**
     * Validates that the WAR file has a .war extension.
     */
    private File validateWarFile() throws MojoExecutionException {
        File war = warFile != null ? warFile : packagedWar();
        if (war == null || !war.isFile()) {
            throw new MojoExecutionException(
                "No WAR file found. Package the project as a WAR (packaging = war), or set warFile to the WAR to upload");
        }
        String name = war.getName();
        if (!name.toLowerCase().endsWith(".war")) {
            String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "(no extension)";
            throw new MojoExecutionException("Unsupported file type: " + ext + ". Only .war files are accepted");
        }
        return war;
    }

    /**
     * Logs upload information including file name, size, and target server.
     */
    private void logUploadInfo(File war) {
        double fileSizeMB = war.length() / (1024.0 * 1024.0);
        getLog().info("Uploading WAR file: " + war.getName());
        getLog().info(String.format("File size: %.2f MB", fileSizeMB));
        getLog().info("Target URL: " + uploadUrl());
    }

    /**
     * Uploads WAR file with exponential backoff retry logic.
     */
    private void uploadWarWithRetry(File war) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                uploadWarFile(war);
                return; // Success
            } catch (Exception e) {
                lastException = e;
                if (e instanceof RejectedUploadException) {
                    throw e;
                }
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    long delayMs = RETRY_DELAY_MS * attempt;
                    getLog().warn("Upload attempt " + attempt + " failed: " + e.getMessage() + 
                                  ". Retrying in " + delayMs + "ms...");
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw new MojoExecutionException("Upload failed after " + MAX_RETRY_ATTEMPTS + " attempts", lastException);
    }

    /**
     * Uploads WAR file using multipart/form-data with streaming.
     */
    private void uploadWarFile(File war) throws Exception {
        
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String appXml = resolveApplicationXml();
        
        HttpURLConnection connection = createConnection(uploadUrl(), boundary);
        writeMultipartBody(connection, boundary, appXml, war);
        
        int responseCode = connection.getResponseCode();
        getLog().info("Response: " + responseCode + " - " + connection.getResponseMessage());
        
        validateResponse(connection, responseCode);
        logResponseBody(connection);
    }

    /**
     * Writes the multipart/form-data body to the connection output stream.
     * Sends two parts: "applicationXml" (text/xml) and "warFile" (application/octet-stream).
     * Streams the WAR binary in 8KB chunks to avoid loading it into memory.
     */
    private void writeMultipartBody(HttpURLConnection connection, String boundary, String appXml, File war)
        throws IOException {

        try (OutputStream out = connection.getOutputStream()) {

            // --- Part 1: applicationXml ---
            out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write("Content-Disposition: form-data; name=\"applicationXml\"\r\n".getBytes(StandardCharsets.UTF_8));
            out.write("Content-Type: text/xml; charset=UTF-8\r\n".getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            out.write(appXml.getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // --- Part 2: warFile ---
            out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"warFile\"; filename=\"" + war.getName() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            out.write("Content-Type: application/octet-stream\r\n".getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));

            try (FileInputStream fileInput = new FileInputStream(war)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytes = 0;
                long lastLoggedMB = 0;

                while ((bytesRead = fileInput.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;

                    // Log progress every 100MB for large files
                    long currentMB = totalBytes / (1024 * 1024);
                    if (currentMB - lastLoggedMB >= 100) {
                        getLog().info("Uploaded: " + currentMB + "MB");
                        lastLoggedMB = currentMB;
                    }
                }

                getLog().info(String.format("Total uploaded: %.2f MB", totalBytes / (1024.0 * 1024.0)));
            }

            // --- Final boundary ---
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Validates the HTTP response code.
     */
    private void validateResponse(HttpURLConnection connection, int responseCode) throws Exception {
        if (!isSuccess(responseCode)) {
            String message = "Upload failed with code " + responseCode + ": " + getErrorMessage(connection);
            // 3xx (redirects are never followed) and 4xx (refused) cannot succeed on retry, and
            // retrying bad credentials could lock the user ID.
            if (responseCode >= 300 && responseCode < 500) {
                throw new RejectedUploadException(message);
            }
            throw new MojoExecutionException(message);
        }
    }

    /**
     * Returns the upload endpoint URL: the configured serverUrl plus /appdeploy/uploadApp.
     * A serverUrl that already ends with the endpoint path is used as it is.
     */
    private String uploadUrl() {
        String base = cicsAppDeploy.getServerUrl().trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base.endsWith(UPLOAD_PATH) ? base : base + UPLOAD_PATH;
    }

    /**
     * Creates HTTP connection configured for multipart/form-data upload with authentication.
     */
    private HttpURLConnection createConnection(String url, String boundary) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        if (clientSocketFactory != null && connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(clientSocketFactory);
        }
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);  // Never follow redirects: a redirected POST would resend credentials and the WAR
        
        // Set timeout configurations
        int connectTimeout = cicsAppDeploy.getConnectTimeout();
        int readTimeout = cicsAppDeploy.getReadTimeout();
        
        connection.setConnectTimeout(connectTimeout);  // Time to establish connection
        connection.setReadTimeout(readTimeout);        // Time to wait for response
        
        getLog().info("Timeout configuration - Connect: " + connectTimeout + "ms, Read: " + readTimeout + "ms");
        
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        
        addAuthentication(connection);
        
        return connection;
    }

    /**
     * Adds authentication header to connection (Basic Auth or Bearer Token).
     * A client certificate is sent in the TLS handshake, so it needs no header.
     * If no credentials are provided, no Authorization header is added.
     */
    private void addAuthentication(HttpURLConnection connection) {
        String bearerToken = cicsAppDeploy.getBearerToken();
        String userName = cicsAppDeploy.getUserName();
        String password = cicsAppDeploy.getPassword();
        
        if (bearerToken != null && !bearerToken.isEmpty()) {
            // Use JWT Bearer token
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            getLog().info("Using JWT Bearer token authentication");
        } else if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
            // Use Basic Authentication
            String credentials = userName + ":" + password;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
            getLog().info("Using Basic Authentication");
        } else if (clientSocketFactory != null) {
            getLog().info("Using client certificate authentication");
        } else {
            // No credentials provided - send request without authentication
            getLog().info("No authentication - sending request without Authorization header");
        }
    }

    /**
     * Logs the HTTP response body if available.
     */
    private void logResponseBody(HttpURLConnection connection) {
        try {
            if (connection.getInputStream() != null) {
                java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A");
                String responseBody = scanner.hasNext() ? scanner.next() : "";
                if (!responseBody.isEmpty()) {
                    getLog().info("Server response: " + responseBody);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Extracts error message from HTTP connection.
     */
    private String getErrorMessage(HttpURLConnection connection) {
        try {
            if (connection.getErrorStream() != null) {
                java.util.Scanner scanner = new java.util.Scanner(connection.getErrorStream()).useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : connection.getResponseMessage();
            }
        } catch (Exception e) {
            // Ignore
        }
        try {
            return connection.getResponseMessage();
        } catch (Exception e) {
            return "Unknown error";
        }
    }

    /**
     * Checks if response code is a success.
     */
    private boolean isSuccess(int code) {
        for (int successCode : SUCCESS_STATUS_CODES) {
            if (code == successCode) return true;
        }
        return false;
    }

    /**
     * Validates that all required configuration properties are set.
     */
    private void validateConfiguration() throws MojoExecutionException {
        if (cicsAppDeploy == null) {
            throw new MojoExecutionException(UPLOAD_CONFIG_EXCEPTION);
        }

        StringBuilder errors = new StringBuilder();

        if (cicsAppDeploy.getServerUrl() == null || cicsAppDeploy.getServerUrl().isEmpty()) {
            errors.append(MISSING_SERVER_URL).append("\n");
        }
        boolean hasInlineApplicationXml = cicsAppDeploy.getApplicationXml() != null &&
                                          !cicsAppDeploy.getApplicationXml().trim().isEmpty();
        boolean hasApplicationXmlLocation = cicsAppDeploy.getApplicationXmlLocation() != null &&
                                            !cicsAppDeploy.getApplicationXmlLocation().trim().isEmpty();

        if (!hasInlineApplicationXml && !hasApplicationXmlLocation) {
            errors.append(MISSING_APPLICATION_XML).append("\n");
        }

        if (hasInlineApplicationXml && hasApplicationXmlLocation) {
            getLog().warn("Both applicationXml and applicationXmlLocation provided. Inline applicationXml will be used.");
        }
        
        // Authentication is optional - validate only if credentials are provided
        boolean hasBasicAuth = cicsAppDeploy.getUserName() != null && !cicsAppDeploy.getUserName().isEmpty() &&
                               cicsAppDeploy.getPassword() != null && !cicsAppDeploy.getPassword().isEmpty();
        boolean hasBearerToken = cicsAppDeploy.getBearerToken() != null && !cicsAppDeploy.getBearerToken().isEmpty();
        boolean hasClientCert = cicsAppDeploy.getClientKeyStore() != null && !cicsAppDeploy.getClientKeyStore().isEmpty();
        boolean hasPartialBasicAuth = (cicsAppDeploy.getUserName() != null && !cicsAppDeploy.getUserName().isEmpty() &&
                                       (cicsAppDeploy.getPassword() == null || cicsAppDeploy.getPassword().isEmpty())) ||
                                      ((cicsAppDeploy.getUserName() == null || cicsAppDeploy.getUserName().isEmpty()) &&
                                       cicsAppDeploy.getPassword() != null && !cicsAppDeploy.getPassword().isEmpty());
        
        // Only error on partial Basic Auth if there's no bearerToken or clientKeyStore
        if (hasPartialBasicAuth && !hasBearerToken && !hasClientCert) {
            errors.append("Incomplete Basic Authentication. Provide both userName and password, or use bearerToken or clientKeyStore, or omit all credentials if the server uses auth=\"none\".").append("\n");
        }

        if (hasClientCert) {
            File keyStoreFile = clientKeyStoreFile();
            if (!keyStoreFile.isFile()) {
                errors.append("clientKeyStore does not exist: ").append(keyStoreFile.getAbsolutePath()).append("\n");
            }
        } else if (cicsAppDeploy.getClientKeyStorePassword() != null && !cicsAppDeploy.getClientKeyStorePassword().isEmpty()) {
            errors.append("clientKeyStorePassword is set without clientKeyStore").append("\n");
        }

        if (!hasBasicAuth && !hasPartialBasicAuth && !hasBearerToken && !hasClientCert) {
            getLog().warn("No authentication credentials provided. Request will be sent without authentication. The server must be configured with auth=\"none\".");
        }

        if (errors.length() > 0) {
            throw new MojoExecutionException(errors.toString() + "\n" + UPLOAD_CONFIG_EXCEPTION);
        }
    }

    /**
     * Returns the client keystore file. A relative path is resolved against the project directory.
     */
    private File clientKeyStoreFile() {
        File file = new File(cicsAppDeploy.getClientKeyStore());
        return file.isAbsolute() ? file : new File(project.getBasedir(), cicsAppDeploy.getClientKeyStore());
    }

    /**
     * Loads the client keystore and returns a socket factory that presents its certificate, or null if no
     * clientKeyStore is configured. The server certificate is checked against the JVM default truststore.
     */
    private SSLSocketFactory buildClientSocketFactory() throws MojoExecutionException {
        if (cicsAppDeploy.getClientKeyStore() == null || cicsAppDeploy.getClientKeyStore().isEmpty()) {
            return null;
        }
        File file = clientKeyStoreFile();
        String keyStorePassword = cicsAppDeploy.getClientKeyStorePassword();
        char[] password = keyStorePassword == null ? new char[0] : keyStorePassword.toCharArray();

        try (InputStream in = new FileInputStream(file)) {
            KeyStore keyStore = KeyStore.getInstance(cicsAppDeploy.getClientKeyStoreType());
            keyStore.load(in, password);

            boolean hasPrivateKey = false;
            for (String alias : Collections.list(keyStore.aliases())) {
                hasPrivateKey |= keyStore.isKeyEntry(alias);
            }
            if (!hasPrivateKey) {
                throw new MojoExecutionException("clientKeyStore has no private key entry: " + file.getAbsolutePath());
            }

            KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagers.init(keyStore, password);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagers.getKeyManagers(), null, null);
            return context.getSocketFactory();
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot load clientKeyStore " + file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    private String resolveApplicationXml() throws MojoExecutionException {
        String inlineApplicationXml = cicsAppDeploy.getApplicationXml();
        if (inlineApplicationXml != null && !inlineApplicationXml.trim().isEmpty()) {
            return inlineApplicationXml.trim();
        }

        String applicationXmlLocation = cicsAppDeploy.getApplicationXmlLocation();
        File applicationXmlFile = new File(applicationXmlLocation);
        if (!applicationXmlFile.isAbsolute()) {
            applicationXmlFile = new File(project.getBasedir(), applicationXmlLocation);
        }

        if (!applicationXmlFile.exists()) {
            throw new MojoExecutionException("applicationXmlLocation does not exist: " + applicationXmlFile.getAbsolutePath());
        }

        if (!applicationXmlFile.isFile()) {
            throw new MojoExecutionException("applicationXmlLocation is not a file: " + applicationXmlFile.getAbsolutePath());
        }

        try {
            return new String(Files.readAllBytes(applicationXmlFile.toPath()), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read applicationXmlLocation: " + applicationXmlFile.getAbsolutePath(), e);
        }
    }
}
