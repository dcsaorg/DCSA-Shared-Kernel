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
`@ComponentScan` annotation.  For DCSA implementations, the
following definition should work:

```
@ComponentScan(basePackages = "org.dcsa")
```


## Including Shared-Kernel modules in other projects

### Including all modules

Using the following dependency will include all shared-kernel modules in your project:

```
      <dependency>
        <groupId>org.dcsa.shared-kernel</groupId>
        <artifactId>dcsa-shared-kernel-all</artifactId>
        <version>${dcsa.shared-kernel.version}</version>
      </dependency>
```

### Picking which modules to use

The parent pom of Shared-Kernel can be used as a BOM to define all the version for all modules.
Import the parent in ```dependencyManagement``` part of the root pom in your project:

```
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.dcsa.shared-kernel</groupId>
        <artifactId>dcsa-shared-kernel-parent</artifactId>
        <version>${dcsa.shared-kernel.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
```

After that you can depend on modules independently:

```
  <dependencies>
    <dependency>
      <groupId>org.dcsa.shared-kernel</groupId>
      <artifactId>dcsa-shared-kernel-domain</artifactId>
    </dependency>
    <dependency>
      <groupId>org.dcsa.shared-kernel</groupId>
      <artifactId>dcsa-shared-kernel-errors</artifactId>
    </dependency>
    <dependency>
      <groupId>org.dcsa.shared-kernel</groupId>
      <artifactId>dcsa-shared-kernel-infrastructure</artifactId>
    </dependency>
  </dependencies>
```
