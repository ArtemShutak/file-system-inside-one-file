package com.shutart.onefilefs.test.completed;

import com.shutart.filesys.domain.BufferedDisk;
import com.shutart.filesys.domain.FSConstans;
import com.shutart.filesys.domain.FileImpl;
import com.shutart.filesys.domain.IDisk;
import com.shutart.filesys.domain.IDiskDriver;
import com.shutart.filesys.domain.IFile;
import com.shutart.filesys.domain.IFileSystem;
import com.shutart.filesys.driverimpl.DiskDriverImpl;
import com.shutart.filesys.filesysimpl.FileSysImpl;
import com.shutart.filesys.onefiledisk.OneFileDisk;
import com.shutart.onefilefs.test.abstracttests.AbstractFileTests;

public final class FileImplTestsOverOneFileDisk extends AbstractFileTests {

	private IDisk disk;
	
	@Override
	protected IFile newFile(String name) {
		disk = OneFileDisk.getInstance("testPath/" + FSConstans.DISK_NAME,
				FSConstans.DISK_NUMBER_OF_PAGES, FSConstans.DISK_PAGE_SIZE);
//		disk = MemoryDisk.getInstance(FSConstans.DISK_NUMBER_OF_PAGES, 
//				FSConstans.DISK_PAGE_SIZE);
		disk = new BufferedDisk(disk, FSConstans.DISK_BUFFER_SIZE_IN_PAGES);
		IDiskDriver diskDriver = DiskDriverImpl.getDriver4Disk(disk);
		diskDriver.formatDisk();
		IFileSystem fs = new FileSysImpl(diskDriver);
		return new FileImpl(name, fs);
	}
	
	@Override
	protected void diskDriverReleaseDisk() {
		disk.release();
		DiskDriverImpl.releaseDisk(disk);
	}

}
