![build](https://github.com/ArneLimburg/microjpa/workflows/build/badge.svg) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=security_rating)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=bugs)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=coverage)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa)


# MicroJPA

MicroJPA is a framework to provide injection of resource-local ``EntityManager``s and their factories via ``@PersistenceContext`` and ``@PersistenceUnit`` in CDI-environments where no such injection is present.
That may be the case in testing scenarios or with servers that do not implement the full JEE stack like [Meecrowave](https://openwebbeans.apache.org/meecrowave/).  

## Latest Release

Latest release version: `0.9.5`

To use it in a Maven project the following dependency should be added to the pom.xml:
```xml
<dependency>
  <groupId>org.microjpa</groupId>
  <artifactId>microjpa</artifactId>
  <version>0.9.5</version>
</dependency>
```

or when using Gradle the following line needs to be added to the dependencies block:

```groovy
implementation 'org.microjpa:microjpa:0.9.5'
```

The version listed here may not always be up-to-date. It can be checked on [Maven Central](https://search.maven.org/artifact/org.microjpa/microjpa).

## Build

### Requirements

In order to use requirements are necessary:
 * JDK 8 or newer
 * Maven 3 or newer

### Build and install locally

Check out the repository and run

```bash
$ git clone git@github.com:ArneLimburg/microjpa.git
$ cd microjpa
$ mvn verify
``` 

This uses openwebbeans by default. WELD can be used by passing `-Pweld` to the `mvn` calls.

After all test run successfully, _MicroJPA_ can be installed into the local Maven repository via:

```bash
$ mvn install
```
