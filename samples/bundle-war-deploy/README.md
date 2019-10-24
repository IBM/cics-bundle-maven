# Bundle WAR deploy sample
This sample demonstrates how you can configure an existing WAR project to build a CICS bundle.
The demo-war directory would be your existing Maven project. The additional step to publish this to CICS is to configure the cics-bundle-maven plugin in the demo-war/pom.xml.

## Set Up
### Have your system programmer create your BUNDLE definition in CSD
Your system programmer should create a BUNDLE definition in CSD and tell you the CSD group and BUNDLE definition name they have used.
The BUNDLEDIR of the BUNDLE definition your system programmer creates should be set as follows: `<bundles-directory>/<bundle_id>_<bundle_version>`.  So for this sample, if your system programmer configured `bundles-directory` as `/u/someuser/bundles/`, the BUNDLEDIR would be `/u/someuser/bundles/demo-war_0.0.1`.

## Using the sample
There are 2 ways to use this sample. 
Option 1 is to use the whole sample as-is, for example, if you want to try this out before using it with an existing Maven project.
Option 2 is to extend an existing Maven project of packaging type `war`, which you'd like to package and install as a CICS bundle. 

### Option 1: Using the full sample
[Clone the repository](https://github.com/IBM/cics-bundle-maven.git) and import the sample, samples/bundle-war-deploy into your preferred IDE.

Edit the variables from the configuration section in demo-war/pom.xml to match the correct CMCI URL, CSD group, CICSplex, region and BUNDLE definition name for your environment. 

### Option 2: Add to an existing Maven project
If you have an existing Java Maven project, add the snippet shown below to the plugins section of your pom.xml and edit the configuration variables. Your Maven project should now resemble the sample.

```xml
<plugin>
  <groupId>com.ibm.cics</groupId>
  <artifactId>cics-bundle-maven-plugin</artifactId>
  <version>1.0.0</version>
  <executions>
    <execution>
      <goals>
        <goal>bundle-war</goal>
        <goal>deploy</goal>
      </goals>
      <configuration>
        <classifier>cics-bundle</classifier>
        <jvmserver>JVMSRV1</jvmserver>
        <url>http://yourcicsurl.com:9080</url>
        <username>${cics-user-id}</username>
        <password>${cics-password}</password>
        <bunddef>DEMOBUNDLE</bunddef>
        <csdgroup>BAR</csdgroup>
        <cicsplex>CICSEX56</cicsplex>
        <region>IYCWEMW2</region>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Build

To build all projects, install them into your local Maven repository, and deploy the built bundle to your CICS region, change to the bundle-war-deploy directory and run:

```
mvn clean install
```
Visit the servlet (http://yourcicsurl.com:9080/demo-war-0.0.1-SNAPSHOT if you used our sample as-is) to see what you published