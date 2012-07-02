package com.shutart.onefilefs.test.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.shutart.filesys.domain.AbstractDisk;
import com.shutart.filesys.domain.IDisk;

public final class MemoryDisk extends AbstractDisk implements IDisk {
	private static final Map<Integer, MemoryDisk> pseudoId2Disk = new HashMap<Integer, MemoryDisk>();
	
	public static IDisk getInstance(int numberOfPages, int pageSize){
		if (numberOfPages > 999 || pageSize > 999)
			throw new IllegalArgumentException();
		int pseudoCode = numberOfPages * 1000 + pageSize;
		MemoryDisk disk = pseudoId2Disk.get(pseudoCode);
		if (disk == null){
			disk = new MemoryDisk(numberOfPages, pageSize);
			pseudoId2Disk.put(pseudoCode, disk);
		}
		return disk;
	}
	
	private final byte[] bytes;
	
	private MemoryDisk(int numberOfPages, int pageSize){
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
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof MemoryDisk))
			return false;
		MemoryDisk disk = (MemoryDisk)obj;
		return disk.getNumberOfPages() == this.getNumberOfPages() &&
				disk.getPageSize() == this.getPageSize();
	}

	@Override
	public int hashCode() {
		int rez = 17;
		rez = rez*37 + getNumberOfPages();
		rez = rez*37 + getPageSize();
		return rez;
	}
}
