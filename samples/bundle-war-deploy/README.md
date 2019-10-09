# Bundle WAR deploy sample
This sample demonstrates how you can add to an existing pom.xml of a WAR project that already exists.
Everything within the demo-war directory would be an existing Maven project. The additional step to publish this to CICS is the additional plugin in the pom.xml: 

## Set Up
### Have your system programmer create your bundle definition in CSD
Your system programmer should create a bundle definition in CSD and tell you the CSD group and bundle definition name they have used.
The bundle directory of your bundle definition should be set as follows: `<bundle_deploy_root>/<bundle_id>_<bundle_version>`.  So for this sample, if your bundle_deploy_root was `/u/someuser/bundles/`, the bundle directory would be `/u/someuser/bundles/demo-war_0.0.1`.

## Using the sample
There are 2 options of how to use this sample. 
Option 1 is to use the whole sample as-is, for example, if you want to try this out before using it with an existing Maven project. 
Option 2 is to extend an existing maven project, which you'd like to package and install as a CICS bundle. 

### Option 1: Using the full sample
[Clone the sample](https://github.com/IBM/cics-bundle-maven/tree/master/samples/bundle-war-deploy) into your preferred IDE

Edit the variables from the configuration section below in demo-war/pom.xml to match the CSD group, CICSplex, region and bundle definition name. 

      ```xml
      <plugin>
        <groupId>com.ibm.cics</groupId>
        <artifactId>cics-bundle-maven-plugin</artifactId>
        <version>0.0.2-SNAPSHOT</version>
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
      
### Option 2: Add to an existing Maven project
If you already have an existing Java Maven project, you can add the below to the plugins section of your pom.xml and edit the configuration variables. Your Maven project should now resemble the sample.

      ```xml
      <plugin>
        <groupId>com.ibm.cics</groupId>
        <artifactId>cics-bundle-maven-plugin</artifactId>
        <version>0.0.2-SNAPSHOT</version>
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

To build the project and install into your local Maven repository, run:
mvn clean install

Visit the servlet (http://yourcicsurl.com:9080/demo-war-0.0.1-SNAPSHOT) to see what you published