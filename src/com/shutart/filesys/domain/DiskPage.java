package com.shutart.filesys.domain;

final class DiskPage {
	
	private final IDisk disk;
	private final byte[] bytes;
	private final int pageNumber;
	private volatile boolean changed;
	
	private DiskPage(int pageNumber, byte[] bytes, IDisk disk) {
		this.pageNumber = pageNumber;
		this.bytes = bytes;
		this.disk = disk;
	}

	static DiskPage createAndLoad(int pageNumber, IDisk disk) {
		byte[] bytes = disk.getPageContent(pageNumber);
		DiskPage page = new DiskPage(pageNumber, bytes, disk);
		return page;
	}

	byte[] getContent(int from, int to) {
		byte[] dest = new byte[to-from];
		System.arraycopy(bytes, from, dest, 0, to-from);
		return dest;
	}
	
	void setContent(int from, byte[] pageContent) {
		changed = true;
		System.arraycopy(pageContent, 0, bytes, from, pageContent.length);
	}

	void writeToDisk() {
		if (changed){
			disk.setPageContent(pageNumber, bytes);
			changed = false;
		}
	}

}
