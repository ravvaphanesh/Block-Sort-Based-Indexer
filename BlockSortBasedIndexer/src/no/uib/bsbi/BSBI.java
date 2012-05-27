package no.uib.bsbi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import no.uib.bsbi.io.impl.BSBIIndexer;
import no.uib.bsbi.io.impl.BSBINormalizer;
import no.uib.bsbi.io.impl.BlockIndexReader;
import no.uib.bsbi.io.impl.MergeIndexWriter;
import no.uib.bsbi.io.impl.TextFilesBlockReader;
import no.uib.bsbi.models.impl.TermPostingsList;


public class BSBI {
	
	private int blockFinishedCounter;
	private int noBlocks;
	private boolean textFileReaderFinished;
	private boolean normalizerFinished;
	private boolean blockReaderFinished;

	public BSBI() {
		
		File textFilesDirectory = new File("documents");
		if(!textFilesDirectory.isDirectory()) {
			System.err.println("No directory called " + textFilesDirectory.getName() + "found...");
			System.err.println("Exit System");
			System.exit(1);
		}
		File[] documents = textFilesDirectory.listFiles();
		int noDocuments = documents.length;
		int documentsPerBlock = Math.round(Math.round(noDocuments /(Math.log(noDocuments))));
		
		ArrayList<File[]> blocksList = new ArrayList<File[]>();
		for(int i = 0; i < noDocuments; i += documentsPerBlock) {
			int size = (i + documentsPerBlock) < noDocuments ? documentsPerBlock : noDocuments - i - 1;
			System.out.println("i:" + i + ", noDocuments:" + noDocuments + ", documentsPerBlock:" + size);
			File[] block = new File[size];
			System.out.println(block.length);
			for(int j = 0; j < size; j++) {
				block[j] = documents[i+j];
			}
			blocksList.add(block);
		}
		
		HashMap<String, Integer> termList = new HashMap<String, Integer>(400000);
		HashMap<String, Integer> documentList = new HashMap<String, Integer>(1500);
		
		// For each block, index that block
		for(int i = 0; i < blocksList.size(); i++) {
			
			File[] block = blocksList.get(i);
			
			int noDocs = block.length;
			
			BlockingQueue<String[]> normalizeQueue = new LinkedBlockingQueue<String[]>();
			BlockingQueue<String[]> indexQueue = new LinkedBlockingQueue<String[]>();
			
			textFileReaderFinished = false;
			normalizerFinished = false;
			
			TextFilesBlockReader reader = new TextFilesBlockReader(this, block, normalizeQueue);
			BSBINormalizer normalizer = new BSBINormalizer(normalizeQueue, indexQueue, this);
			BSBIIndexer indexer = new BSBIIndexer(indexQueue, termList, documentList, noDocs, i, this);
			
			new Thread(reader).start();
			new Thread(normalizer).start();
			new Thread(indexer).start();
			
			while(!indexer.finished()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Block finished....");
					break;
				}
			}
		}
		
		System.out.println("Blocks finished. Try to merge....");
		
		PriorityBlockingQueue<TermPostingsList> mergeQueue = new PriorityBlockingQueue<TermPostingsList>();
		
		// Directory and file to store indexes in...
		File invertedIndexFile = new File("index" + File.separator + "invertedindex.txt");
		File blocksDirectory = new File("blocks");
		
		blockFinishedCounter = 0;
		noBlocks = 0;

		Thread consumer;
		try {
			consumer = new Thread(
					new MergeIndexWriter(this, mergeQueue, invertedIndexFile)
					);
			
			for(File block : blocksDirectory.listFiles()) {

				Thread producer = new Thread(new BlockIndexReader(this, mergeQueue, block));
				producer.start();
				noBlocks++;
			}
			
			consumer.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("The merge writer or one of the block readers" +
					" encountered an IO problem. Shutting down indexer...");
			System.exit(1);
		}
	}
	
	
	public synchronized void incrementBlockFinishedCounter() {
		blockFinishedCounter++;
	}
	
	public synchronized  boolean getBlocksFinishedCounter() {
		return blockFinishedCounter == noBlocks;
	}
	
	public synchronized boolean getTextFileReaderFinished() {
		return textFileReaderFinished;
	}
	
	public synchronized void setTextFileReaderFinished() {
		textFileReaderFinished = true;
	}
	
	public synchronized boolean getNormalizerFinished() {
		// TODO Auto-generated method stub
		return normalizerFinished;
	}
	
	public synchronized void setNormalizerFinished() {
		normalizerFinished = true;
	}
	
	public synchronized boolean getBlockReaderFinished() {
		return blockReaderFinished;
		
	}
	
	public synchronized void setBlockReaderFinished() {
		blockReaderFinished = true;
	}
	
	public static void main(String[] args) {
		new BSBI();
	}

}
