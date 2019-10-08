# Bundle reactor deploy sample
This sample demonstrates how you can create a new Maven reactor project with multiple modules. 
One module (demo-war) contains a Java project which uses JCICS from Maven Central. 
The other module (demo-bundle) contains the configuration to package the Java project into a CICS bundle and then deploy this to CICS. 
This is the quickest way to give this a try without having an existing Java Maven project. 

## Set Up
### Have your system programmer create your bundle definition in CSD
Your system programmer should create a bundle definition in CSD and tell you the CSD group and bundle definition name they have used.
The bundle directory of your bundle definition should be set as follows: `<bundle_deploy_root>/<bundle_id>_<bundle_version>`.  So for this sample, if your bundle_deploy_root was `/u/someuser/bundles/`, the bundle directory would be `/u/someuser/bundles/demo-bundle_0.0.1`.

## Using the sample
[Clone the sample](https://github.com/IBM/cics-bundle-maven/tree/master/samples/bundle-reactor-deploy) into your preferred IDE

Edit the variables in demo-bundle/pom.xml to match the CSD group, CICSplex, region and bundle definition name. 

The project is built as a reactor project. By running the parent project's build, all the children will also be built.

To build all projects and install them into your local Maven repository, run:
mvn clean install

Visit the servlet (`http://yourcicsurl.com:9080/demo-war-0.0.1-SNAPSHOT`) to see what you published
