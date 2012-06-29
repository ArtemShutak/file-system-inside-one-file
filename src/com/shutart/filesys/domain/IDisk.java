package com.shutart.filesys.domain;

public interface IDisk {
	
	int getNumberOfPages();
	
	/**
	 * @return page size in bytes
	 */
	int getPageSize();
	
	/**
	 * @return the bytes of page by number (as {@link #getPagesContent(pageNumber, 0, pageSize)})
	 */
	byte[] getPageContent(int pageNumber);
	
	/**
	 * 
	 * @param pageNumber - number of page 
	 * 	(from 0 to {@link #getNumberOfPages()}-1)
	 * @param from - values: 0 - ({@link #getPageSize()}-1)
	 * @param to - values: 1 - {@link #getPageSize()}
	 * @return
	 */
	byte[] getPageContent(int pageNumber, int from, int to);
	/**
	 * Equivalent to the {@link #setPageContent(pageNumber, 0, pageSize, pageContent)
	 */
	void setPageContent(int pageNumber, byte[] pageContent);
	
	/**
	 * 
	 * @param pageNumber
	 * @param from - values: 0 - ({@link #getPageSize()}-1)
	 * @param pageContent
	 */
	void setPageContent(int pageNumber, int from, byte[] pageContent);

	boolean delete();

	int getSizeInBytes();

	void clear();



}
