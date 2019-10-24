## Archetypes

Another way to get started with the plugin is to use one of the provided archetypes. Maven archetypes provide parameterized templates for how a module could, or should, be structured. 
By using `cics-bundle-reactor-archetype`, you are provided with a reactor (multi-module build) module, containing a CICS bundle module and a dynamic Web (WAR) module. The CICS bundle is preconfigured to depend on the WAR module. Building the reactor module builds both the children, so you end up with a CICS bundle that contains the built WAR file. If you use the `cics-bundle-deploy-reactor-archetype` archetype, this is extended to install the CICS bundle in CICS using the CICS bundle deployment API. 


1. Create a Maven module by referring to the either the [`com.ibm.cics:cics-bundle-reactor-archetype`](https://search.maven.org/artifact/com.ibm.cics/cics-bundle-reactor-archetype/0.0.1/maven-archetype) or `com.ibm.cics:cics-bundle-deploy-reactor-archetype` artifact:

    * ![On command line](images/cmd.png) On command line:
    
         ```
         mvn archetype:generate -DarchetypeGroupId=com.ibm.cics -DarchetypeArtifactId=cics-bundle-deploy-reactor-archetype -DarchetypeVersion=1.0.0 -DgroupId=<my-groupid> -DartifactId=<my-artifactId>
         ```
         In the pom.xml file of the bundle module there will be default values for each of the following parameters which you will need to set:  
         `defaultjvmserver`, `bunddef`, `csdgroup`, `username`, `password`, `deployURL`, `cicsplex`, `region`  
         If using the `com.ibm.cics:cics-bundle-reactor-archetype` archetype, only `defaultjvmserver` will need defining as the other parameters are used during the deploy. 
         
    * ![In Eclipse](images/eclipse.png) If you're using Eclipse:
        1. Click **File > New > Maven Project**.
        1. Click **Next** on the first page
        1. On the second page, click **Add archetype...** in the New Maven Project dialog and specify the following information, making sure to use the latest version:
    
            Archetype Group Id: `com.ibm.cics`  
            Archetype Artifact Id: `cics-bundle-deploy-reactor-archetype`              
            Archetype Version: `1.0.0`  

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
