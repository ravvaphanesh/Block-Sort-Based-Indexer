package com.snorreware.io.impl;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.snorreware.BSBI;

import com.snorreware.utils.Stemmer;

import com.snorreware.io.NormalizerIntf;

public class BSBINormalizer implements NormalizerIntf {

	private BlockingQueue<String[]> normalizeQueue;
	private BlockingQueue<String[]> indexQueue;
	private BSBI bsbi;

	public BSBINormalizer(BlockingQueue<String[]> normalizeQueue, 
			BlockingQueue<String[]> indexQueue,
			BSBI bsbi) {
		this.normalizeQueue = normalizeQueue;
		this.indexQueue = indexQueue;
		this.bsbi = bsbi;
	}

	@Override
	public void run() {

		int timeoutInMillis = 1000;
		boolean readerFinished = false;
		while(!readerFinished) {
			try {
				Stemmer stemmer = new Stemmer(); // Init porter stemmer
				String[] document = normalizeQueue.poll(timeoutInMillis, TimeUnit.MILLISECONDS);
				if(document != null) {

					String text = document[1];
					String latinNormalized = Normalizer.normalize(text, Form.NFC);
					String normalized = latinNormalized.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase();
					System.out.println(normalized);
					
					//StringTokenizer wordsToStem = new StringTokenizer(text);
					//String normalized ="";

//					while(wordsToStem.hasMoreElements()) {
//						String token = wordsToStem.nextToken().toLowerCase();
//						
//						char[] chars = token.toCharArray();
//						if(Character.isLetter(chars[0])) {
//							stemmer.add(token.toCharArray(), token.length());
//							stemmer.stem();
//							normalized += stemmer.toString() + " ";
//							
//						} else {
//							normalized += token + " ";
//						}
//					}
					//System.out.println("Document: " + normalized);
					document[1] = text;

					boolean inserted = false;
					while(!inserted) {
						try {
							indexQueue.offer(document, timeoutInMillis, TimeUnit.MILLISECONDS);
							inserted = true;
						} catch (InterruptedException e) {
							e.printStackTrace();
							System.out.println("Waited for index queue to be available. Try again...");
						}
					}
				} else {
					throw new InterruptedException();
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(bsbi.getTextFileReaderFinished()) {
					readerFinished = true;
					bsbi.setNormalizerFinished();
					System.out.println("Normalizer finished!");
				} else {
					System.out.println("Waited for reader queue element to be available. Try again....");
				}

			}

		}

	}
}
