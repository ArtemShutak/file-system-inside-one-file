package com.shutart.filesys.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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
			OneFileDisk disk = diskName2Inst.get(diskName);
			if (disk != null)
				return disk;

			String fullDiskName = getFullDiskName(diskName, numberOfPages,
					pageSize);
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

	private OneFileDisk(File file, int numberOfPages, int pageSize) {
		super(numberOfPages, pageSize);
		
		RandomAccessFile  tmpRaf = null;
		FileChannel tmpChannel = null;
		this.file = file;
		try {
			if (!file.exists())
				throw new IllegalArgumentException();
			if (numberOfPages*pageSize > file.getFreeSpace())
				throw new IllegalArgumentException(numberOfPages*pageSize +" > "+ file.getFreeSpace());
			tmpRaf = new RandomAccessFile(file, "rw");
			tmpChannel = tmpRaf.getChannel();
		} catch (FileNotFoundException e) {
			processCatchCase(tmpRaf, tmpChannel, e);
		} 
		this.channel = tmpChannel;
		this.raf = tmpRaf;
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

	private static void processCatchCase(RandomAccessFile raf, FileChannel channel,
			Exception e) {
		try {
			if (channel != null)
				channel.close();
			if (raf != null)
				raf.close();
		} catch (IOException e1) {
			throw new IllegalStateException(e1);
		}
		throw new IllegalStateException(e);
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
		boolean fileDeleted = file.delete();
		if (fileDeleted){
			try {
				channel.close();
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileDeleted ;
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
