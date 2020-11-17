# Build

## Requirements

In order to use requirements are necessary:
 * JDK 8 or newer
 * Maven 3 or newer

## Build and install locally

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
