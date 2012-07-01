package com.shutart.onefilefs.test.completed;

import com.shutart.filesys.domain.IDisk;
import com.shutart.filesys.domain.OneFileDisk;
import com.shutart.onefilefs.test.util.AbstractDiskTest;
import static org.junit.Assert.*;

public final class OneFileDiskTest extends AbstractDiskTest {

	private static final String DISK_NAME = "testPath/testDiskInOneFile";

	@Override
	public IDisk getNewDisk(int numberOfPages, int pageSize) {
		IDisk disk = OneFileDisk
				.getInstance(DISK_NAME, numberOfPages, pageSize);
		assertNotNull(disk);
		return disk;
	}

}
