package com.shutart.filesys.domain;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IFileSystem {

	void clear();

	void deleteFile(String fileName);

	boolean deleteFile(IFile file);

	char getSeparator();

	long length(IFile file);

	boolean isWritable(IFile file);
	boolean setWritable(IFile file, boolean isWritable);

	long lastModified(IFile file);
	boolean setLastModified(IFile file, long time);

	OutputStream getNewOutputStream(IFile file, boolean append) throws FileNotFoundException;

	InputStream getNewInputStream(IFile file, long startByteIndex) throws FileNotFoundException ;


	boolean exists(IFile file);

	boolean initFile(IFile file);

	
}
