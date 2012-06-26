package com.shutart.onefilefs.test;

import com.shutart.filesys.domain.IFile;

public final class RealFileTest extends AbstractFileTests {

	@Override
	protected IFile newFile(String name) {
		return new RealFileWrap(name);
	}

}
