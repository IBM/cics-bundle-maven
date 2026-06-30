# Build Requirements

Verify the JDK version used in .github/workflows/maven-build.yml - check that JAVA_HOME and PATH include this version before attempting to build locally.

# Making updates
Generally the tests should all pass locally. Run a clean build before starting updates to verify this.
The build takes several minutes to run and produces a lot of output, pipe it to a file and search the output afterwards to preserve context.

# Integration Tests

All WireMock-based integration tests share a single port reserved by build-helper-maven-plugin. The maven-invoker-plugin version is pinned at 3.6.1 (see comment in cics-bundle-maven-plugin/pom.xml explaining why we cannot upgrade).

# Local Build Failure with maven-invoker-plugin 3.6.1

The `samples` module fails locally when running `mvn clean verify`:

**Failure:**
- Sample: `samples/bundle-reactor-deploy`
- Goal: `cics-bundle:deploy` during `verify` phase
- Error: HTTP 404 "Context Root Not Found" from `http://localhost:9080`
- Build passes in CI but fails locally

**Needs debugging** - the failure is environment-specific and the root cause is unclear. The sample attempts to deploy to `http://localhost:9080` which returns an Open Liberty 404 page.