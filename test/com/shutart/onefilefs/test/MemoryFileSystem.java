package com.shutart.onefilefs.test;

import java.util.HashMap;
import java.util.Map;

import com.shutart.filesys.domain.IFile;
import com.shutart.filesys.domain.IFileSystem;

public class MemoryFileSystem implements IFileSystem {
	
	private final Map<String, IFile> files = new HashMap<String, IFile>();

	@Override
	public void clear() {
		files.clear();
	}

	@Override
	public IFile getFileByName(String fileName) {
		IFile file = files.get(fileName);
		if (file == null){
			file = new MemoryFile(fileName);
			files.put(fileName, file);
		}
		return file;
	}

	@Override
	public boolean isEmpty() {
		return files.isEmpty();
	}

	@Override
	public void deleteFile(String fileName) {
		files.remove(fileName);
	}

	@Override
	public void deleteFile(IFile file) {
		deleteFile(file.getName());
	}

	@Override
	public boolean containsFile(String fileName) {
		return files.containsKey(fileName);
	}

}
