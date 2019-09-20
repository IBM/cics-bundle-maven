# Archetype Maven Settings

Running the Maven project that the cics-bundle-deploy-reactor-archetype has generated will provide the following error:
"[ERROR] Failed to execute goal com.ibm.cics:cics-bundle-maven-plugin:0.0.1-SNAPSHOT:deploy (deploy) on project {artifactId}-bundle: Server 'deployserver' does not exist"

The Maven settings for 'deployserver' are in the included settings.xml of this project, in the {artifactId}-bundle directory. By default the run configuration will be pointing at the global m2 user settings, so you either need to copy the settings into your global m2 settings.xml file, or change the run configuration for this project to set the user settings as the settings.xml file in this project. 
