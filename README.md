# Layout Framework

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=imsweb_layout&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=imsweb_layout)
[![integration](https://github.com/imsweb/layout/workflows/integration/badge.svg)](https://github.com/imsweb/layout/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.imsweb/layout.svg)](https://central.sonatype.com/artifact/com.imsweb/layout)

The layout framework is a Java library that allows file formats to be declared using XML definition files;
those formats can then be used to read and write corresponding data files.

## Features

* Supports recent [NAACCR](http://www.naaccr.org/) XML formats, including fields documentation.
* Supports all recent [NAACCR](http://www.naaccr.org/) fixed-columns formats, including fields documentation.
* Supports [NAACCR](http://www.naaccr.org/) HL7 2.5.1 (no field documentation is available for that format).
* Allows other *fixed-columns* and *comma-separated-values* formats to be registered via an XML definition file. 
* Provides data file format auto-discovery.

## Download

The library is available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.imsweb%22%20AND%20a%3A%22layout%22).

To include it to your Maven or Gradle project, use the group ID `com.imsweb` and the artifact ID `layout`.

You can check out the [release page](https://github.com/imsweb/layout/releases) for a list of the releases and their changes.

This library requires Java 21.

## Usage

### Reading a NAACCR XML file

The layout returns the Patient objects contained in the file:

```java
NaaccrXmlLayout layout = LayoutFactory.getNaaccrXmlLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_18);
for (Patient patient : layout.readAllPatients(inputFile)))
    processPatient(patient);
```

For large files, consider creating a reader and using the "readPatientMessage".

### Reading a NAACCR fixed-columns file

Each line is read as a record that is represented as a map using keys defined
in the [internal XML definition files](https://github.com/imsweb/layout/tree/master/src/main/resources/layout/fixed/naaccr). 

To read all the records of a file:

```java
NaaccrLayout layout = LayoutFactory.getNaaccrFixedColumnsLayout(LayoutFactory.LAYOUT_ID_NAACCR_18);
for (<Map<String, String> record : layout.readAllRecords(inputFile))
    processRecord(record);
```

For large files, consider creating a reader and using the "readNextRecord".

### Reading a NAACCR HL7 file

The layout returns the HL7 messages contained in the file:

```java
NaaccrHl7Layout layout = LayoutFactory.getNaaccrHl7Layout(LayoutFactory.LAYOUT_ID_NAACCR_HL7_2_5_1);
for (Hl7Message message : layout.readAllMessages(inputFile)))
    processMessage(message);
```

For large files, consider creating a reader and using the "readNextMessage".

### Using format auto-discovery

This feature allows the library to detect which layouts can be used to handle a given file.

The library will try to identify one or several layouts that are suitable based on the beginning of the data file:
 - for NAACCR XML files, the root attributes are used (without reading the entire data file)
 - for NAACCR fixed-columns files, the NAACCR version and record types from the first line are used
 - for generic fixed-length-columns files, the line length is used
 - for generic comma-separated-values formats, the number of fields is used

``` java
List<LayoutInfo> possibleFormats = LayoutFactory.discoverFormat(inputFile);
if (!possibleFormats.isEmpty())
    System.out.println("Best format for this data file is " + possibleFormats.get(0));
else
    System.out.println("No registered format can be used for this data file");
```

If the default behavior is not good enough, you can extend the generic algorithm and override the
"buildFileInfo" method to provide your own definition of what data files the layout can support.

### Registering new formats

From an XML file:

```xml
<fixed-column-layout id="my-layout" name="My Layout" length="10">
    <field name="field1" start="1" end="10"/>
</fixed-column-layout>
```

```java
FixedColumnsLayout layout = new FixedColumnsLayout(new File("my_layout.xml"))
LayoutFactory.registerLayout(layout);
```

Programmatically:

```java
FixedColumnsLayout layout = new FixedColumnsLayout();
layout.setLayoutId("my-layout");
layout.setLayoutName("My Layout");
layout.setLayoutLineLength(10);
FixedColumnsField field1 = new FixedColumnsField();
field1.setName("field1");
field1.setStart(1);
field1.setEnd(10);
layout.setFields(Collections.singleton(field1));
LayoutFactory.registerLayout(layout);
````

Note that it is not necessary to register a layout for using it but the auto-discovery feature will work only with registered layouts.

## About SEER

This library was developed through the [SEER](http://seer.cancer.gov/) program.

The Surveillance, Epidemiology and End Results program is a premier source for cancer statistics in the United States.
The SEER program collects information on incidence, prevalence and survival from specific geographic areas representing
a large portion of the US population and reports on all these data plus cancer mortality data for the entire country.