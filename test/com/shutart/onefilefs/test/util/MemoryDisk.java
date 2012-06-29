package com.shutart.onefilefs.test.util;

import java.util.Arrays;

import com.shutart.filesys.domain.AbstractDisk;
import com.shutart.filesys.domain.IDisk;

public final class MemoryDisk extends AbstractDisk implements IDisk {
	private final byte[] bytes;
	
	public MemoryDisk(int numberOfPages, int pageSize){
		super(numberOfPages, pageSize);
		bytes= new byte[numberOfPages*pageSize];
	}
	
	private int getFirstIndexOfByteOfPage(int pageNumber) {
		return pageNumber*getPageSize();
	}

	@Override
	public boolean delete() {
		return true;
	}

	@Override
	protected byte[] getPageContentBody(int pageNumber, int from, int to) {
		int prefix = getFirstIndexOfByteOfPage(pageNumber);
		return Arrays.copyOfRange(bytes, prefix + from, prefix + to); 
	}

	@Override
	protected void setPageContentBody(int pageNumber, int from, byte[] pageContent) {
		for (int i = getFirstIndexOfByteOfPage(pageNumber) + from, j = 0; 
				j < pageContent.length; i++, j++) {
			bytes[i] = pageContent[j];
		}
	}

}
