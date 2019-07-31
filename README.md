# cics-bundle-maven

A collection of Maven plugins and utilities that can be used to build CICS bundles, ready to be installed into CICS TS.

This project contains:
 - `cics-bundle-maven-plugin`, a Maven plugin that builds CICS bundles, including Java-based dependencies.
 - `cics-bundle-reactor-archetype`, a Maven archetype that shows how a simple reactor build can pull together a CICS bundle and a Java project.

## Using `cics-bundle-maven-plugin`

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

## Using `cics-bundle-reactor-archetype`

Maven archetypes provide templates for how a module could, or should, be structured. By using `cics-bundle-reactor-archetype`, you are provided with a reactor (multi-module build) module, containing a CICS bundle module and a dynamic Web (WAR) module. The CICS bundle is preconfigured to depend on the WAR module. Building the reactor module builds both the children, so you end up with a CICS bundle that contains the built WAR file.

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

## Building the project

The project is built as a reactor project. By running the parent project's build, all the children will also be built.

To build all projects and install them into the local Maven repository, run:

```
mvn install
```

## Contributing

We welcome contributions! Find out how in our [contribution guide](CONTRIBUTING.md).

## Licence

This project is licensed under the Eclipse Public License, Version 2.0.
