package com.shutart.filesys.domain;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FileImpl implements IFile{
	
	private final String fileName;
	private final IFileSystem fs;

	public FileImpl(String fileName, IFileSystem fs){
		if (fileName == null || fs == null)
			throw new NullPointerException();
		this.fileName = FileNameUtil.normalizeFileName(fileName);
		this.fs = fs;
	}

	@Override
	public final String getName() {
		return fileName;
	}

	@Override
	public final String getSimpleName() {
		return FileNameUtil.getSimpleName(fileName);
	}

	@Override
	public final String getPath() {
		return FileNameUtil.getPath(fileName);
	}

	@Override
	public OutputStream getNewOutputStream() throws FileNotFoundException {
		return getNewOutputStream(false);
	}

	@Override
	public OutputStream getNewOutputStream(boolean append) throws FileNotFoundException {
		return fs.getNewOutputStream(this, append);
	}

	@Override
	public InputStream getNewInputStream() throws FileNotFoundException {
		return getNewInputStream(0);
	}

	@Override
	public InputStream getNewInputStream(long startByteIndex) throws FileNotFoundException {
//		if (fromByte >= length())
//			fromByte = length()-1;
		return fs.getNewInputStream(this, startByteIndex) ;
	}

	@Override
	public boolean canWrite() {
		return fs.canWriteTo(this);
	}

	@Override
	public boolean delete() {
		return fs.deleteFile(this);
	}

	@Override
	public long length() {
		return fs.length(this);
	}

	@Override
	public long lastModified() {
		return fs.lastModified(this);
	}

	@Override
	public boolean setLastModified(long time) {
		return fs.setLastModified(this, time);
	}

	@Override
	public boolean setReadOnly() {
		return setReadOnly(true);
	}

	@Override
	public boolean setReadOnly(boolean isReadOnly) {
		return fs.setReadOnly(this, isReadOnly);
	}

	@Override
	public boolean exists() {
		return fs.exists(this);
	}

	@Override
	public boolean createNewFile() {
		return fs.initFile(this);
	}
	
	@Override
	public final boolean equals(Object o){
		if (!(o instanceof IFile))
			return false;
		IFile file = (IFile) o;
		return fileName.equals(file.getName());
	}
	
	@Override
	public final int hashCode(){
		return fileName.hashCode();
	}
	
	@Override
	public String toString(){
		return fileName;
	}

}
