package com.snorreware.io.impl;

import java.util.concurrent.BlockingQueue;

import com.snorreware.io.NormalizerIntf;

public class Normalizer implements NormalizerIntf {
	
	private BlockingQueue<String[]> normalizeQueue;
	private BlockingQueue<String[]> indexQueue;
	
	
	public Normalizer(BlockingQueue<String[]> normalizeQueue, BlockingQueue<String[]> indexQueue) {
		this.normalizeQueue = normalizeQueue;
		this.indexQueue = indexQueue;
	}

	@Override
	public void run() {
		
		boolean readerFinished = false;
		while(!readerFinished) {
			
		}
		
	}

}
