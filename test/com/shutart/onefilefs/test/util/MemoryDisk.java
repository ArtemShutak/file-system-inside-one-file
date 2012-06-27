package com.shutart.onefilefs.test.util;

import java.util.Arrays;

import com.shutart.filesys.domain.IDisk;

public final class MemoryDisk implements IDisk {
	private final byte[] bytes;
	private final int pageSize;
	private final int numberOfPages;
	
	public MemoryDisk(int numberOfPages, int pageSize){
		bytes= new byte[numberOfPages*pageSize];
		this.pageSize = pageSize;
		this.numberOfPages = numberOfPages;
	}
	
	@Override
	public int getNumberOfPages() {
		return numberOfPages;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public byte[] getPageContent(int pageNumber) {
		return getPagesContent(pageNumber, pageNumber);
	}

	@Override
	public byte[] getPagesContent(int startPageNumber, int endPageNumber) {
		validatePageNumbers(startPageNumber, endPageNumber);
		return Arrays.copyOfRange(bytes, getFirstIndexOfByteOfPage(startPageNumber), 
				getFirstIndexOfByteOfPage(endPageNumber+1));
	}

	private void validatePageNumbers(int startPageNumber, int endPageNumber) {
		if (startPageNumber > endPageNumber)
			throw new IllegalArgumentException();
		if (startPageNumber < 0 || endPageNumber > maxPageNumber())
			throw new IndexOutOfBoundsException();
		
	}

	private int maxPageNumber() {
		return getNumberOfPages()-1;
	}

	private int getFirstIndexOfByteOfPage(int pageNumber) {
		return pageNumber*pageSize;
	}

	@Override
	public void setPageContent(int pageNumber, byte[] pageContent) {
		setPagesContent(pageNumber, pageNumber, pageContent);
	}

	@Override
	public void setPagesContent(int startPageNumber, int endPageNumber,
			byte[] pageContent) {
		if (pageContent == null)
			throw new NullPointerException();
		validatePageNumbers(startPageNumber, endPageNumber);
		if (pageContent.length != (endPageNumber-startPageNumber+1)*getPageSize())
			throw new IllegalArgumentException();
		for (int i = getFirstIndexOfByteOfPage(startPageNumber), j = 0; j < pageContent.length; i++, j++) {
			bytes[i] = pageContent[j];
		}
	}

	@Override
	public void delete() {
	}

}
