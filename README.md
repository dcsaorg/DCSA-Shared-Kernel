# DCSA Shared Kernel

The repository contains common entities and related logic shared
most of DCSA APIs (such as `Location`, `Address` and `Party).
It is packaged as a jar, and uploaded to GitHub packages, to be
downloaded via Maven


Code standard
-------------------------------------
We use [Google Java Style](https://google.github.io/styleguide/javaguide.html), when using
IntelliJ it is recommended to download and activate the
[google-java-format plugin](https://github.com/google/google-java-format).


### To build manually, run:
```
mvn clean install -nsu
```

If you need any of the Services, Controllers or Repositories required by
this module, then you may have to explicitly define `basePackages` in the
`@ComponentScan` and `@EnableR2dbcRepositories` annotation.  For DCSA implementations, the
following definition should work:

```
@ComponentScan(basePackages = "org.dcsa")
@EnableR2dbcRepositories(basePackages = {"org.dcsa"}, repositoryBaseClass = ExtendedRepositoryImpl.class)
```

