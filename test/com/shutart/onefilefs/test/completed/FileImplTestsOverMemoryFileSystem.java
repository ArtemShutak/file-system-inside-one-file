package com.shutart.onefilefs.test.completed;

import com.shutart.filesys.domain.FileImpl;
import com.shutart.filesys.domain.IFile;
import com.shutart.filesys.domain.IFileSystem;
import com.shutart.onefilefs.test.util.AbstractFileTests;
import com.shutart.onefilefs.test.util.MemoryFileSystem;

public final class FileImplTestsOverMemoryFileSystem extends AbstractFileTests {

	@Override
	protected IFile newFile(String name) {
		IFileSystem fileSys = new MemoryFileSystem();
		return new FileImpl(name, fileSys);
	}

	@Override
	protected void diskDriverReleaseDisk() {
	}

}
