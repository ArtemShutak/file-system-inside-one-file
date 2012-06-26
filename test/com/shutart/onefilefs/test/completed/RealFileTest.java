package com.shutart.onefilefs.test.completed;

import com.shutart.filesys.domain.IFile;
import com.shutart.onefilefs.test.util.AbstractFileTests;
import com.shutart.onefilefs.test.util.RealFileWrap;

public final class RealFileTest extends AbstractFileTests {

	@Override
	protected IFile newFile(String name) {
		return new RealFileWrap(name);
	}

}
