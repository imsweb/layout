8/21/2021 FD

The documentation used to be included in the library (all versions). But that really increased the size of the library 
and other than File*Pro, no software needed the old versions (as far as I know). 

And so I decided to "archive" the documentation files and use those externally in File*Pro. The library still
contains the documentation for the last 2 versions. To be consistent, I added all the versions to the archive folder.

To use the documentation externally, ZIP up the "naaccrXX" documentation folder you want (or all of them) and pass
the ZIP file as an argument to the layout methods that get the documentation for a given field. Those method work
with either a ZIP file or an exploded directory.
