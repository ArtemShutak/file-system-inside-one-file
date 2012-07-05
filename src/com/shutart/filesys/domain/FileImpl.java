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
	public InputStream getNewInputStream(int startByteIndex) throws FileNotFoundException {
//		if (fromByte >= length())
//			fromByte = length()-1;
		return fs.getNewInputStream(this, startByteIndex) ;
	}
	@Override
	public byte[] getBytes(int fromPosition, int length){
		return fs.getBytes(this, fromPosition, length);
	}
	@Override
	public boolean setBytes(int fromPosition, byte[] bytes){
		return setBytes(fromPosition, bytes, 0, bytes.length);
	}
	@Override
	public boolean setBytes(int fromPosition, byte[] bytes, int startByte, int length){
		return fs.setBytes(this, fromPosition, bytes, startByte, length);
	}

	@Override
	public boolean isWritable() {
		return fs.isWritable(this);
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
		return setWritable(false);
	}

	@Override
	public boolean setWritable(boolean isWritable) {
		return fs.setWritable(this, isWritable);
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
