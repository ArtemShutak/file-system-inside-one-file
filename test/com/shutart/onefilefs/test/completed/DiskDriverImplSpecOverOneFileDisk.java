package com.shutart.onefilefs.test.completed;

import static org.junit.Assert.assertNotNull;

import com.shutart.filesys.domain.IDisk;
import com.shutart.filesys.domain.IDiskDriver;
import com.shutart.filesys.driverimpl.DiskDriverImpl;
import com.shutart.filesys.onefiledisk.OneFileDisk;
import com.shutart.onefilefs.test.util.AbstractDiskDriverSpec;

public final class DiskDriverImplSpecOverOneFileDisk extends AbstractDiskDriverSpec {

	private static final String DISK_NAME = "testPath/testDiskInOneFile";
	
	@Override
	protected IDisk getDisk(int numberOfPages, int pageSize) {
		IDisk disk = OneFileDisk
				.getInstance(DISK_NAME, numberOfPages, pageSize);
		assertNotNull(disk);
		return disk;
	}

	@Override
	protected IDiskDriver getDiskDriver(IDisk disk) {
		return DiskDriverImpl.getDriver4Disk(disk);
	}

	@Override
	protected void diskDriverReleaseDisk(IDisk disk) {
		DiskDriverImpl.releaseDisk(disk);
	}

}
