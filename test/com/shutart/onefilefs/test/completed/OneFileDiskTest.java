package com.shutart.onefilefs.test.completed;

import com.shutart.filesys.domain.IDisk;
import com.shutart.filesys.domain.OneFileDisk;
import com.shutart.onefilefs.test.util.AbstractDiskTest;

public final class OneFileDiskTest extends AbstractDiskTest{

	@Override
	public IDisk getNewDisk(int numberOfPages, int pageSize) {
		return new OneFileDisk("testPath/testDiskInOneFile", numberOfPages, pageSize);
	}

}
