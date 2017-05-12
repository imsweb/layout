# Layout Framework

[![Build Status](https://travis-ci.org/imsweb/layout.svg?branch=master)](https://travis-ci.org/imsweb/layout)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imsweb/layout/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imsweb/layout)

The layout framework is a Java library  that allows file formats to be declared using XML definition files;
those formats can then be used to read and write corresponding data files.

## Features

* Supports XML definitions for *fixed-length-columns* and *comma-separated-values* formats.
* Other types of format can be supported, but those need to be defined programmatically.
* Recent [NAACCR](http://www.naaccr.org/) formats are included with the library, including all the data items documentation.
* Supports NAACCR HL7 2.5.1 (no data item documentation is available for that format yet).
* Provides data file format auto-discovery.

## Download

The library is available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.imsweb%22%20AND%20a%3A%22layout%22).

To include it to your Maven or Gradle project, use the group ID `com.imsweb` and the artifact ID `layout`.

You can check out the [release page](https://github.com/imsweb/layout/releases) for a list of the releases and their changes.

## Usage

### Reading a NAACCR file

Each line is read as a record that is represented as a map using keys defined
in the [internal XML definition files](https://github.com/imsweb/layout/tree/master/src/main/resources/layout/fixed/naaccr). 

To read all the records of a file:

``` java
Layout layout = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT);
for (<Map<String, String> record : (RecordLayout)layout.readAllRecords(new File("my_file.txt")))
    processRecord(record);
```

For large files, the "readNextRecord" method should be used instead.

The library also supports reading NAACCR HL7 files:

```java
Layout layout = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_HL7_2_5_1);
for (Hl7Message message : (NaaccrHl7Layout)layout.readAllMessages(new File("my_file.txt")))
    processMessage(message);
```

### Using format auto-discovery

This feature allows the library to detect which layouts can be used to handle a given file.

For NAACCR files, the NAACCR version and record types are used. For generic fixed-length-columns files,
the line lengths are used. And for generic comma-separated-values formats, the number of fields is used.

``` java
List<LayoutInfo> possibleFormats = LayoutFactory.discoverFormat(new File("my_file.txt"));
if (!possibleFormats.isEmpty())
    System.out.println("Best format for this data file is " + possibleFormats.get(0));
else
    System.out.println("No registered format can be used for this data file");
```

If the default behavior is not good enough, you can extend the generic algorithm and override the
"buildFileInfo" method to provide your own definition of what data files the layout can support.

### Registering new formats

From an XML file:

``` xml
<fixed-column-layout id="my-layout" name="My Layout" length="10">
    <field name="field1" start="1" end="10"/>
</fixed-column-layout>
```

``` java
FixedColumnsLayout layout = new FixedColumnsLayout(new File("my_layout.xml"))
LayoutFactory.registerLayout(layout);
```

Programmatically:

``` java
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