# cics-bundle-maven

A collection of Maven plugins and utilities that can be used to build CICS bundles, ready to be installed into CICS TS.

This project contains:
 - `cics-bundle-maven-plugin`, a Maven plugin that builds CICS bundles, including Java-based dependencies.
 - `cics-bundle-reactor-archetype`, a Maven archetype that shows how a simple reactor build can pull together a CICS bundle and a Java project.

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