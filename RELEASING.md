# Releasing

This repository releases two Maven plugins, `cics-bundle-maven-plugin` and `cics-appdeploy-maven-plugin`, together. They share the parent `pom.xml` and always have the same version. There is no way to release one on its own.

## Prerequisites

- Write access to this repository
- The current version on `main` must be a `-SNAPSHOT` version

## Steps

1. **Trigger the release workflow** — go to Actions → Prepare Release → Run workflow, or run:
   ```bash
   gh workflow run prepare-release.yml
   ```
   This opens a PR that strips `-SNAPSHOT` from every module's `pom.xml` (e.g. `2.0.5-SNAPSHOT → 2.0.5`) and sets the `cics-bundle-maven-plugin.version` and `cics-appdeploy-maven-plugin.version` properties in the samples to the same version.

2. **Review and merge the PR** — merging triggers the existing deploy to OSSRH via `maven-build.yml`.

3. **Publish to Maven Central** — a maintainer with Sonatype publishing permission must log in to [central.sonatype.com/publishing](https://central.sonatype.com/publishing) and publish the staged artifact.

4. **Automation takes over** — once the release PR is merged, `post-release.yml` automatically:
   - Creates a GitHub Release tagged `vX.Y.Z` with generated release notes
   - Opens a PR bumping to the next patch SNAPSHOT (e.g. `2.0.6-SNAPSHOT`)

5. **Merge the next-dev PR** — no rush, merge when ready.
