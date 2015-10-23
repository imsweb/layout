Layout Framework Version History
--------------------------------

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
