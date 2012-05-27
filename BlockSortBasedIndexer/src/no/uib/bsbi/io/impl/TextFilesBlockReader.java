package no.uib.bsbi.io.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.uib.bsbi.BSBI;
import no.uib.bsbi.io.BlockReaderIntf;


public class TextFilesBlockReader implements BlockReaderIntf {

	BSBI bsbi;
	BufferedReader reader;
	File[] textFiles;
	BlockingQueue<String[]> normalizeQueue;

	public TextFilesBlockReader(BSBI bsbi, File[] blockFiles, BlockingQueue<String[]> normalizeQueue) {
		this.bsbi = bsbi;
		this.normalizeQueue = normalizeQueue;
		this.textFiles = blockFiles;
	}

	@Override
	public void run() {

		int timeoutInMilliSeconds = 500; // Queue.offer should time out after 500 ms.

		for(File textFile : textFiles) {
			try {
				reader = new BufferedReader(new FileReader(textFile));

				String text = "";
				String line;
				while((line = reader.readLine()) != null) {
					text += line;
				}
				
				String[] document = new String[2];
				document[0] = textFile.getName();
				document[1] = text;
				
				boolean inserted = false;
				while(!inserted) {
					try {
						normalizeQueue.offer(document, timeoutInMilliSeconds, TimeUnit.MILLISECONDS);
						inserted = true;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.err.println("Queue.offer timed out (waited " + timeoutInMilliSeconds + " milliseconds), queue full for too long!");
						System.err.println("Try again....");
					}
				}
				
				reader.close();
				bsbi.setTextFileReaderFinished();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
