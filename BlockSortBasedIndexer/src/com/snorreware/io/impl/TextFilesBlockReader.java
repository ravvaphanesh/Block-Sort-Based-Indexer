package com.snorreware.io.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.snorreware.io.BlockReaderIntf;

public class TextFilesBlockReader implements BlockReaderIntf {

	BufferedReader reader;
	File[] textFiles;
	BlockingQueue<String[]> normalizeQueue;

	public TextFilesBlockReader(File blockDirectory, BlockingQueue<String[]> normalizeQueue) {
		initFiles(blockDirectory);
		this.normalizeQueue = normalizeQueue;
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

				System.out.println("Finished reading the block. Close reader...");
				reader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initFiles(File blockDirectory) {

		if(!blockDirectory.isDirectory()) {
			if(blockDirectory.isFile()) {
				blockDirectory.delete();
				blockDirectory.mkdir();
			} else if(!blockDirectory.exists()) {
				blockDirectory.mkdir();
			}
		}

		for(File file : blockDirectory.listFiles()) {
			if(!file.getName().endsWith(".txt")) {
				file.delete();
			}
		}

		textFiles = blockDirectory.listFiles();
	}
}
