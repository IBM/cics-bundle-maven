# cics-bundle-maven [![Maven Central Latest](https://maven-badges.herokuapp.com/maven-central/com.ibm.cics/cics-bundle-maven-plugin/badge.svg)](https://search.maven.org/search?q=g:com.ibm.cics%20AND%20a:cics-bundle-maven-plugin) [![Build Status](https://travis-ci.com/IBM/cics-bundle-maven.svg?branch=master)](https://travis-ci.com/IBM/cics-bundle-maven) [![Nexus Snapshots](https://img.shields.io/nexus/s/com.ibm.cics/cics-bundle-maven.svg?server=https%3A%2F%2Foss.sonatype.org&label=snapshot&color=success)](https://oss.sonatype.org/#nexus-search;gav~com.ibm.cics~cics-bundle-maven-plugin~~~)

A collection of Maven plugins and utilities that can be used to build CICS bundles, ready to be installed into CICS TS.

This project contains:
 - `cics-bundle-maven-plugin`, a Maven plugin that authors CICS bundles for deploying resources into CICS TS. It supports a subset of bundleparts, including Java assets. Read the [Maven doc](https://ibm.github.io/cics-bundle-maven/plugin-info.html) for information about this plugin's goals.
 - `cics-bundle-reactor-archetype`, a Maven archetype that provides a simple reactor build that contains a CICS bundle and a Dynamic Web Project (WAR). This archetype builds and packages the WAR and CICS bundle.
 - `cics-bundle-deploy-reactor-archetype`, a Maven archetype that provides a simple reactor build that contains a CICS bundle and a Dynamic Web Project (WAR). This archetype packages the WAR into a CICS Bundle and installs it into CICS.
 - `samples`, a collection of samples that demonstrate the different ways in which to use the plugin.

## Supported bundlepart types
The `cics-bundle-maven-plugin` currently supports the following CICS bundleparts:
 - EAR
 - OSGi bundle
 - WAR
 - EPADAPTER
 - EPADAPTERSET
 - EVENTBINDING
 - FILE
 - LIBRARY
 - PACKAGESET
 - POLICY
 - PROGRAM
 - TCPIPSERVICE
 - TRANSACTION
 - URIMAP

## Prerequisites
 To use the plugins, make sure that:
 * Maven is installed into your environment, or
 * You use a Java IDE that supports Maven, e.g. Eclipse, IntelliJ, VS Code...


## Create a CICS bundle using `cics-bundle-maven-plugin`

The `cics-bundle-maven-plugin` contributes a new packaging type called `cics-bundle`. This is bound to a new `build` goal that will use the information in the `pom.xml` and dependencies to create a CICS bundle, ready to be stored in an artifact repository or installed into CICS.

To use the `cics-bundle-maven-plugin`:

1. Create a new Maven module for the CICS bundle.
1. Register the plugin to the `pom.xml` of the CICS bundle module:

    ```xml
    <build>
      <plugins>
        <plugin>
          <groupId>com.ibm.cics</groupId>
          <artifactId>cics-bundle-maven-plugin</artifactId>
          <version>0.0.1-SNAPSHOT</version>
          <extensions>true</extensions>
        </plugin>
      </plugins>
    </build>
    ```
1. Change the packaging type of the CICS bundle module to the new CICS bundle type:

    ```xml
    <packaging>cics-bundle</packaging>
    ```

    If at this point you build the CICS bundle module, it will create a valid CICS bundle! However, it doesn't do much, because it doesn't define any bundle parts.

1. In the CICS bundle module, define a dependency on another module for a Java project that [can be included](#supported-bundlepart-types) into CICS, such as an OSGi bundle, WAR, or EAR.

    ```xml
    <dependencies>
      <dependency>
        <groupId>my.group.id</groupId>
        <artifactId>my-web-project</artifactId>
        <version>1.0.0</version>
        <type>war</type>
      </dependency>
    </dependencies>
    ```
1. The plugin requires very little configuration. However, an important property for Java bundleparts is the name of the JVM server that the application will be installed into. To define this, alter how you registered the plugin to add some configuration, with the `<defaultjvmserver>` property:

    ```xml
    <build>
      <plugins>
        <plugin>
          <groupId>com.ibm.cics</groupId>
          <artifactId>cics-bundle-maven-plugin</artifactId>
          <version>0.0.1-SNAPSHOT</version>
          <extensions>true</extensions>
          <configuration>
            <defaultjvmserver>JVMSRV1</defaultjvmserver>
          </configuration>
        </plugin>
      </plugins>
    </build>
    ```
    Now if you build the CICS bundle it will pull in the dependency, add it into the CICS bundle, and define it in the CICS bundle's manifest. The CICS bundle is ready to be stored in an artifact repository or deployed to CICS.

    The generated CICS bundle takes its bundle ID from the Maven module's `artifactId` and its version from the Maven module's `version`.

1. To include CICS bundleparts like FILE or URIMAP, put the bundlepart files in your Maven project's resources directory, for instance `src/main/resources`. Files in your Maven project's resources directory will be included within the output CICS bundle, and supported types will have a define added to the CICS bundle's cics.xml.


## Using nightly/snapshot builds

Snapshot builds are published to the Sonatype OSS Maven snapshots repository.  To try a snapshot build, you will need to add the following plugin repository to your `pom.xml`:

```xml
<project>
  ...
  <pluginRepositories>
    <!-- Configure Sonatype OSS Maven snapshots repository -->
    <pluginRepository>
      <id>sonatype-nexus-snapshots</id>
      <name>Sonatype Nexus Snapshots</name>
      <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
      <releases>
        <enabled>false</enabled>
      </releases>
    </pluginRepository>
  </pluginRepositories>
  ...
</project>
```


## Samples

Use of this plugin will vary depending on what you're starting with and the structure of your project. We have included some samples to demonstrate the different methods. 

[Reactor sample](https://github.com/IBM/cics-bundle-maven/tree/master/samples/bundle-reactor-deploy)  
This sample is the best starting place if you don't already have a Java project you want you build and want to have a go at building and deploying straight away. This is a reactor project with one module including the source for a web page (including a JCICS call), which will be packaged into a WAR. It has a second module, which creates the bundle and installs this in CICS. 
Further information can be found [here](samples/bundle-reactor-deploy/README.md)

[WAR sample](https://github.com/IBM/cics-bundle-maven/tree/master/samples/bundle-war-deploy)  
This sample shows how you can add to the pom of an existing Java Maven project, to build it into a bundle and install it in CICS. 
Further information can be found [here](samples/bundle-war-deploy/README.md)


## Archetypes

Another way to get started with the plugin is to use one of the provided archetypes. Maven archetypes provide templates for how a module could, or should, be structured. By using `cics-bundle-reactor-archetype`, you are provided with a reactor (multi-module build) module, containing a CICS bundle module and a dynamic Web (WAR) module. The CICS bundle is preconfigured to depend on the WAR module. Building the reactor module builds both the children, so you end up with a CICS bundle that contains the built WAR file. If you use the `cics-bundle-deploy-reactor-archetype` archetype, this is extended to install the CICS bundle in CICS. 


1. Create a Maven module by referring to the either the [`com.ibm.cics:cics-bundle-reactor-archetype`](https://search.maven.org/artifact/com.ibm.cics/cics-bundle-reactor-archetype/0.0.1/maven-archetype) or `com.ibm.cics:cics-bundle-deploy-reactor-archetype` artifact:

    * ![On command line](images/cmd.png) On command line:
    
         ```
         mvn archetype:generate -DarchetypeGroupId=com.ibm.cics -DarchetypeArtifactId=cics-bundle-deploy-reactor-archetype -DarchetypeVersion=0.0.2-SNAPSHOT -DgroupId=<my-groupid> -DartifactId=<my-artifactId>
         ```
         In the pom.xml file of the bundle module there will be default values for each of the following parameters which you will need to set:  
         `defaultjvmserver`, `bunddef`, `csdgroup`, `username`, `password`, `deployURL`, `cicsplex`, `region`  
         If using the `com.ibm.cics:cics-bundle-reactor-archetype` archetype, only `defaultjvmserver` will need defining as the other parameters are used during the deploy. 
         
    * ![In Eclipse](images/eclipse.png) If you're using Eclipse:
        1. Click **File > New > Maven Project**.
        1. Click **Next** on the first page
        1. On the second page, click **Add archetype...** in the New Maven Project dialog and specify the following information:
    
            Archetype Group Id: `com.ibm.cics`  
            Archetype Artifact Id: `cics-bundle-deploy-reactor-archetype`              
            Archetype Version: `0.0.2-SNAPSHOT`  

            Then hit **OK**.  
        1. From the archetype list, select `cics-bundle-deploy-reactor-archetype` and hit **Next**. 
        1. Specify information about your own project, including the group ID and artifact ID.
        1. Set the values for the listed parameters. Then hit **Finish**.  
    
2. Build the bundle and install it into your local repository.

    * ![On command line](images/cmd.png) On command line:
    
        Switch to the parent module and run:
        
        ```
        mvn install
        ```
    
    * ![In Eclipse](images/eclipse.png) In Eclipse:
    
        In the `pom.xml` editor of the parent module, right-click and select **Run As > Maven install**.  

**Result:** Both the CICS bundle and the WAR file on which it depends are built. The generated CICS bundle takes its bundle ID from the Maven module's artifactId and its version from the Maven module's version. Its manifest file is also generated during the build. This has been installed into CICS using the provided bundle definition. 
If using `cics-bundle-reactor-archetype`, this is not yet installed in CICS. You can store the built bundle in an artifact repository or deploy it to CICS.


## Building this project

This project is built as a reactor project. By running the parent project's build, all the children will also be built.

To build all projects and install them into the local Maven repository, run:

```
mvn install
```

## Contributing

We welcome contributions! Find out how in our [contribution guide](CONTRIBUTING.md).

## Licence

This project is licensed under the Eclipse Public License, Version 2.0.
