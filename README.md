# Layout Framework

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
[Version](VERSION) | 
[Changelog](changelog.txt) | 
[Snapshots](http://cilantro.imsweb.com:8080/nexus/content/repositories/snapshots/com/imsweb/layout/) | 
[Releases](http://cilantro.imsweb.com:8080/nexus/content/repositories/releases/com/imsweb/layout/)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Group ID: ```com.imsweb```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Artifact ID: ```layout```

Use this framework to define fixed-length-columns and comma-separated file formats. You can then use those formats to easily read.write records from/to given data files.

The format specificiations are typically defined with an XML file, but they can also be created programmatically.

The framework supports the recent version of NAACCR out-of-the-box (those are sometimes called the "internal" layouts).

The Layout factory can be used to register a cutomized layout, get an internal layout, or get information about a given data file (which layout can handle it).
