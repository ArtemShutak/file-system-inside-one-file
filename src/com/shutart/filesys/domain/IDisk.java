package com.shutart.filesys.domain;

public interface IDisk {
	
	int getNumberOfPages();
	
	/**
	 * @return page size in bytes
	 */
	int getPageSize();
	
	/**
	 * @param pageNumber - number of page (from 0 to {@link #getNumberOfPages()}-1)
	 * @return the bytes of page by number (as {@link #getPagesContent(pageNumber, pageNumber)})
	 */
	byte[] getPageContent(int pageNumber);
	
	/**
	 * @param startPageNumber - start number of page 
	 * 	(from 0 to {@link #getNumberOfPages()}-1)
	 * @param endPageNumber- end number of page 
	 * 	(from 0 to {@link #getNumberOfPages()}-1)
	 * @return
	 */
	byte[] getPagesContent(int startPageNumber, int endPageNumber);
	
	/**
	 * Equivalent to the {@link #setPagesContent(pageNumber, pageNumber, pageContent)}
	 */
	void setPageContent(int pageNumber, byte[] pageContent);
	/**
	 * 
	 * @param pageNumber
	 * @param pageContent - the bytes for writing on disk
	 * @throws IllegalArgumentException if pageContent.length() != {@link #getPageSize()} * (endPageNumber-startPageNumber + 1)
	 */
	void setPagesContent(int startPageNumber, int endPageNumber, byte[] pageContent);

	void delete();

}
