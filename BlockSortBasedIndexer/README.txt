Block Sort Based Indexer README Manual
--------------------------------------

NOTE: The implementation in the TextFileTermListWriter class is currently NOT implemented.
Implementation will be done when time permits.

To use this software (in it's current state) you need to do the following:
1. Run the project under Java 1.6 or later (some code used does not work in < 1.6)
2. Create a folder "documents" and "index" in the project-folder (program checks for these folders)
3. Dump a bunch of .txt-documents into the documents folder that can be indexed (filenames should be unique)
4. Wait for the program to finish up (console outputs a message letting you know).
5. Browse block and index files to see that everything went OK...

This program is a simple example of BSBI (block sort based indexing). It is obviously not supposed to
be used professionally. If you want to contribute, please feel free to do so! The indexer is currently
very dependent on the implementation of the index and type of input/output-source (i.e. text documents).
Some abstraction could be useful. Error handling could probably be improved. Work on the indexer will be 
continued quite irregularly.

TODO

1. Implement a MySQL-based index reader/writer to increase performance.
2. Add more documentation to make code more easy to read.