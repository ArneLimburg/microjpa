[![maintained](https://img.shields.io/badge/Maintained-yes-brightgreen.svg)](https://github.com/ArneLimburg/microjpa/graphs/commit-activity)
![Maven Central Version](https://img.shields.io/maven-central/v/org.microjpa/microjpa)
![build](https://github.com/ArneLimburg/transactionunit/workflows/build/badge.svg)
![Method Coverage](https://img.shields.io/badge/method%20coverage-100%25-brightgreen)
![Branch Coverage](https://img.shields.io/badge/branch%20coverage-95%25-brightgreen)
[![Liberapay](https://img.shields.io/badge/Liberapay-Donate-%23f6c915.svg)](https://liberapay.com/arnelimburg)
[![Ko-Fi](https://img.shields.io/badge/Ko--fi-Buy%20me%20a%20coffee!-%2346b798.svg)](https://ko-fi.com/arnelimburg)

# MicroJPA

MicroJPA is a framework to provide injection of resource-local ``EntityManager``s and their factories via ``@PersistenceContext`` and ``@PersistenceUnit`` in CDI-environments where no such injection is present.
That may be the case in testing scenarios or with servers that do not implement the full JEE stack like [Meecrowave](https://openwebbeans.apache.org/meecrowave/).  

## Latest Release

Latest release version: `2.1.4`

To use it in a Maven project the following dependency should be added to the pom.xml:
```xml
<dependency>
  <groupId>org.microjpa</groupId>
  <artifactId>microjpa</artifactId>
  <version>2.1.4</version>
</dependency>
```

or when using Gradle the following line needs to be added to the dependencies block:

```groovy
implementation 'org.microjpa:microjpa:2.1.4'
```
