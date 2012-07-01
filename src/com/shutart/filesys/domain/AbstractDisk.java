package com.shutart.filesys.domain;



public abstract class AbstractDisk implements IDisk {
	
	private final int pageSize;
	private final int numberOfPages;
	
	protected AbstractDisk(int numberOfPages, int pageSize){
		this.pageSize = pageSize;
		this.numberOfPages = numberOfPages;
	}
	
	@Override
	public final int getNumberOfPages() {
		return numberOfPages;
	}

	@Override
	public final int getPageSize() {
		return pageSize;
	}
	
	@Override
	public byte getByte(int pageNumber, int innerIndex) {
		return getPageContent(pageNumber, innerIndex, innerIndex + 1)[0];
	}


	@Override
	public final byte[] getPageContent(int pageNumber) {
		return getPageContent(pageNumber, 0, getPageSize());
	}
	
	private void validateParams(int pageNumber, int from, int to) {
		if (from < 0  ||  to > getPageSize() || 
				pageNumber < 0 || pageNumber > maxPageNumber())
			throw new IndexOutOfBoundsException();
		if (to <= from || to - from > getPageSize())
			throw new IllegalArgumentException();
	}
	
	private int maxPageNumber() {
		return getNumberOfPages()-1;
	}

	@Override
	public final byte[] getPageContent(int pageNumber, int from, int to) {
		validateParams(pageNumber, from, to);
		return getPageContentBody(pageNumber,from, to);
	}

	protected abstract byte[] getPageContentBody(int pageNumber, int from, int to) ;

	@Override
	public void setByte(int pageNumber, int innerIndex, byte b) {
		setPageContent(pageNumber, innerIndex, new byte[]{b});
	}

	@Override
	public final void setPageContent(int pageNumber, byte[] pageContent) {
		setPageContent(pageNumber, 0, pageContent);
	}
	
	@Override
	public final void setPageContent(int pageNumber, int from, byte[] pageContent) {
		if (pageContent == null)
			throw new NullPointerException();
		validateParams(pageNumber, from, from + pageContent.length);
		setPageContentBody(pageNumber, from, pageContent);
	}
	
	protected abstract void setPageContentBody(int pageNumber, int from, byte[] pageContent) ;

	@Override
	public abstract boolean delete() ;

	@Override
	public int getSizeInBytes() {
		return numberOfPages*pageSize;
	}
	
	@Override
	public void clear(){
		final byte[] zeroArray = new byte[pageSize]; 
		for (int i = 0; i < numberOfPages; i++) {
			setPageContent(i, zeroArray);
		}
	}

}
