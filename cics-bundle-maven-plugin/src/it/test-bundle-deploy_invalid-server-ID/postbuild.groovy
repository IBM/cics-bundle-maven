import com.ibm.cics.cbmp.DeployPreBuild

File buildLog = new File(basedir, 'build.log')

assert buildLog.exists()
assert buildLog.text.contains("[ERROR] Failed to execute goal com.ibm.cics:cics-bundle-maven-plugin:0.0.1-SNAPSHOT:deploy (deploy) on project test-bundle-deploy: Server 'INVALID' does not exist")
assert buildLog.text.contains("Caused by: org.apache.maven.plugin.MojoExecutionException: Server 'INVALID' does not exist")


DeployPreBuild.teardownWiremock();
