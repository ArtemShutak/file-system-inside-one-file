package com.shutart.filesys.domain;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Map;

public final class OneFileDisk extends AbstractDisk implements IDisk{
	
	private static final Map<String, OneFileDisk> diskName2Inst = new HashMap<String, OneFileDisk>(1);
	private static final String NUM_OF_PAGES = "numOfPages=";
	private static final String PAGE_SIZE = "pageSize=";	

	public static boolean exists(String diskName, int numberOfPages,
			int pageSize) {
		String fullDiskName = getFullDiskName(diskName, numberOfPages, pageSize);
		if (diskName2Inst.containsKey(fullDiskName))
			return true;
		File diskFile = new File(fullDiskName);
		return diskFile.exists();
	}
	
	public static OneFileDisk getInstance(String diskName, int numberOfPages,
			int pageSize) {
		try {
			String fullDiskName = getFullDiskName(diskName, numberOfPages,
					pageSize);
			OneFileDisk disk = diskName2Inst.get(fullDiskName);
			if (disk != null)
				return disk;

			File diskFile = new File(fullDiskName);
			boolean fileAlreadyExists = diskFile.exists();
			if (!fileAlreadyExists) {
				if (!diskFile.createNewFile())
					throw new IllegalStateException();
			}
			
			OneFileDisk newDiskInst = new OneFileDisk(diskFile, numberOfPages,
					pageSize);
			if (fileAlreadyExists) {
				if (newDiskInst.getSizeInBytes() != diskFile.length())
					throw new IllegalStateException();
			} else {
				newDiskInst.allocateDiskSpace();
			}

			
			diskName2Inst.put(fullDiskName, newDiskInst);
			return newDiskInst;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String getFullDiskName(String diskName, int numberOfPages,
			int pageSize) {
		return diskName + "(" + NUM_OF_PAGES + numberOfPages
				+ "; " + PAGE_SIZE + pageSize + ")";
	}
	
	private final FileChannel channel;
	private final RandomAccessFile  raf;
	private final File file;
	private final FileLock lock;

	private OneFileDisk(File file, int numberOfPages, int pageSize) {
		super(numberOfPages, pageSize);
		
		RandomAccessFile  tmpRaf = null;
		FileChannel tmpChannel = null;
		FileLock tmpLock = null;
		this.file = file;
		try {
			if (!file.exists())
				throw new IllegalArgumentException();
			if (numberOfPages*pageSize > file.getFreeSpace())
				throw new IllegalArgumentException(numberOfPages*pageSize +" > "+ file.getFreeSpace());
			tmpRaf = new RandomAccessFile(file, "rw");
			tmpChannel = tmpRaf.getChannel();
			tmpLock = tmpChannel.tryLock();
			if (tmpLock == null){
				releaseResourses(tmpRaf, tmpChannel, tmpLock);
				throw new IllegalStateException("This file is used by another process.");
			}
		} catch (IOException e) {
			releaseResourses(tmpRaf, tmpChannel, tmpLock);
			throw new IllegalStateException(e);
		} 
		this.channel = tmpChannel;
		this.raf = tmpRaf;
		this.lock = tmpLock;
		
	}

	private void allocateDiskSpace() {
		try {
			ByteBuffer buf = ByteBuffer.wrap(new byte[getPageSize()]);
			for (int i = 0; i < getNumberOfPages(); i++) {
				channel.write(buf);
				buf.flip();
			}
			channel.position(0);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void releaseResourses(RandomAccessFile raf, FileChannel channel,
			FileLock lock) {
		try {
			if (channel != null)
				channel.close();
			if (raf != null)
				raf.close();
			if (lock != null)
				lock.release();
		} catch (IOException e1) {
			throw new IllegalStateException(e1);
		}
	}
	
	@Override
	protected byte[] getPageContentBody(int pageNumber, int from, int to) {
		try {
			int prefix = pageNumber*getPageSize();
			MappedByteBuffer buf = channel.map(MapMode.READ_ONLY, prefix + from, to - from);
			byte[] rez = new byte[to-from];
			buf.get(rez);
			return rez;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	protected void setPageContentBody(int pageNumber, int from, byte[] pageContent) {
		try {
			int prefix = pageNumber*getPageSize();
			MappedByteBuffer buf = channel.map(MapMode.READ_WRITE, prefix + from, pageContent.length);
			buf.put(pageContent, 0, pageContent.length);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}

	@Override
	public int getSizeInBytes() {
		int size = super.getSizeInBytes();
		int fileSize = (int) file.length();
		if (size != fileSize)
			throw new IllegalStateException("Expected size:" + size
					+ " actual size:" + fileSize);
		return size;
	}

	@Override
	public boolean delete() {
		try {
			lock.release();
			raf.close();
			channel.close();
			return file.delete() ;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public final boolean equals(Object o){
		if (! (o instanceof OneFileDisk))
			return false;
		OneFileDisk ofd = (OneFileDisk) o;
		return ofd.file.equals(file);
	}
	
	@Override
	public final int hashCode(){
		return file.hashCode();
	}

}
