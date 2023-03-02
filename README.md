<!--

Editing notes:

The table of contents is manually created and relies on the wording of the headings. Check for broken links when updating headings.

-->

# cics-bundle-maven [![Maven Central Latest](https://maven-badges.herokuapp.com/maven-central/com.ibm.cics/cics-bundle-maven-plugin/badge.svg)](https://search.maven.org/search?q=g:com.ibm.cics%20AND%20a:cics-bundle-maven-plugin) [![Nexus Snapshots](https://img.shields.io/nexus/s/com.ibm.cics/cics-bundle-maven.svg?server=https%3A%2F%2Foss.sonatype.org&label=snapshot&color=success)](https://oss.sonatype.org/#nexus-search;gav~com.ibm.cics~cics-bundle-maven-plugin~~~) [![Build Status](https://github.com/IBM/cics-bundle-maven/actions/workflows/maven-build.yml/badge.svg?branch=main)](https://github.com/IBM/cics-bundle-maven/actions/workflows/maven-build.yml)

Also see the [Generated Maven plugin documentation ↗](https://ibm.github.io/cics-bundle-maven/plugin-info.html)

---

 - [About this project](#about-this-project)
 - The `cics-bundle-maven-plugin`
   - [Supported bundlepart types](#supported-bundlepart-types)
   - [Prerequisites](#prerequisites)
   - [Create a CICS bundle (in a separate module)](#create-a-cics-bundle-in-a-separate-module-using-cics-bundle-maven-plugin)
   - [Create a CICS bundle (from an existing Java module)](#create-a-cics-bundle-from-an-existing-java-module-using-cics-bundle-maven-plugin)
   - [Deploy a CICS bundle](#deploy-a-cics-bundle-using-cics-bundle-maven-plugin)
   - [Using nightly/snapshot development builds](#using-nightlysnapshot-development-builds)
   - [Samples](#samples)
   - [Archetypes](#archetypes)
   - [Versioning your Maven-built CICS bundles](#versioning-your-maven-built-cics-bundles)
   - [Troubleshooting](#troubleshooting)
 - [Contributing](#contributing)
 - [Support](#support)
 - [License](#license)


## About this project

This is a Maven plugin and related utilities that can be used to build CICS bundles, and deploy them into CICS TS.

This project contains:
 - `cics-bundle-maven-plugin`, a Maven plugin that authors CICS bundles for deploying resources into CICS TS. It supports a subset of bundleparts, including Java assets. Read the [generated plugin documentation](https://ibm.github.io/cics-bundle-maven/plugin-info.html) for information about this plugin's goals.

    Using the plugin you can:
     - [build specialist CICS bundle modules into CICS bundles](#create-a-cics-bundle-in-a-separate-module-using-cics-bundle-maven-plugin), including Java dependencies and other bundleparts (the powerful option)
     - [package existing Java modules into CICS bundles](#create-a-cics-bundle-from-an-existing-java-module-using-cics-bundle-maven-plugin) (the lightweight option)
     - [deploy a CICS bundle to a target CICS region](#deploy-a-cics-bundle-using-cics-bundle-maven-plugin)

 - `cics-bundle-reactor-archetype`, a Maven archetype that provides a simple reactor build that contains a CICS bundle and a Dynamic Web Project (WAR). This archetype builds and packages the WAR and CICS bundle.

 - `cics-bundle-deploy-reactor-archetype`, a Maven archetype that provides a simple reactor build that contains a CICS bundle and a Dynamic Web Project (WAR). This archetype packages the WAR into a CICS Bundle and installs it into CICS.

 - `samples`, a collection of samples that demonstrate the different ways in which to use the plugin.

## Supported bundlepart types
The `cics-bundle-maven-plugin` supports building CICS bundles that contain the following bundleparts:
 - EAR
 - OSGi bundle
 - WAR
 - EBA
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
**Note:** [Enterprise Bundle Archive (EBA) support is stabilized in CICS TS](https://www.ibm.com/docs/en/cics-ts/latest?topic=releases-stabilization-notices).

It can deploy CICS bundles containing any bundleparts.

## Prerequisites
To use the plugin to build CICS bundles, make sure that Maven is installed.

Make sure any required bundles or projects are installed into your local maven repository (.m2 cache) correctly using `mvn install` if they are not available in an online repository.

The plugin builds CICS bundles for any in-service version of CICS Transaction Server for z/OS (version 5.3 and later at the time of writing).

However, if you are using the `deploy` goal of the plugin to deploy bundles to CICS, you must enable the CICS bundle deployment API. The CICS bundle deployment API is supported by the CMCI JVM server that must be set up in a WUI region or a single CICS region. See the [CICS TS doc](https://www.ibm.com/docs/en/cics-ts/6.1_beta?topic=suc-configuring-cmci-jvm-server-cics-bundle-deployment-api) for details. To use the `deploy` goal, make sure that:
 * For a CICSPlex SM environment, set up the CMCI JVM server in the WUI region of the CICSplex that contains the deployment target region. The WUI region must be at CICS TS 5.6 or later.  
 * For a single CICS region environment (SMSS), set up the CMCI JVM server in the deployment target region. The region must be at CICS TS 5.6 with APAR PH35122 applied, or later. 


## Create a CICS bundle (in a separate module) using `cics-bundle-maven-plugin`

This way of building a CICS bundle is useful when the CICS bundle contains more than one Java bundlepart, or contains extra bundleparts like FILE or URIMAP. It uses a separate module for the CICS bundle.

The `cics-bundle-maven-plugin` contributes a new packaging type called `cics-bundle`. This is bound to the plugin's `build` goal that will use the information in the `pom.xml` and dependencies to create a CICS bundle, ready to be stored in an artifact repository or installed into CICS.

To create a CICS bundle in this way:

1. Create a new Maven module for the CICS bundle.
1. Register the plugin to the `pom.xml` of the CICS bundle module:

    ```xml
    <build>
      <plugins>
        <plugin>
          <groupId>com.ibm.cics</groupId>
          <artifactId>cics-bundle-maven-plugin</artifactId>
          <version>1.0.3</version>
          <extensions>true</extensions>
        </plugin>
      </plugins>
    </build>
    ```
    Note that the version should be the latest version of this plugin, which can be found at the top of this page.

1. Change the packaging type of the CICS bundle module to the new CICS bundle type:

    ```xml
    <packaging>cics-bundle</packaging>
    ```

    If at this point you build the CICS bundle module, it will create a valid CICS bundle! However, it doesn't do much, because it doesn't define any bundle parts.

1. In the CICS bundle module, define a dependency on another module for a Java project that [can be included](#supported-bundlepart-types) into CICS, such as an OSGi bundle, WAR, EAR, or EBA.

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
          <version>1.0.3</version>
          <extensions>true</extensions>
          <configuration>
            <defaultjvmserver>DFHWLP</defaultjvmserver>
          </configuration>
        </plugin>
      </plugins>
    </build>
    ```
    Now if you build the CICS bundle (`mvn clean verify` or `mvn clean install`) it will pull in the dependency, add it into the CICS bundle, and define it in the CICS bundle's manifest. The CICS bundle is ready to be deployed to CICS or stored in an artifact repository (if you use `mvn clean install` the CICS bundle is installed into your local .m2 repository at the end of the build).

    The generated CICS bundle takes its bundle ID from the Maven module's `artifactId` and its version from the Maven module's `version`.

1. To include CICS bundleparts like FILE or URIMAP, put the bundlepart files in your bundle Maven module's bundle parts directory, which defaults to `src/main/bundleParts`. Files in your Maven module's bundle parts directory will be included within the output CICS bundle, and supported types will have a `<define>` element added to the CICS bundle's `cics.xml`. 
The location of the bundle parts directory can be configured by using the `<bundlePartsDirectory>` property. The configured directory is relative to `src/main/`.

## Create a CICS bundle (from an existing Java module) using `cics-bundle-maven-plugin`

This way of building a CICS bundle modifies an existing Java module to make it also build the CICS bundle. This makes it more lightweight, but it has limitations - the CICS bundle can only contain one Java bundlepart, and can't contain any extra bundleparts such as FILE or URIMAP.

To create a CICS bundle in this way:

1. Take an existing OSGi bundle, WAR, EAR or EBA module. In this case we'll use the `bundle-war` goal for a WAR module, but there are matching `bundle-osgi`, `bundle-ear` and `bundle-eba` goals for those types of module. These goals bind, by default, to the `verify` phase.

1. Register the plugin to the `pom.xml` of the CICS bundle module, and add the appropriate goal as an execution, including configuration for which JVM server the CICS bundle will be installed into:

    ```xml
    <build>
      <plugins>
        <plugin>
          <groupId>com.ibm.cics</groupId>
          <artifactId>cics-bundle-maven-plugin</artifactId>
          <version>1.0.3</version>
          <executions>
            <execution>
              <goals>
                <goal>bundle-war</goal>
              </goals>
              <configuration>
                <jvmserver>DFHWLP</jvmserver>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
    ```

  Now if you build the Java module with verify phase or later, i.e. `mvn clean verify` to avoid installing the artifact locally or `mvn clean install` to install it locally in your .m2 directory, it will build the module as usual but then also wrap it in a CICS bundle, and define it in the CICS bundle's manifest. The CICS bundle is added to the output of the module, by default using the `cics-bundle` classifier, and is ready to be stored in an artifact repository or deployed to CICS.

## Deploy a CICS bundle using `cics-bundle-maven-plugin`

Following the instructions from one of the two methods above, you will have built a CICS bundle. You can use the `cics-bundle-maven-plugin` to install this into CICS by using the CICS bundle deployment API. This requires some setup in CICS as a [prerequisite](#Prerequisites).

1. Ensure a BUNDLE definition for this CICS bundle has already been created in the CSD. You will need to know the CSD group and name of the definition.
The bundle directory of the BUNDLE definition should be set as follows: `<bundle_deploy_root>/<bundle_id>_<bundle_version>`.

1. In the `pom.xml`, extend the plugin configuration to include the extra parameters below:  

    ```xml
    <build>
      <plugins>
        <plugin>
          <groupId>com.ibm.cics</groupId>
          <artifactId>cics-bundle-maven-plugin</artifactId>
          <version>1.0.3</version>
          <executions>
            <execution>
              <goals>
                <goal>bundle-war</goal>
                <goal>deploy</goal>
              </goals>
              <configuration>
                <!-- if you are deploying a Java module that also builds the CICS bundle,
                     you must specify the artifact with the cics-bundle classifier to be deployed.
                <classifier>cics-bundle</classifier>
                -->
                <defaultjvmserver>DFHWLP</defaultjvmserver>
                <url>http://yourcicsurl.com:9080</url>
                <username>${cics-user-id}</username>
                <password>${cics-password}</password>
                <bunddef>DEMOBUND</bunddef>
                <csdgroup>BAR</csdgroup>
                <cicsplex>CICSEX56</cicsplex>
                <region>IYCWEMW2</region>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
    ```
    **Note:** If you're deploying the bundle into a single CICS region environment (SMSS), omit the `<cicsplex>` and `<region>` fields.

1. Edit the values in the configuration section to match your CICS configuration.
   * `url` - Set the transport, hostname, and port for your CMCI
   * `username` & `password` - These are your credentials for CICS. Use Maven's password encryption, or supply your credentials using environment variables or properties
   * `bunddef` - The name of the BUNDLE definition to be installed
   * `csdgroup` - The name of the CSD group that contains the BUNDLE definition
   * `cicsplex` - The name of the CICSplex that the target region belongs to. This value is ignored in a single CICS region environment (SMSS).
   * `region` - The name of the region that the bundle should be installed to. This value is ignored in a single CICS region environment (SMSS).

  Now if you run the Maven build it will create the CICS bundle as above, and install this in CICS. If you run into an `unable to find valid certification path to requested target` error during deployment, see [Troubleshooting](#troubleshooting) for a fix.

  Each time you make a change to the Java project and rerun the build it will redeploy the bundle and publish your changes.

  Typically, you won't want this deployment to happen in every environment that the build is run. Placing this execution in a separate Maven profile that is only enabled in development environments is suggested.


## Using nightly/snapshot development builds

Snapshot builds are published to the Sonatype OSS Maven snapshots repository which is not available in a default Maven install.  To try a snapshot build, you will need to add the following plugin repository to your `pom.xml`:

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

- [Reactor sample](https://github.com/IBM/cics-bundle-maven/tree/main/samples/bundle-reactor-deploy)  
This sample is the best starting place if you don't already have a Java project you want to build and want to have a go at building and deploying straight away. This is a reactor project with one module including the source for a web page (including a JCICS call), which will be packaged into a WAR. It has a second module, which creates the bundle and installs this in CICS.
Further information can be found [here](samples/bundle-reactor-deploy/README.md).

- [WAR sample](https://github.com/IBM/cics-bundle-maven/tree/main/samples/bundle-war-deploy)  
This sample shows how you can add to the pom of an existing Java Maven project, to build it into a bundle and install it in CICS.
Further information can be found [here](samples/bundle-war-deploy/README.md).


## Archetypes

Another way to get started with the plugin is to use one of the provided archetypes. Maven archetypes provide parameterized templates for how a module could, or should, be structured.
There are two archetypes, one which builds and packages a WAR into a CICS Bundle, and another which then installs this bundle to CICS using the CICS bundle deployment API.
Further details on how to use the archetypes can be found [here](ARCHETYPES-README.md).

## Versioning your Maven-built CICS bundles

Maven best practice is to version your code `<version>-SNAPSHOT` during development (for instance `0.0.1-SNAPSHOT` or `1.0.0-SNAPSHOT` - for more information see the Maven doc about [SNAPSHOT versions](https://maven.apache.org/guides/getting-started/index.html#What_is_a_SNAPSHOT_version)).

Developing your code using a SNAPSHOT version will ensure that every time you build your code the previous SNAPSHOT is overwritten in your local Maven repository.

When you are happy with the quality of your code and want to make a release version, reversion your plugin to remove the `-SNAPSHOT` qualifier. The [maven-release-plugin](http://maven.apache.org/guides/mini/guide-releasing.html) provides facilities to automatically remove qualifiers, automatically updating dependencies and promoting code in your source control.

If you do not want to use the maven-release-plugin, you can update the version numbers of your parent reactor project, which will also update child project version numbers. Run this command within your parent reactor project:

```
mvn versions:set -DnewVersion=0.0.1
```

Note that this `versions:set` approach will not update dependencies (e.g. from your bundle project to your Java project) which you will need to update manually.

After releasing your code, update to your next development version number, for instance `0.0.2-SNAPSHOT`.

## Troubleshooting
### `unable to find valid certification path to requested target` during deployment
**Why does it happen?**  
You may run into this error when deploying your CICS bundle.
```
sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
```
It indicates an issue with establishing a trusted connection over TLS/SSL to the remote server (CICS Bundle Deployment API). It may happen when you are using a self-signed certificate or a certificate that's issued by an internal certificate authority, or that the certificate is not added to the trusted certificate list of your JVM.

**How to resolve it?**  
You have two ways of resolving this issue:
1. **Recommended** Obtain the server certificate(s) and add it/them to the trusted certificate list to your JVM:  
For security consideration, you may still want the TLS/SSL checking to be enabled. In this case, follow the instructions in [How do I import a certificate into the truststore](https://backstage.forgerock.com/knowledge/kb/article/a94909995) to trust the server's certificate, supplying your server's information. More information about the command involved is listed below:
    * [openssl s_client](https://www.openssl.org/docs/man1.1.0/man1/openssl-s_client.html)
    * [openssl x509](https://www.openssl.org/docs/man1.1.0/man1/openssl-x509.html)
    * [Certificate encoding & extensions](https://support.ssl.com/Knowledgebase/Article/View/19/0/der-vs-crt-vs-cer-vs-pem-certificates-and-how-to-convert-them)

1. Disable TLS/SSL certificate checking:  
Add `<insecure>true</insecure>` to the `<configuration/>` block for the deploy goal in the bundle's `pom.xml` (See snippet in Step 2 in [deploy a CICS bundle to a target CICS region](#deploy-a-cics-bundle-using-cics-bundle-maven-plugin)).  
**Note:** Trusting all certificates can pose a security issue for your environment.

### `internal server error` during deployment  
You might see this error in the Maven log when you deploy a CICS bundle:  
```
com.ibm.cics.bundle.deploy.BundleDeployException: An internal server error occurred. Please contact your system administrator
```  
**Why does it happen?**  
It indicates errors on the CMCI JVM server side.  
**How to resolve it?**  
Contact your system administrator to check the `messages.log` file of the CMCI JVM server. For more information about how to resolve CMCI JVM server errors, see [Troubleshooting CMCI JVM server](https://www.ibm.com/docs/en/cics-ts/5.6?topic=troubleshooting-cmci-jvm-server) in CICS documentation.  

### `Error creating directory` during deployment
You might see this message in the Maven log when deploying a CICS bundle:  
```
[ERROR]  - Error creating directory '<directory>'.
```
**Why does it happen?**  
The error occurs because the user ID that deploys the bundle doesn't have access to the bundles directory.  
**How to resolve it?**  
Contact your system administrator to make sure the `deploy_userid` configured for the CICS bundle deployment API has WRITE access to the bundles directory. The bundles directory is specified on the `com.ibm.cics.jvmserver.cmci.bundles.dir` option in the JVM profile of the CMCI JVM server.  
For instructions on how to specify the bundles directory and grant access to `deploy_userid`, see [Configuring the CMCI JVM server for the CICS bundle deployment API](https://www.ibm.com/docs/en/cics-ts/5.6?topic=suc-configuring-cmci-jvm-server-cics-bundle-deployment-api) in CICS documentation.

## Contributing

We welcome contributions! Find out how in our [contribution guide](CONTRIBUTING.md).

## Support

The CICS bundle Maven plugin is supported as part of the CICS Transaction Server for z/OS license. Problems can be raised as [IBM Support cases](https://www.ibm.com/mysupport/), and requests for enhancement can use the [Ideas site](https://ibm-z-software-portal.ideas.ibm.com/), for product CICS Transaction Server for z/OS.

Equally, problems and enhancement requests can be raised here on GitHub, as [new issues](https://github.com/IBM/cics-bundle-maven/issues/new).

## License

This project is licensed under the Eclipse Public License, Version 2.0.
