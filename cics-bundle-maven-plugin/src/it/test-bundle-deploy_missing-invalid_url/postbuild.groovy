import com.ibm.cics.cbmp.DeployPreBuild

File buildLog = new File(basedir, 'build.log')

assert buildLog.exists()
assert buildLog.text.contains("[ERROR] Failed to execute goal com.ibm.cics:cics-bundle-maven-plugin:0.0.1-SNAPSHOT:deploy (deploy) on project test-bundle-deploy: invalid: nodename nor servname provided, or not known: Unknown host invalid: nodename nor servname provided, or not known")
assert buildLog.text.contains("Caused by: com.ibm.cics.cbmp.BundleDeployException: invalid: nodename nor servname provided, or not known");

DeployPreBuild.teardownWiremock();
