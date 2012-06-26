package com.shutart.filesys.domain;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IFileSystem {

	void clear();

	IFile getFileByName(String fileName);

	boolean isEmpty();

	void deleteFile(String fileName);

	boolean deleteFile(IFile file);

	boolean containsFile(String fileName);
	
	char getSeparator();

	long length(IFile file);

	boolean canWriteTo(IFile file);
	boolean setReadOnly(IFile file, boolean isReadOnly);

	long lastModified(IFile file);

	OutputStream getNewOutputStream(IFile file, boolean append) throws FileNotFoundException;

	InputStream getNewInputStream(IFile file, long startByteIndex) throws FileNotFoundException ;

	boolean setLastModified(IFile file, long time);

	boolean exists(IFile file);

	boolean initFile(IFile file);

	
}
