# Bundle reactor deploy sample
This sample demonstrates how you can create a new Maven reactor project with multiple modules. 
One module (demo-war) contains a Java project which uses JCICS from Maven Central. 
The other module (demo-bundle) contains the configuration to package the Java project into a CICS bundle and then deploy this to CICS. 
This is the quickest way to give this a try without having an existing Java Maven project. 

## Set Up
### Have your system programmer create your bundle definition in CSD
Your system programmer has the freedom to choose whichever CSD group and BUNDDEF name they like.
The bundle directory of your bundle definition should be set as follows: <bundle_deploy_root>/<bundle_id>_<bundle_version>.  So for this sample, ensure the bundle deploy root is set to `/u/expauto/bundles/demo-bundle_0.0.1`.

## Using the sample
Clone the sample into your preferred IDE

Edit the variables in demo-bundle/pom.xml to match the CSD group, CICS Plex, region and bundle definition name. 

The project is built as a reactor project. By running the parent project's build, all the children will also be built.

To build all projects and install them into your local Maven repository, run:
mvn clean install

Visit the servlet (http://yourcicsurl.com:9080/demo-war-0.0.1-SNAPSHOT) to see what you published
