package com.shutart.onefilefs.test.util.complete;

import com.shutart.filesys.domain.FileImpl;
import com.shutart.filesys.domain.IFile;
import com.shutart.filesys.domain.IFileSystem;
import com.shutart.onefilefs.test.util.AbstractFileTests;
import com.shutart.onefilefs.test.util.MemoryFileSystem;

final class FileImplTest extends AbstractFileTests {

	@Override
	protected IFile newFile(String name) {
		IFileSystem fileSys = new MemoryFileSystem();
		return new FileImpl(name, fileSys);
	}

}
