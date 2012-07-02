package com.shutart.onefilefs.test.completed;

import com.shutart.filesys.domain.IDisk;
import com.shutart.onefilefs.test.util.AbstractDiskTest;
import com.shutart.onefilefs.test.util.MemoryDisk;

public final class MemoryDiskTest extends AbstractDiskTest {

	@Override
	public IDisk getNewDisk(int numberOfPages, int pageSize) {
		return MemoryDisk.getInstance(numberOfPages, pageSize);
	}

}
