## Layout Framework Version History

**Changes in version 2.2**

- Updated NAACCR XML library from version 6.5 to version 7.0.
- Updated XStream dependency from version 1.4.11.1 to version 1.4.12.
- Updated CSV library from version 5.0 to version 5.2.
- Updated commons-io library from version 2.6 to version 2.7.

**Changes in version 2.1**

- Fixed NAACCR XML layout throwing an exception for duplicate item names.
- Fixed NAACCR XML layout not properly taking into account user-defined dictionaries when reading and writing patients.
- Now ignoring invalid segments when reading NAACCR HL7 messages instead of raising an exception; this is controlled by an option. 

**Changes in version 2.0**

- Renamed many field names in standard NAACCR flat layouts to align with the official NAACCR XML IDs.
- Fixed bug in HL7 writer related to encoding.

**Changes in version 1.25**

- Added proper support for reading and writing escaped sequences in HL7 layout.
- Updated NAACCR XML library from version 6.1 to version 6.5.
- Updated CSV library from version 4.2 to version 5.0.

**Changes in version 1.24**

- Moved length as an attribute of all fields instead of just CSV fields.
- Now properly escaping values containing quotes when writing CSV data.

**Changes in version 1.23**

- Fixed two mistakes in sub-fields for two date fields in standard NAACCR 14 to 18 layouts.  

**Changes in version 1.22**

- Now returning more information in format discovery for NAACCR XML data files.
- Improved format discovery mechanism for NAACCR flat files.
- Fixed fields not properly cached when programmatically creating a fixed-columns layout.

**Changes in version 1.21**

- Now returning documentation for retired data items in NAACCR 18 layout.
- Shorten short label for "behavior" in all NAACCR layout.
- Updated NAACCR XML library from version 5.3 to version 6.1.

**Changes in version 1.20**

- Fixed behavior of getting sub-fields for NAACCR XML layout so it aligns with the behavior of flat layouts.
- Made minor improvements to the HL7 layout writing methods.
- Updated NAACCR XML library from version 5.2 to version 5.3.

**Changes in version 1.19**

- Fixed incorrect line length for NAACCR 18 Confidential layout.
- Updated NAACCR XML library from version 5.1 to version 5.2.

**Changes in version 1.18**

- Fixed some bad values for section attribute in NAACCR 18 layout.
- Added missing setter for parent layout ID in RecordLayout.
- Fixed missing length of added date fields in NAACCR XML Layout.
- Added missing getters/setters from NAACCR XML layout.

**Changes in version 1.17**

- Fixed bug in CSV writing method not using the proper separator.

**Changes in version 1.16**

- Updated NAACCR XML library from version 5.0 to version 5.1.
- Cleaned up options in record-based layouts.

**Changes in version 1.15**

- Updated NAACCR XML library from version 4.15 to version 5.0.
- Fixed discovery mechanism of NAACCR XML layout.
- Supported reading Zip files in discovery mechanism of NAACCR XML layout.
- Allowed custom descriptions for NAACCR XML layout.

**Changes in version 1.14**

- Fixed writeRecords method in RecordLayout so it properly handles writing to a GZip file.
- Added short label to NAACCR XML fields.
- Updated NAACCR 18 field documentation.
- Updated NAACCR XML library from version 4.12 to version 4.15.
- Updated XStream dependency from version 1.4.10 to version 1.4.11.1.
- Fixed warnings in the console about unsafe access to private fields.

**Changes in version 1.13**

- Updated NAACCR XML library to version 4.12.
- Fixed alignment of items #3823 and #3846 in NAACCR 18 layout.
- Updated NAACCR 18 field documentation.
- Add subfield variable to NaaccrXmlField

**Changes in version 1.12**

- Updated NAACCR XML library to version 4.11.
- Updated CSV library to version 4.2.

**Changes in version 1.11**

- Updated NAACCR XML library to version 4.10.
- Updated NAACCR 18 field documentation.
- Set default user dictionary URI in NaaccrXmlLayout.buildFileInfo() 

**Changes in version 1.10**

- Updated NAACCR XML library to version 4.8.
- Fixed padding and alignment rules in NAACCR layouts
- Fixed wrong lengths in v18 NAACCR layout for the following fields: derivedPreRx7N, afpPretreatmentLabValue, afpPreOrchiectomyLabValue, primarySiteIcdO1, addressCurrentCity, addressCurrentState

**Changes in version 1.9**

- Updated NAACCR XML library to version 4.7.
- Fixed typos in field names for items ceaPretreatmentInterpretation and dateOfLastCancerStatusYear in v18 NAACCR layout.

**Changes in version 1.8**

- Final version of the NAACCR 18 layout.
- Added new NaaccrLayout.getXmlIdFromLayoutName() utility method to create a cross-over between NAACCR layout and NAACCR XML dictionary.

**Changes in version 1.7**

- Added support for NAACCR 18; that version is not final yet and it's possible it will change in a future release.
- Added support for NAACCR XML layouts.
- Fixed wrong NAACCR Item Number for item reserved05 in v16 NAACCR layout.

**Changes in version 1.6**

- Added new utility method to load a Layout without making the assumption of which type of layout it is.
- Added a proper security environment to XStream by limiting the classes that it can create when loading XML files.
- Updated XStreams library from version 1.4.9 to version 1.4.10. 

**Changes in version 1.5.1**

- Fixed a bug in NAACCR HL7 layout where MSH segment was not properly written.

**Changes in version 1.5**

- Added support for reading and writing NAACCR HL7 2.5.1.

