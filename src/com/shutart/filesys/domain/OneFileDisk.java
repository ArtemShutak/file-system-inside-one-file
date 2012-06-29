package com.shutart.filesys.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public final class OneFileDisk extends AbstractDisk implements IDisk{
	
	private final FileChannel channel;
	private final RandomAccessFile  raf;
	private final File file;

	public OneFileDisk(String fileName, int numberOfPages, int pageSize) {
		super(numberOfPages, pageSize);
		
		RandomAccessFile  tmpRaf = null;
		FileChannel tmpChannel = null;
		file = new File(fileName);
		try {
			file.createNewFile();
			if (numberOfPages*pageSize > file.getFreeSpace())
				throw new IllegalArgumentException(numberOfPages*pageSize +" > "+ file.getFreeSpace());
			tmpRaf = new RandomAccessFile(file, "rw");
			tmpChannel = tmpRaf.getChannel();
		} catch (FileNotFoundException e) {
			processCatchCase(tmpRaf, tmpChannel, e);
		} catch (IOException e) {
			processCatchCase(tmpRaf, tmpChannel, e);
		} 
		this.channel = tmpChannel;
		this.raf = tmpRaf;
		
		allocateDiskSpace();
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
		try {
			channel.close();
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.delete();
	}

}
