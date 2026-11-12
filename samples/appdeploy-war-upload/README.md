# AppDeploy WAR upload sample

This sample shows how to use `cics-appdeploy-maven-plugin` to upload a WAR file directly to a Liberty JVM server in CICS, without building a CICS bundle.

It is a different route from the `cics-bundle-maven-plugin` samples. Use this sample if your Liberty JVM server has the `cicsts:appDeploy-1.0` feature. If you want to deploy a CICS bundle, see the [WAR sample](../bundle-war-deploy) or the [reactor sample](../bundle-reactor-deploy).

## Prerequisites

- A Liberty JVM server in CICS with the `cicsts:appDeploy-1.0` feature enabled.
- The server's `appDeploy` element configured for the authentication mode you want to use (`basic`, `jwt`, `client-cert` or `none`), with `allowedDeployers` including your user.
- An HTTPS connection to the server.

## What the sample does

The `pom.xml` is an ordinary WAR project with the `cics-appdeploy:deploy` goal added. You don't configure how the WAR is built or packaged.

Running the goal does everything in order:

1. Maven compiles the code and packages the WAR.
2. The `deploy` goal sends the WAR, plus the Liberty application XML, as a `multipart/form-data` POST to your Liberty server.

Because the WAR goes to Liberty and not to a Maven repository, the sample sets `maven.deploy.skip` to skip the standard Maven deploy step.

## Running the sample

Pass your credentials on the command line. Do not put them in the `pom.xml`.

```sh
mvn cics-appdeploy:deploy -Dcics-user-id=<user> -Dcics-password=<password>
```

This builds the WAR for you and then uploads it, so you don't need to run `mvn package` first. If the project is already built, it is built again, which takes a few seconds.

`BUILD SUCCESS` means the files were delivered, not that the application started. Check its state with `GET /appdeploy/applications/<name>`; `STARTED` means it is running.

Because the WAR is built first, your project's unit tests run too. If a test fails, the build stops before anything is uploaded. Add `-DskipTests` to skip them.

`mvn deploy` also works, because the goal is bound to the `deploy` phase. In that case the WAR is built twice.

To run it against a real server, change `serverUrl` in the `pom.xml` to the `https` base URL of your Liberty JVM server, for example `https://<host>:<port>`. The plugin adds `/appdeploy/uploadApp` itself, so don't include it.

To authenticate with a JWT instead of a user ID and password, replace `userName` and `password` with `bearerToken`. To use a server with `auth="none"`, leave out all credentials.

To authenticate with a client certificate (server `auth="client-cert"`), add `clientKeyStore`, the path to a PKCS12 keystore holding the certificate and private key, in the `pom.xml`. Recommended to pass its password on the command line, not in the `pom.xml`: `-Dclient.keystore.password=<password>`, with `<clientKeyStorePassword>${client.keystore.password}</clientKeyStorePassword>`. Use `clientKeyStoreType` for a keystore that isn't PKCS12. <!-- pragma: allowlist secret -->

## Parameters

These are the `cicsAppDeploy` settings. The generated plugin documentation does not list them, because they are nested inside one parameter.

| Parameter | Description | Default |
|---|---|---|
| `serverUrl` | Base URL of the Liberty server. The plugin adds `/appdeploy/uploadApp` itself. | none (required) |
| `applicationXml` | Inline Liberty application XML. Used if both this and `applicationXmlLocation` are set. | none |
| `applicationXmlLocation` | Path to a file with the Liberty application XML. | none |
| `userName`, `password` | Basic Authentication. Set both. | none |
| `bearerToken` | JWT for the server's `jwt` mode. | none |
| `clientKeyStore` | Keystore with the client certificate and private key, for the `client-cert` mode. A relative path is resolved against the project directory. | none |
| `clientKeyStorePassword` | Password of the client keystore. It is also used for the private key. | none |
| `clientKeyStoreType` | Type of the client keystore. | `PKCS12` |
| `connectTimeout` | Time to establish the connection, in milliseconds. | `30000` |
| `readTimeout` | Time to wait for the server's response, in milliseconds. | `300000` |

Set the application XML with one of `applicationXml` or `applicationXmlLocation`. Credentials are optional: leave them all out for a server with `auth="none"` only when SEC=NO.


## Application XML

The Liberty application XML tells Liberty how to run the WAR. Supply it in one of two ways:

- `applicationXml`, inline in the `pom.xml` (used by this sample).
- `applicationXmlLocation`, the path to a file.

## Troubleshooting

- `401`: the user ID, password, token or certificate was rejected, or the authentication mode on the server doesn't match.
- `403`: you authenticated, but the user is not in `allowedDeployers`.
- `PKIX path building failed`: the JVM running Maven doesn't trust the server's certificate. Add the certificate to the trust store of that JVM.
