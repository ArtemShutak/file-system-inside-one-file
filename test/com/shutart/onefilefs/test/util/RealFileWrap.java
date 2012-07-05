package com.shutart.onefilefs.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

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
	public InputStream getNewInputStream(int startByteIndex) throws FileNotFoundException{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWritable() {
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
	public boolean setWritable(boolean isWritable) {
		return file.setWritable(isWritable);
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

	private static final byte[] NULL_BYTE_ARRAY = {};
	@Override
	public byte[] getBytes(int fromPosition, int length) {
		if (! file.exists() || fromPosition > file.length())
			return NULL_BYTE_ARRAY;
		RandomAccessFile  raf = null;
		FileChannel channel = null;
		byte[] rez = null;
		try {
			raf = new RandomAccessFile(file, "r");
			channel = raf.getChannel();
			int actualLength =  Math.min(length, (int)length()-fromPosition);
			MappedByteBuffer buf = channel.map(MapMode.READ_ONLY, fromPosition,actualLength);
			rez = new byte[actualLength];
			buf.get(rez, 0, actualLength);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally{
			close(raf, channel);
		}
		return rez;
	}

	@Override
	public boolean setBytes(int fromPosition, byte[] bytes) {
		return setBytes(fromPosition, bytes, 0, bytes.length);
	}

	@Override
	public boolean setBytes(int fromPosition, byte[] bytes, int startByte,
			int length) {
		RandomAccessFile  raf = null;
		FileChannel channel = null;
		try {
			if (!file.exists())
				createNewFile();
			raf = new RandomAccessFile(file, "rw");
			channel = raf.getChannel();
			int actualLength =  Math.min(length, bytes.length - startByte);
			MappedByteBuffer buf = channel.map(MapMode.READ_WRITE, fromPosition,actualLength);
			buf.put(bytes, 0, actualLength);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally{
			close(raf, channel);
		}
		return true;
	}
	
	private static void close(RandomAccessFile raf, FileChannel channel) {
		try {
			if (channel != null)
				channel.close();
			if (raf != null)
				raf.close();
		} catch (IOException e1) {
			throw new IllegalStateException(e1);
		}
	}

}
