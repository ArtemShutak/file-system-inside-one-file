package com.shutart.onefilefs.test.completed;

import com.shutart.filesys.domain.DiskDriverImpl;
import com.shutart.filesys.domain.IDisk;
import com.shutart.filesys.domain.IDiskDriver;
import com.shutart.onefilefs.test.util.AbstractDiskDriverSpec;
import com.shutart.onefilefs.test.util.MemoryDisk;

public final class DiskDriverImplSpecOverMemoryDisk extends AbstractDiskDriverSpec {

	@Override
	protected IDisk getDisk(int numberOfPages, int pageSize) {
		return new MemoryDisk(numberOfPages, pageSize);
	}

	@Override
	protected IDiskDriver getDiskDriver(IDisk disk) {
		return DiskDriverImpl.getDriver4Disk(disk);
	}

}
