package com.shutart.filesys.domain;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

final class FileId2FileAttrsMapper {
	static final int FILE_ID = 1;
	private static final int FREE_ENTRY_MARKER = -1;
	private static final byte[] FREE_ENTRY_MARKER_AS_BYTES;
//	private final Map<Integer, FileAttrs> fileId2Attrs = new HashMap<Integer, FileId2FileAttrsMapper.FileAttrs>();
	private final FileSysImpl fileSys;

	static{
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(FREE_ENTRY_MARKER);
		FREE_ENTRY_MARKER_AS_BYTES = buf.array();
	}
	
	public FileId2FileAttrsMapper(FileSysImpl fileSys, boolean isNewDisk) {
		this.fileSys = fileSys;
		if(isNewDisk){
			ByteBuffer buf = ByteBuffer.allocate(4 + sizeAttrsInBytes());
			buf.putInt(FILE_ID);
			FileAttrs attrs = getDefaultAttrs(FILE_ID);
			byte isReadOnlyByte = attrs.isReadOnly ? (byte) 1 : 0;
			buf.put(isReadOnlyByte);
			buf.putLong(attrs.lastModified);
			fileSys.setBytes(FILE_ID, 0, buf.array());
			
			attrs.setLastModified(System.currentTimeMillis());
		}
	}

	final FileAttrs getDefaultAttrs(int fileId){return new FileAttrs(fileId, false, FSConstans.START_LAST_MODIF_VAL);}

	
	public FileAttrs getAttrsOf(int fileId) {
		InputStream in = null;
		DataInputStream dataIn = null;
		try {
			in = fileSys.getNewIntputStream(FILE_ID, 0);
			dataIn = new DataInputStream(
				new BufferedInputStream(in));
			while (dataIn.available() > 0) {
				int fId = dataIn.readInt();
				if (fId == fileId){
					boolean isReadOnly = dataIn.readBoolean();
					long lastModified = dataIn.readLong();
					return new FileAttrs(fileId, isReadOnly, lastModified);
				}else{
					dataIn.skip(sizeAttrsInBytes());
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
//		FileAttrs attrs = fileId2Attrs.get(fileId);
//		return attrs;
	}
	
	public void clear() {
		try {
			OutputStream out = fileSys.getNewOutputStream(FILE_ID, fileSys.attrsOf(FILE_ID), false);
			out.close();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
//		fileId2Attrs.clear();
	}
	
	public void deleteAttrs(int fileId) {
		int numberOfEntryWithThisFileId = numberOfEntryWithThisFileId(fileId);
		int startWritePosition = numberOfEntryWithThisFileId * (4 + sizeAttrsInBytes());
		fileSys.setBytes(FILE_ID, startWritePosition, FREE_ENTRY_MARKER_AS_BYTES);
//		fileId2Attrs.remove(fileId);
	}
	
	public void put(int newFileId, FileAttrs attrs) {
		if (numberOfEntryWithThisFileId(newFileId) != -1)
			throw new IllegalStateException();
		int numberOfFreeEntry = numberOfEntryWithThisFileId(FREE_ENTRY_MARKER);
		
		putUniversal(newFileId, attrs, numberOfFreeEntry);
		// fileId2Attrs.put(newFileId, attrs);
	}
	
	private void putUniversal(int fileId, FileAttrs attrs, int numberOfEntry4Put) {
		if (fileId == FILE_ID)
			return;
		ByteBuffer buf = ByteBuffer.allocate(4 + sizeAttrsInBytes());
		buf.putInt(fileId);
		byte isReadOnlyByte = attrs.isReadOnly ? (byte) 1 : 0;
		buf.put(isReadOnlyByte);
		buf.putLong(attrs.lastModified);
		if (numberOfEntry4Put != -1) {
			int startWritePosition = numberOfEntry4Put
					* (4 + sizeAttrsInBytes());
			fileSys.setBytes(FILE_ID, startWritePosition, buf.array());
		} else {
			OutputStream out = new BufferedOutputStream(
					fileSys.getNewOutputStream(FILE_ID,
							fileSys.attrsOf(FILE_ID), true, false));
			try {
				out.write(buf.array());
				out.flush();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			} finally {
				if (out != null)
					try {
						out.close();
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}
			}
		}
		// fileId2Attrs.put(newFileId, attrs);
	}

	private int numberOfEntryWithThisFileId(int fileId) {
		InputStream in = null;
		DataInputStream dataIn = null;
		try {
			in = fileSys.getNewIntputStream(FILE_ID, 0);
			dataIn = new DataInputStream(new BufferedInputStream(in));
			int numberOfEntry = 0;
			while (dataIn.available() > 0) {
				int fId = dataIn.readInt();
				if (fId == fileId)
					return numberOfEntry;
				else{
					dataIn.skip(sizeAttrsInBytes());					
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
	
	private void updateAttrsOnDisk(int fileId, FileAttrs attrs) {
		int numberOfEntryWithThisFileId = numberOfEntryWithThisFileId(fileId);
		
		putUniversal(fileId, attrs, numberOfEntryWithThisFileId);
	}

	private static int sizeAttrsInBytes() {
		return 1 + 8;
	}
	final class FileAttrs{
		private final int fileId;
		private boolean isReadOnly;
		private long lastModified;
		
		FileAttrs(int fileId, boolean isReadOnly, long lastModified) {
			this.fileId = fileId;
			this.isReadOnly = isReadOnly;
			this.lastModified = lastModified;
		}


		boolean isReadOnly() {
			return isReadOnly;
		}

		void setReadOnly(boolean isReadOnly) {
			if (this.isReadOnly != isReadOnly){
				this.isReadOnly = isReadOnly;
				updateAttrsOnDisk(fileId, this);				
			}
		}

		long getLastModified() {
			return lastModified;
		}

		void setLastModified(long time) {
			this.lastModified = time;
			updateAttrsOnDisk(fileId, this);
		}
		
	}


}
