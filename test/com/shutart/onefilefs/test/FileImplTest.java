package com.shutart.onefilefs.test;

import com.shutart.filesys.domain.FileImpl;
import com.shutart.filesys.domain.IFile;
import com.shutart.filesys.domain.IFileSystem;

public final class FileImplTest extends AbstractFileTests {

	@Override
	protected IFile newFile(String name) {
		IFileSystem fileSys = new MemoryFileSystem();
		return new FileImpl(name, fileSys);
	}

}
