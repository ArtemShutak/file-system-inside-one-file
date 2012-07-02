package com.shutart.filesys.domain;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

final class FileName2FileIdMapper {
	static final int FILE_ID_4_THIS_MAP = 0;
	static final int MAX_LENGTH_OF_FILE_NAME = 100;
	private static final int FREE_ENTRY_MARKER = -1;
	private static final int SIZE_OF_ONE_ENTRY = (4 + MAX_LENGTH_OF_FILE_NAME) + 4;

	private final FileSysImpl fileSys;

	public FileName2FileIdMapper(FileSysImpl fileSys) {
		this.fileSys = fileSys;
	}

	public Integer get(String fileName) {
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
					dataIn.skip(SIZE_OF_ONE_ENTRY);
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
		int numberOfEntryWithThisFileId = numberOfEntryWithThisFileId(fileId);
		if (numberOfEntryWithThisFileId == -1)
			return;
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(FREE_ENTRY_MARKER);
		byte[] bytes = buf.array();
		int startPos = numberOfEntryWithThisFileId * SIZE_OF_ONE_ENTRY;
		fileSys.setBytes(FILE_ID_4_THIS_MAP, startPos, bytes);
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
					dataIn.skip((4 + MAX_LENGTH_OF_FILE_NAME) + 4);
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
		if (numberOfEntryWithThisFileId(fileId) != -1)
			throw new IllegalArgumentException("fileId:"+fileId+" already contains");
		int freeEntryNumber = numberOfEntryWithThisFileId(FREE_ENTRY_MARKER);
		
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
