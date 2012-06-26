package com.shutart.onefilefs.test.util.complete;

import com.shutart.filesys.domain.IFile;
import com.shutart.onefilefs.test.util.AbstractFileTests;
import com.shutart.onefilefs.test.util.RealFileWrap;

final class RealFileTest extends AbstractFileTests {

	@Override
	protected IFile newFile(String name) {
		return new RealFileWrap(name);
	}

}
