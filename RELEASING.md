# Releasing

This repository releases two Maven plugins, `cics-bundle-maven-plugin` and `cics-appdeploy-maven-plugin`, together. They share the parent `pom.xml` and have one version, so every release includes both.

## Prerequisites

- Write access to this repository
- The current version on `main` must be a `-SNAPSHOT` version (e.g. `2.0.6-SNAPSHOT`). The Prepare Release workflow fails if it is not.
- Someone with permission to release on Sonatype, to publish the release in step 4
- If you are also releasing [cics-bundle-common](https://github.com/IBM/cics-bundle-common), release it first and wait until it is on Maven Central. `cics-bundle-maven-plugin` depends on it.

## Steps

1. **Trigger the release workflow** — go to Actions → Prepare Release → Run workflow, or run:
   ```bash
   gh workflow run prepare-release.yml
   ```
   This opens a PR from a `release/<version>` branch into `main`. The PR strips `-SNAPSHOT` from every module's `pom.xml` (e.g. `2.0.6-SNAPSHOT → 2.0.6`) and sets the `cics-bundle-maven-plugin.version` and `cics-appdeploy-maven-plugin.version` properties in the samples to the same version.

2. **Make any manual changes on the release branch** — the workflow does not make these changes:
   - If the version of `cics-bundle-common` has changed, update the `cics-bundle-common` dependency in `cics-bundle-maven-plugin/pom.xml`. This command uses the latest release on Maven Central:
     ```bash
     mvn versions:use-releases -Dincludes=com.ibm.cics -DgenerateBackupPoms=false -U && mvn versions:use-latest-releases -Dincludes=com.ibm.cics -DgenerateBackupPoms=false -U
     ```
   - Update the plugin version in the code snippets in `README.md` and `samples/bundle-war-deploy/README.md` to the version being released.

3. **Review and merge the PR** — merging triggers `maven-build.yml` on `main`, which runs `mvn -P sign deploy` and uploads the signed artifacts to Sonatype Central through its OSSRH staging API. Check the progress of the build under [Actions](https://github.com/IBM/cics-bundle-maven/actions).

4. **Check and publish to Maven Central** — check the staged release, then ask someone with permission to release it to publish it at [central.sonatype.com/publishing](https://central.sonatype.com/publishing). Afterwards, check that the new version is on Maven Central for both plugins. The parent `cics-bundle-maven` and the two archetypes are also deployed with them. Search results are cached, so browse [repo.maven.apache.org/maven2/com/ibm/cics/](https://repo.maven.apache.org/maven2/com/ibm/cics/) instead.

5. **Automation takes over** — merging the release PR also triggers `post-release.yml`, which:
   - Creates a GitHub Release tagged `vX.Y.Z` with generated release notes
   - Opens a `develop/<version>` PR bumping to the next patch SNAPSHOT (e.g. `2.0.7-SNAPSHOT`)

6. **Merge the next-dev PR** — no rush, merge when ready.
