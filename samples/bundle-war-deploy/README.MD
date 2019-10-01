This sample demonstrates how you can add to an existing pom.xml of a war project that already exists.
Everything within the demo-war directory would be an existing Maven project. The additional step to publish this to CICS is the additional plugin in the pom.xml: 


## Set Up
# Have your system programmer create your bundle definition in CSD
Your system programmer has the freedom to choose whichever CSD group and BUNDDEF name they like.
The bundle directory of your bundle definition should be set as follows: <bundle_deploy_root>/<bundle_id>_<bundle_version>.  So for this sample, ensure the bundle deploy root is set to `/u/expauto/bundles/demo-war_0.0.1`.

## Using the sample
Clone the sample into your preferred IDE

Edit the variables from the below block in demo-war/pom.xml to match the CSD group, CICS Plex, region and bundle definition name. 

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
              <username>${deploy-user}</username>
              <password>${deploy-password}</password>
              <bunddef>DEMOBUNDLE</bunddef>
              <csdgroup>BAR</csdgroup>
              <cicsplex>CICSEX56</cicsplex>
              <region>IYCWEMW2</region>
            </configuration>
          </execution>
        </executions>
      </plugin>
      
##Build

To build the project and install into your local Maven repository, run:
mvn clean install

Visit the servlet (http://yourcicsurl.com:9080/demo-war-0.0.1-SNAPSHOT) to see what you published