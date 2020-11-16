![build](https://github.com/ArneLimburg/microjpa/workflows/build/badge.svg) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=security_rating)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=bugs)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=coverage)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa)


# MicroJPA

MicroJPA is a framework to provide injection of resource-local ``EntityManager``s and their factories via ``@PersistenceContext`` and ``@PersistenceUnit`` in CDI-environments where no such injection is present.
That may be the case in testing scenarios or with servers that do not implement the full JEE stack like [Meecrowave](https://openwebbeans.apache.org/meecrowave/).

## Build

### Requirements

In order to build the library _GPG_ and _Maven_ are required.
 * https://maven.apache.org/download.cgi
 * https://gnupg.org/download/index.html

GPG is required in the _verify_ step to sign the package. It can be disabled for local development via `-Pdisable-gpg` command line parameter.  

### Run tests

#### Local

To run the tests locally run:

```
$ mvn verify -Popenwebbeans
$ mvn verify -Pweld
```

#### Via Docker

To run the tests using Docker build the containers and run them:

```
$ docker build -t microjpa-owb -f Dockerfile.owb .
$ docker build -t microjpa-weld -f Dockerfile.weld .
$ docker run --rm microjpa-owb
$ docker run --rm microjpa-weld
```
