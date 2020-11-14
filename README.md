![build](https://github.com/ArneLimburg/microjpa/workflows/build/badge.svg) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=security_rating)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=bugs)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=ArneLimburg_microjpa&metric=coverage)](https://sonarcloud.io/dashboard?id=ArneLimburg_microjpa)


# MicroJPA

MicroJPA is a framework to provide injection of resource-local ``EntityManager``s and their factories via ``@PersistenceContext`` and ``@PersistenceUnit`` in CDI-environments where no such injection is present.
That may be the case in testing scenarios or with servers that do not implement the full JEE stack like [Meecrowave](https://openwebbeans.apache.org/meecrowave/).  