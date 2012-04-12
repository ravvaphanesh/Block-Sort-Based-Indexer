Block Sort Based Indexer README Manual
--------------------------------------

To use this software (in it's current state) you need to do the following:
1. Run the project under Java 1.6 or later (some code used does not work in < 1.6)
2. Create a folder "documents" in the project-folder (program checks for this folder)
3. Dump a bunch of .txt-documents into this folder than can be indexed (filenames should be unique)
4. Stop the program manually (no completion-detection implemented as of yet)

This program is a simple example of BSBI (block sort based indexing) and is meant to be used for
learning purposes. If you want to contribute, please feel free to do so! The indexer is currently
very dependent on the implementation of the index and type of input/output-source (i.e. text documents).
Some abstraction could be useful.


TODO

1. 	Implement a function to stop the writer once the readers are done reading the blocks
	This should be implemented using a counter in the BSBI-class. The method that iterates
	this counter must be synchronized (to prevent errors). The consumer (mergewriter) should
	utilize the timeout-version (remove) of BlockingPriorityQueue. If it times out, then it
	should check if the iterater corresponds to the number of readers, and then close the reader
	if iterator equals no. of readers...
2. 	Put the actual Buffered readers and writers in their own classes, and make an IO-interface
	This should make it possible to use SQL-servers etc as storage-medium for the blocks .....
3. 	Ponder the meaning of life, everything and the universe.
4. 	See if it is possible to enable different types of indexes trough dependency-injection

Snorre.