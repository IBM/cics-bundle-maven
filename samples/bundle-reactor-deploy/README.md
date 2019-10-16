# Bundle reactor deploy sample
This sample demonstrates how you can create a new Maven reactor project with multiple modules. Reactor is the name for a Maven module that contains a list of other modules that will be built together in one step.

- The outer module (bundle-reactor-deploy) is the reactor module which links the two child modules together.
- One child module (demo-war) contains a Java project which uses JCICS, declared as a dependency from Maven Central.
- The other child module (demo-bundle) contains the configuration to package the demo-war Java project into a CICS bundle and then deploy this to CICS.

This sample is the quickest way to give the Maven plugin a try without having an existing Java Maven project.

## Set Up
### Have your system programmer create your BUNDLE definition in CSD
Your system programmer should create a BUNDLE definition in CSD and tell you the CSD group and BUNDLE definition name they have used.
The BUNDLEDIR of the BUNDLE definition your system programmer creates should be set as follows: `<bundles-directory>/<bundle_id>_<bundle_version>`.  So for this sample, if your system programmer configured `bundles-directory` as `/u/someuser/bundles/`, the BUNDLEDIR would be `/u/someuser/bundles/demo-bundle_0.0.1`.

## Using the sample
[Clone the repository](https://github.com/IBM/cics-bundle-maven.git) and import the sample, samples/bundle-reactor-deploy into your preferred IDE.

Edit the variables in demo-bundle/pom.xml to match the correct CMCI URL, CSD group, CICSplex, region and BUNDLE definition name for your environment.

The project is built as a reactor project. By running the parent project's build (bundle-reactor-deploy/pom.xml), all the children will also be built.

To build all projects, install them into your local Maven repository, and deploy the built bundle to your CICS region, change to the bundle-reactor-deploy directory and run:

```
mvn clean install
```

Visit the servlet (`http://yourcicsurl.com:9080/demo-war-0.0.1-SNAPSHOT`) to see what you published
