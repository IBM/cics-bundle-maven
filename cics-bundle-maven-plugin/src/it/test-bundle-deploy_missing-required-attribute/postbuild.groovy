import com.ibm.cics.cbmp.DeployPreBuild

context.get("wireMockServer").shutdownServer()

File buildLog = new File(basedir, 'build.log')

assert buildLog.exists()
assert buildLog.text.contains("[ERROR] Failed to execute goal com.ibm.cics:cics-bundle-maven-plugin:0.0.1-SNAPSHOT:deploy (deploy) on project test-bundle-deploy: The parameters 'bundleName' for goal com.ibm.cics:cics-bundle-maven-plugin:0.0.1-SNAPSHOT:deploy are missing or invalid")
assert buildLog.text.contains("Caused by: org.apache.maven.plugin.PluginParameterException: The parameters 'bundleName' for goal com.ibm.cics:cics-bundle-maven-plugin:0.0.1-SNAPSHOT:deploy are missing or invalid")