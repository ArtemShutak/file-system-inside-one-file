package com.shutart.onefilefs.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.shutart.filesys.domain.FileNameUtil;
import com.shutart.filesys.domain.IFile;

public final class RealFileWrap implements IFile{
	
	private final File file;
	private final String fileName;
	
	public RealFileWrap(String name) {
		fileName = FileNameUtil.normalizeFileName(name);
		if (name.indexOf('/')==0)
			name = name.substring(1);
		file = new File(name);
	}

	@Override
	public String getName() {
		return fileName;
	}

	@Override
	public String getSimpleName() {
		return FileNameUtil.getSimpleName(fileName);
	}

	@Override
	public String getPath() {
		return FileNameUtil.getPath(fileName);
	}

	@Override
	public OutputStream getNewOutputStream() throws FileNotFoundException {
		return new FileOutputStream(file);
	}

	@Override
	public OutputStream getNewOutputStream(boolean append)
			throws FileNotFoundException {
		return new FileOutputStream(file, append);
	}

	@Override
	public InputStream getNewInputStream() throws FileNotFoundException {
		return new FileInputStream(file);
	}

	@Override
	public InputStream getNewInputStream(long startByteIndex) throws FileNotFoundException{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canWrite() {
		return file.canWrite();
	}

	@Override
	public boolean delete() {
		return file.delete();
	}

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public long lastModified() {
		return file.lastModified();
	}

	@Override
	public boolean setLastModified(long time) {
		return file.setLastModified(time);
	}

	@Override
	public boolean setReadOnly() {
		return file.setReadOnly();
	}

	@Override
	public boolean setReadOnly(boolean isReadOnly) {
		return file.setWritable(!isReadOnly);
	}

	@Override
	public boolean createNewFile() {
		try {
			return file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

}
