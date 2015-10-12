# Layout Framework

Use this framework to define fixed-length-columns and comma-separated file formats. You can then use those formats to easily read/write records from/to given data files.

The format specifications are typically defined with an XML file, but they can also be created programmatically.

The framework supports the recent versions of NAACCR out-of-the-box (those are sometimes called the "internal" layouts).

The Layout factory can be used to register a customized layout, get an internal layout, or get information about a given data file (which layout can handle it).
