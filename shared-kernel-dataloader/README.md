Shared-Kernel-Dataloader
===================================================================================================

Shared-Kernel-Dataloader is a module/component that can be used to load sample and test data from
a number of sources.

## Usage

To get started import it's dependency
```xml
    <dependency>
      <groupId>org.dcsa.shared-kernel</groupId>
      <artifactId>dcsa-shared-kernel-dataloader</artifactId>
    </dependency>
```

### application.yml
In the application.yml you will want to add a dataloader section

```yaml
dcsa:
  dataloader:
    autoload: [reference]
    sources: classpath:db/dataloader-sources.yml
    endpoint: /unofficial/dataloader/load
```

Here the values are:
  * autoload - list of groups of data to load on startup (may be empty)
  * sources - location of a configuration file for specifying data sources
  * endpoint - endpoint to bind the controller to


### dataloader-sources.yml

Note: file may be named differently depending on the value in application.yml

dataloader-sources.yml will have two sections, a "defaults" and a "sources". Defaults will look like this

```yaml
defaults:
  csv:
    onChecksumError: REFRESH
```

"sources" contains a list of sources like this

```yaml
  - path: http://hostname:port/referencedata.d
    type: csv
    groups: [reference]

  - path: http://hostname:port/samples.d
    type: csv
    groups: [sample]

  - path: http://hostname:port/implementation-detail-data.d
    type: csv
    groups: [impl-detail]
    dependsOn: [reference, sample]

  - path: classpath:db/testdata.d
    type: flyway
    historyTable: flyway_testdata_history
    groups: [test]
    dependsOn: [reference, sample, impl-detail]
```

For each source the values are:

All sources:
  * path - path to load data from
  * type - "csv" or "flyway"
  * groups (list) - which groups does the source belong to
  * dependsOn (list) - which other groups does the source depend on (optional)

CSV sources additionally:
  * onChecksumError - "REFRESH" or "FAIL" - should the database be refreshed if the checksum fails or should
                      the application fail (optional)
  * includes (list) - list of tables or files to process from this source (optional)
  * excludes (list) - list of tables or files to exclude from this source (optional)

Flyway sources additionally:
  * historyTable - name of a history table for the migrations in this source.

##### A note on dependsOn

Note that dependsOn will add all dependent groups to the requested groups before running loading data, but will not reorder the imports,
so a source should never depend on a groups used later in the file as that would lead to a non-working configuration.


### csvloader.yml

CSV sources are expected to contain a file named "csvloader.yml" that describes the contents of the source. csvloader.yml also contains two sections:
  * default - defaults for each table populated by this source. The default section supports the following values - "csvheader", "emptyIsNull"
    and "otherNullValues" (described later).
  * tables - describes the tables to populate and the csv files containing the data.

The values used in the table section are:

All tables:
  * table - name of the table to populate
  * file - name of csv file to read data from
  * columns (list) - list of columns in the table if it can not be guessed from csvheader (optional*)
  * primaryKey - name of the primary key in the table if it is not the first column (optional*)
  * csvheader - true or false, overrides the default (optional)
  * emptyIsNull - true or false, overrides the default (optional)
  * otherNullValues - if need to override the default (optional)

##### Note on columns:
The columns are deduced from the csvheader if there is one by some simple rules. Spaces and dashes (-) are
converted to underscore, camelCase is converted to snake_case and the name is trimmed. In cases where the csvheader
is absent or the columns can not be deduced in this way or if the columns are not all of type string it becomes
necessary to specify the columns manually.
To specify the type of a column simply append a colon and the type to the name so instead "display_order"
use "display_order:int". The csvloader does the following type conversions:
  * "int", "integer" -> Integer::parseInt
  * "long" -> Long::parseLong
  * "bool", "boolean" -> Boolean::parseBoolean
  * "real", "float" -> Float::parseFloat
  * "double" -> Double::parseDouble
  * "str", "string" -> s -> s

If no type is specified string is assumed.

##### Note on primaryKey:

Knowing the primary key becomes necessary if the first column is not the primary key for the case where a file is
changed and the csvloader runs in "REFRESH" mode.


#### Example csvloader.yml file:
```yaml
defaults:
  csvheader: true
  emptyIsNull: true
  otherNullValues: ["null", "<null>"]

tables:
  - table: negotiation_cycle
    file: negotiationcycles.csv
    columns: [cycle_key, cycle_name, display_order:int]

  - table: port_call_part
    file: portcallpart.csv
    columns: [port_call_part, display_order:int]

  - table: publisher_pattern
    file: publisherpattern.csv
```
For further examples the [DCSA-Sample-Data](https://github.com/dcsaorg/DCSA-Sample-Data) project contains some csv sources and
csvloader.yml configuration files.

Note that the dataloader will calculate a checksum of the imported files and store in a table called "csvloader_history". This avoids loading files
already loaded and allows you to see what data were imported.

### Load data groups not loaded at startup

The endpoint defined in application.yml is used to bind to a controller and becomes a POST endpoint that takes the data groups to
load on the url. So assuming your endpoint is defined with the value "/unofficial/dataloader/load":
```yaml
dcsa:
  dataloader:
    endpoint: /unofficial/dataloader/load
```

You can make the dataloader load data by posting to the endpoint. For example to load the "sample" and "reference" data groups:
```sh
curl -X POST http://localhost:9090/unofficial/dataloader/load/sample,reference
```


## Advanced

Note that it is possible to use variable substitution in dataloader-sources.yml like this:

application.yml:
```yaml
dcsa:
  dataloader:
    autoload: [reference]
    sources: classpath:db/dataloader-sources.yml
    endpoint: /unofficial/dataloader/load
    sampledata.hostport: ${sampledata_hostport}
```

dataloader-sources.yml:
```yaml
sources:
  - path: http://${dcsa.dataloader.sampledata.hostport}/referencedata.d
    type: csv
    groups: [reference]
```
