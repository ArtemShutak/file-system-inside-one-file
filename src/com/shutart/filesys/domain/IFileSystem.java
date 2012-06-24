package com.shutart.filesys.domain;

public interface IFileSystem {

	void clear();

	IFile getFileByName(String fileName);

	boolean isEmpty();

	void deleteFile(String fileName);

	void deleteFile(IFile file);

	boolean containsFile(String fileName);

}
