package com.shutart.filesys.filesysimpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

final class FileName2FileIdMapper {
	static final int FILE_ID_4_THIS_MAP = 0;
	static final int MAX_LENGTH_OF_FILE_NAME = 100;
	private static final int FREE_ENTRY_MARKER = -1;//pseudo length of file name
	private static final int SIZE_OF_ONE_ENTRY = (4 + MAX_LENGTH_OF_FILE_NAME) + 4;

	private final FileSysImpl fileSys;
	private final Map<String, Integer> fileName2FileIdCache = new HashMap<String, Integer>();

	public FileName2FileIdMapper(FileSysImpl fileSys) {
		this.fileSys = fileSys;
	}

	public Integer get(String fileName) {
		if (fileName2FileIdCache.containsKey(fileName))
			return fileName2FileIdCache.get(fileName);
		
		Integer rez = readFromFileSys(fileName);
		if (rez != null)
			fileName2FileIdCache.put(fileName, rez);
		return rez;
	}

	private Integer readFromFileSys(String fileName) {
		if (fileName.length() > MAX_LENGTH_OF_FILE_NAME)
			return null;
		InputStream in = fileSys.getNewIntputStream(FILE_ID_4_THIS_MAP, 0);
		DataInputStream dataIn = new DataInputStream(
				new BufferedInputStream(in));
		try {
			while (dataIn.available() > 0) {
				int realLengthOfFileName = dataIn.readInt();
				if (realLengthOfFileName != FREE_ENTRY_MARKER
						&& realLengthOfFileName == fileName.length()) {
					byte[] bytesOfFileName = new byte[realLengthOfFileName];
					dataIn.read(bytesOfFileName);
					dataIn.skip(MAX_LENGTH_OF_FILE_NAME - realLengthOfFileName);
					String fName = new String(bytesOfFileName);
					if (fName.equals(fileName))
						return dataIn.readInt();
				} else {
					dataIn.skip(MAX_LENGTH_OF_FILE_NAME + 4);
				}
			}
			return null;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				if (dataIn != null)
					dataIn.close();
				else if (in != null)
					in.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public void clear() {
		fileName2FileIdCache.clear();
		try {
			OutputStream out = fileSys.getNewOutputStream(FILE_ID_4_THIS_MAP,
					fileSys.attrsOf(FILE_ID_4_THIS_MAP), false);
			out.close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void deleteFileName(int fileId) {
		if (fileId == FILE_ID_4_THIS_MAP)
			return;
		deleteFromCache(fileId);
		int numberOfEntryWithThisFileId = numberOfEntryWithThisFileId(fileId);
		if (numberOfEntryWithThisFileId == -1)
			return;
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(FREE_ENTRY_MARKER);
		byte[] bytes = buf.array();
		int startPos = numberOfEntryWithThisFileId * SIZE_OF_ONE_ENTRY;
		fileSys.setBytes(FILE_ID_4_THIS_MAP, startPos, bytes);
	}

	private void deleteFromCache(int fileId) {
		for (Iterator<Entry<String, Integer>> iterator = fileName2FileIdCache.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Integer> entry = iterator.next();
			if (entry.getValue().equals(fileId))
				iterator.remove();
		}
	}

	private int numberOfEntryWithThisFileId(int fileId) {
		InputStream in = fileSys.getNewIntputStream(FILE_ID_4_THIS_MAP, 0);
		DataInputStream dataIn = new DataInputStream(
				new BufferedInputStream(in));
		try {
			int numberOfEntry = 0;
			while (dataIn.available() > 0) {
				int realLengthOfFileName = dataIn.readInt();
				if (realLengthOfFileName != FREE_ENTRY_MARKER) {
					dataIn.skip(MAX_LENGTH_OF_FILE_NAME);
					int readingFileId = dataIn.readInt();
					if (readingFileId == fileId)
						return numberOfEntry;
				} else {
					dataIn.skip((MAX_LENGTH_OF_FILE_NAME) + 4);
				}
				numberOfEntry++;
			}
			return -1;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				if (dataIn != null)
					dataIn.close();
				else if (in != null)
					in.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private int numberOfFreeEntry() {
		InputStream in = fileSys.getNewIntputStream(FILE_ID_4_THIS_MAP, 0);
		DataInputStream dataIn = new DataInputStream(
				new BufferedInputStream(in));
		try {
			int numberOfEntry = 0;
			while (dataIn.available() > 0) {
				int realLengthOfFileName = dataIn.readInt();
				if (realLengthOfFileName == FREE_ENTRY_MARKER) {
					return numberOfEntry;
				} else {
					dataIn.skip((MAX_LENGTH_OF_FILE_NAME) + 4);
				}
				numberOfEntry++;
			}
			return -1;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				if (dataIn != null)
					dataIn.close();
				else if (in != null)
					in.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public void put(String name, int fileId) {
		if (fileId == FILE_ID_4_THIS_MAP)
			throw new IllegalArgumentException();
		fileName2FileIdCache.put(name, fileId);
		
		if (numberOfEntryWithThisFileId(fileId) != -1)
			throw new IllegalArgumentException("fileId:"+fileId+" already contains");
		int freeEntryNumber = numberOfFreeEntry();
		
		ByteBuffer buf = ByteBuffer.allocate(SIZE_OF_ONE_ENTRY);
		buf.putInt(name.length());
		buf.put(name.getBytes());
		buf.position(MAX_LENGTH_OF_FILE_NAME + 4);
		buf.putInt(fileId);
		byte[] bytes = buf.array();
		
		if (freeEntryNumber != -1){
			int startPos = freeEntryNumber * SIZE_OF_ONE_ENTRY;
			fileSys.setBytes(fileId, startPos, bytes);
		} else {
			OutputStream out = new BufferedOutputStream(
					fileSys.getNewOutputStream(FILE_ID_4_THIS_MAP,
							fileSys.attrsOf(FILE_ID_4_THIS_MAP), true));
			try {
				out.write(bytes);
				out.flush();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}finally{
				if (out!=null)
					try {
						out.close();
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}
			}
		}
	}

}