**Changes in version 1.4.1**

- Fixed wrong start columns for reserved11 field in NAACCR 16 layout (issue #8)

**Changes in version 1.4**

- Added support for read/write options in the record-based layout, see RecordLayoutOptions class (issue #7).
- Fields that are completely blank in the data file won't be included in the resulting map even if their definition says they can't be trimmed.
- Added support for NAACCR reserved fields; this feature is only available for NAACCR 16 and later (issue #4).
- Updated XStreams dependency to version 1.4.9.
- This library now requires Java 8.

**Changes in version 1.3.4**

- Fixed the NAACCR 16 documentation; many fields had the wrong columns information (issue #6).

**Changes in version 1.3.3**

- NAACCR 16 layout XML file was using wrong ID and name (issue #5).

**Changes in version 1.3.2**

- NPCR field (item number 3720) shouldn't be trimmed (issue #3).

**Changes in version 1.3.1**

- CSV layouts setup to ignore first line were not ignoring it in the "readAllRecords" methods (issue #2).
- Fixed documentation for a few new NAACCR 16 fields (SEER derived); removed code 90 (issue #1).

**Changes in version 1.3**

- Added support for NAACCR 16 layout and documentation.
- Restructured the Layout class hierarchy to allow support for non-single-line-based formats like XML.
- Replaced JAXB by XStream for all XML operations.
- This library now requires Java 7.

**Changes in version 1.2.1**

- Fixed some non-ASCII characters in the NAACCR 15 field documentations for a few fields.

**Changes in version 1.2**

- Added missing date subfields for the three Survival date fields added in NAACCR 15...

**Changes in version 1.1**

- Changed default NAACCR layout from 14 to 15.

**Changes in version 1.0**

- Added support for NAACCR 15 layout and documentation.
- Removed support for conversion rules.
- Layout framework split from SEER*Utils into its own project.

**Legacy changes**

- [SEER*Utils v4.9  ]  Moved all layout classes from "com.imsweb.seerutils.layout" to "com.imsweb.layout".
- [SEER*Utils v4.9  ]  Added sections to the NAACCR layout (available on the Field object); only for NAACCR 14 and up.
- [SEER*Utils v4.8.1]  Fixed minor issue where a layout wouldn't inherit the default CSS style from its parent.
- [SEER*Utils v4.7  ]  Fixed some labels in the NAACCR 14 layout.
- [SEER*Utils v4.7  ]  Changed default NAACCR layout from 13 to 14.
- [SEER*Utils v4.6.4]  Fixed typo in NAACCR 14 layout name.
- [SEER*Utils v4.6.4]  Fixed a minor issue with the toString() method of the LayoutInfo class.
- [SEER*Utils v4.6  ]  Added support for NAACCR 14 layout and documentation.
- [SEER*Utils v4.5  ]  Fixed a bug involving subfields in extended layouts.
- [SEER*Utils v4.4  ]  Fixed missing line separators in all the write methods of the layouts.
- [SEER*Utils v4.3.1]  Changed method that returns available layouts so it doesn't return 'alias' layouts.
- [SEER*Utils v4.3.1]  Changed layout writing subfields: the value is always taken from the subfields, never from the parent field.
- [SEER*Utils v4.3  ]  Split the NAACCR Abstract, Modified, Confidential and Incidence layouts into individual layouts.
- [SEER*Utils v4.2.9]  FixedLength layout should not apply to CSV and vice-versa.
- [SEER*Utils v4.2.6]  Fix bad property name in NAACCR layout.
- [SEER*Utils v4.2.5]  Fixed documentation now showing up for Census Occ Code 2010 field.
- [SEER*Utils v4.2.2]  Fixed bad property name in NAACCR 13 documentation.
- [SEER*Utils v4.2.1]  Fixed bad property name in new NAACCR 13 layout.
- [SEER*Utils v4.2  ]  Added NAACCR 13 documentation.
- [SEER*Utils v4.1  ]  Added support for Comma Separated Values layouts.
- [SEER*Utils v4.1  ]  Added support for NAACCR13.
- [SEER*Utils v4.0.5]  Fixed missing documentation for NAACCR field 'eodOld4Digit'.
- [SEER*Utils v4.0.2]  Fixed issues in NAACCR12 layout.
- [SEER*Utils v4.0  ]  Added support for coding rules in the import layout.
- [SEER*Utils v4.0  ]  Now allowing a layout to extend another one.
- [SEER*Utils v4.0  ]  Now allowing external layouts to be loaded.
- [SEER*Utils v4.0  ]  Added no-wrapping CSS style to "code" columns.
- [SEER*Utils v3.1  ]  Changed NAACCR 12 layout ID from naaccr12 to naaccr-12.
- [SEER*Utils v3.0  ]  Added a method to get the default CSS style.
- [SEER*Utils v3.0  ]  Added a method to return all the available versions of the NAACCR layouts.
- [SEER*Utils v3.0  ]  Cleaned-up generated HTML NAACCR documentation.
- [SEER*Utils v2.3.3]  Improved memory usage.
- [SEER*Utils v2.3  ]  Fixed minor issue in NAACCR documentation for item #2840.
- [SEER*Utils v2.2  ]  Added NAACCR version to the NAACCR layout.
- [SEER*Utils v2.0.1]  Fixed minor issues in the NAACCR documentation.
- [SEER*Utils v2.0.1]  Fixed wrong property name in NAACCR layout.
- [SEER*Utils v2.0  ]  Added support for NAACCR layouts.
