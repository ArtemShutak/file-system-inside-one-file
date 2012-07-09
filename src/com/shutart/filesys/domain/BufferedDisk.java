package com.shutart.filesys.domain;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public final class BufferedDisk extends AbstractDisk implements IDisk {

	private Map<Integer, DiskPage> pageNum2Page = new LinkedHashMap<Integer, DiskPage>(16, 0.75f, true);
	private IDisk disk;
	private final int bufferSizeInPages;

	public BufferedDisk(IDisk disk, int bufferSizeInPages) {
		super(disk.getNumberOfPages(), disk.getPageSize());
		this.disk = disk;
		this.bufferSizeInPages = bufferSizeInPages;
	}

	@Override
	protected byte[] getPageContentBody(int pageNumber, int from, int to) {
		DiskPage page = getPageContentFromBuffer(pageNumber);
		return page.getContent(from, to);
	}

	private boolean bufferFull() {
		return pageNum2Page.size() >= bufferSizeInPages;
	}

	@Override
	protected void setPageContentBody(int pageNumber, int from,
			byte[] pageContent) {
		DiskPage page = getPageContentFromBuffer(pageNumber);
		page.setContent(from, pageContent);
	}

	private DiskPage getPageContentFromBuffer(int pageNumber) {
		DiskPage page = pageNum2Page.get(pageNumber);
		if (page == null){
			if (bufferFull()){
				Iterator<Integer> pageNumIter = pageNum2Page.keySet().iterator();
				if (!pageNumIter.hasNext())
					throw new IllegalStateException();
				Integer oldPageNum = pageNumIter.next();
				DiskPage oldPage = pageNum2Page.remove(oldPageNum);
				oldPage.writeToDisk();
			}
			page = DiskPage.createAndLoad(pageNumber, disk);
			pageNum2Page.put(pageNumber, page);
		}
		return page;
	}

	@Override
	public boolean delete() {
		flush();
		pageNum2Page.clear();
		return disk.delete();
	}

	private void flush() {
		for (DiskPage page : pageNum2Page.values()) {
			page.writeToDisk();
		}
	}

	@Override
	public void release() {
		flush();
		disk.release();
	}

	
}
