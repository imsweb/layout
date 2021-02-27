This folder contains the NAACCR lookups used in several SEER software.

Only the "simple" lookups are available here (the ones that correspond 
to a simple collection of code/label pairs).

The "lookups" folder contains individual lookups as CSV files (the file 
names are the NAACCR XML IDs of the NAACCR data items). The files contain 
two columns (code and label) and have no header. With one exception: the 
histology data item is a bit more complex and two CSV files were provided 
for that item: a simple one that has the same format as the other CSV 
files and a more complex ones that includes headers). All the CSV files 
are bundled into a "naaccr-lookups.zip" file so they can be easily 
downloaded.

Most (if not all) of these lookups are available in the NAACCR online 
documentation (https://www.naaccr.org/data-standards-data-dictionary/).
But the codes/labels are embedded in large blocks of HTML and difficult 
to extract. The lookups provided in this folder are meant to be easily 
consumed by a program without having to parse the HTML of the NAACCR
website.

You are welcome to download the lookups and use them in your programs, 
but they are provided as-is. You are welcome to report a wrong code or 
label if you find one, but that does not necessarily mean it will be 
fixed.

These lookups were provided via the SEER program (www.seer.cancer.gov).
The NAACCR organization (www.naaccr.org) was not involved in this effort.
