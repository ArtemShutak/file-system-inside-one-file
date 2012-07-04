package com.shutart.filesys.filesysimpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.shutart.filesys.domain.FSConstans;
import com.shutart.filesys.domain.IBytesOfFile;
import com.shutart.filesys.domain.IDiskDriver;
import com.shutart.filesys.domain.IFile;
import com.shutart.filesys.domain.IFileSystem;
import com.shutart.filesys.filesysimpl.FileId2FileAttrsMapper.FileAttrs;

public class FileSysImpl implements IFileSystem {
	
	private final IDiskDriver diskDriver;
	private final FileName2FileIdMapper fileName2FileId;
	private final FileId2FileAttrsMapper fileId2FileAttrs;

	public FileSysImpl(IDiskDriver diskDriver) {
		this(diskDriver, true);
	}
	
	public FileSysImpl(IDiskDriver diskDriver, boolean isNewDisk) {
		this.diskDriver = diskDriver;
		this.fileName2FileId = new FileName2FileIdMapper(this);
		int fileId4NamesOfFiles = -1;
		if (isNewDisk){
			diskDriver.formatDisk();
			
			fileId4NamesOfFiles = diskDriver.initNewFileAndGetFileId();
			if (fileId4NamesOfFiles != FileName2FileIdMapper.FILE_ID_4_THIS_MAP)
				throw new IllegalStateException("actual fileId=" + fileId4NamesOfFiles);
			
			int fileId4FileId2FileAttrs = diskDriver.initNewFileAndGetFileId();
			if (fileId4FileId2FileAttrs != FileId2FileAttrsMapper.FILE_ID)
				throw new IllegalStateException("actual fileId=" + fileId4FileId2FileAttrs);
		}
		this.fileId2FileAttrs =  new FileId2FileAttrsMapper(this, isNewDisk);
		if (isNewDisk){
//			initAttrs(fileId4FileId2FileAttrs);
//			initAttrs(fileId4NamesOfFiles);
//			FileAttrs attrs2 = getDefaultAttrs(fileId4FileId2FileAttrs);
//			fileId2FileAttrs.put(fileId4FileId2FileAttrs, attrs2);

			FileAttrs attrs = fileId2FileAttrs.getDefaultAttrs(fileId4NamesOfFiles);
			fileId2FileAttrs.put(fileId4NamesOfFiles, attrs);
		
			attrs.setLastModified(System.currentTimeMillis());
//			attrs2.setLastModified(System.currentTimeMillis());
		}
	}

	@Override
	public void clear() {
		fileName2FileId.clear();
		fileId2FileAttrs.clear();
		diskDriver.formatDisk();
	}

	@Override
	public boolean deleteFile(String fileName) {
		if (! exists(fileName))
			return false;
		Integer fileId = fileName2FileId.get(fileName);
		if (fileId == null)
			return false;
		deleteFile(fileId );
		return true;
	}


	@Override
	public boolean deleteFile(IFile file) {
		if (! exists(file))
			return false;
		Integer fileId = fileName2FileId.get(file.getName());
		if (fileId == null)
			return false;
		deleteFile(fileId);
		return true;
	}
	
	private void deleteFile(int fileId) {
		fileId2FileAttrs.deleteAttrs(fileId);
		fileName2FileId.deleteFileName(fileId);		
		diskDriver.deleteFile(fileId);
	}

	@Override
	public char getSeparator() {
		return FSConstans.SEPARATOR;
	}

	@Override
	public long length(IFile file) {
		if (!exists(file))
			return 0;
		Integer fileId = fileName2FileId.get(file.getName());
		if (fileName2FileId == null)
			throw new IllegalArgumentException();
		int size = diskDriver.getBytesOfFile(fileId).size();
		diskDriver.releaseBytesOfFile(fileId);
		return size;
	}

	@Override
	public boolean isWritable(IFile file) {
		return isWritable(file.getName());
	}
	
	boolean isWritable(String file) {
		if (!exists(file))
			return false;
		return !attrsOf(file).isReadOnly();
	}


	@Override
	public boolean setWritable(IFile file, boolean isWritable) {
		if (!exists(file))
			return false;
		attrsOf(file).setReadOnly(!isWritable);
//		attrsOf(file).setLastModified(System.currentTimeMillis());
		return true;
	}

	@Override
	public long lastModified(IFile file) {
		if(!exists(file))
			return FSConstans.START_LAST_MODIF_VAL;
		return attrsOf(file).getLastModified();
	}

	@Override
	public boolean setLastModified(IFile file, long time) {
		if(!exists(file))
			return false;
		attrsOf(file).setLastModified(time);
		return true;
	}

	@Override
	public OutputStream getNewOutputStream(final IFile file, boolean append) throws FileNotFoundException {
		return getNewOutputStream(file.getName(), append);
	}
	
	OutputStream getNewOutputStream(String fileName, boolean append) throws FileNotFoundException {
		if (!exists(fileName))
			initFile(fileName);
		if (!isWritable(fileName))
			throw new FileNotFoundException();
		final Integer fileId = fileName2FileId.get(fileName);
		if (fileId == null)
			throw new IllegalArgumentException();
		return getNewOutputStream(fileId, attrsOf(fileId), append);
//		if (append) {
//			return new AppendedOutputStream(diskDriver, fileId, attrsOf(fileId));
//		} else {
//			return new NoAppendedOutputStream(diskDriver, fileId, attrsOf(fileId));
//		}
	}
	
	OutputStream getNewOutputStream(int fileId, FileAttrs fileAttrs, boolean append){
		return getNewOutputStream(fileId, fileAttrs, append, true);
	}
	
	OutputStream getNewOutputStream(int fileId, FileAttrs fileAttrs, boolean append, boolean needUpdateFileAttrs){
		if (append) {
			return new AppendedOutputStream(diskDriver, fileId, attrsOf(fileId), needUpdateFileAttrs);
		} else {
			return new NoAppendedOutputStream(diskDriver, fileId, attrsOf(fileId), needUpdateFileAttrs);
		}
	}
	
	@Override
	public InputStream getNewInputStream(IFile file, int startByteIndex)
			throws FileNotFoundException {
		if (!exists(file))
			throw new FileNotFoundException();
		return getNewIntputStream(file.getName(), startByteIndex);
	}
	
	InputStream getNewIntputStream(int fileId, int startByteIndex) {
		return new MyInputStream(diskDriver, fileId, startByteIndex);
	}
	
	InputStream getNewIntputStream(String fileName, int startByteIndex) throws FileNotFoundException {
		if (! exists(fileName))
			throw new FileNotFoundException();
		final Integer fileId = fileName2FileId.get(fileName);
		if (fileId == null)
			throw new IllegalArgumentException();
		return getNewIntputStream(fileId, startByteIndex);
	}

	//TODO
//	private BytesOfFile bytesOf(IFile file){
//		if (file == null)
//			throw new NullPointerException();
//		int fileId = fileName2FileId.get(file.getName());
//		return bytesOf(fileId);
//	}

//	private BytesOfFile bytesOf(int fileId) {
//		if (fileId == -1)
//			throw new IllegalStateException();
//		BytesOfFile rez = diskDriver.getBytesOfFile(fileId );
//		if (rez == null){
//			throw new IllegalStateException();
//		}
//		return rez;
//	}
	
	private FileAttrs attrsOf(IFile file) {
		if (file == null)
			throw new NullPointerException();
		return attrsOf(fileName2FileId.get(file.getName()));
	}
	
	private FileAttrs attrsOf(String fileName) {
		if (fileName == null)
			throw new NullPointerException();
		return attrsOf(fileName2FileId.get(fileName));
	}
	
	FileAttrs attrsOf(Integer fileId) {
		if (fileId == null)
			throw new IllegalStateException();
		FileAttrs rez =  fileId2FileAttrs.getAttrsOf(fileId);
		if (rez == null){
			throw new IllegalStateException();
		}
		return rez;
	}
	
	@Override
	public boolean initFile(IFile file) {
		if (file == null)
			throw new NullPointerException();
		return initFile(file.getName());
	}
	
	@Override
	public boolean initFile(String fileName) {
		if (exists(fileName))
			return false;
		if (fileName.length() > FileName2FileIdMapper.MAX_LENGTH_OF_FILE_NAME)
			throw new IllegalArgumentException("length of file Name '"+fileName+"' more than " + FileName2FileIdMapper.MAX_LENGTH_OF_FILE_NAME);
		int newFileId = diskDriver.initNewFileAndGetFileId();
		fileName2FileId.put(fileName, newFileId);
		initAttrs(newFileId);
		return true;
	}

	private void initAttrs(int newFileId) {
		FileAttrs attrs = fileId2FileAttrs.getDefaultAttrs(newFileId);
		fileId2FileAttrs.put(newFileId, attrs);
		attrs.setLastModified(System.currentTimeMillis());
	}

	@Override
	public boolean exists(IFile file) {
		if (file == null)
			throw new NullPointerException();
		return exists(file.getName());
	}

	boolean exists(String fileName) {
		if (fileName == null)
			throw new NullPointerException();
		return fileName2FileId.get(fileName) != null;
	}

	private static abstract class MyAbstractOutputStream extends OutputStream{
		protected final IBytesOfFile bytes;
		
		private final IDiskDriver diskDriver;
		private final int fileId;
		private final FileAttrs attrs;
		private boolean isClosed;
		private final boolean needUpdateFileAttrs;
		
		MyAbstractOutputStream(IDiskDriver diskDriver, int fileId, FileAttrs attrs, boolean needUpdateFileAttrs){
			this.diskDriver = diskDriver;
			this.fileId = fileId;
			this.bytes = diskDriver.getBytesOfFile(fileId);
			this.attrs = attrs;
			this.needUpdateFileAttrs = needUpdateFileAttrs;
		}
		
		@Override
		public final void write(int b) throws IOException {
			if (isClosed)
				throw new IOException("Stream already closed");
			writeBody(b);
			if (needUpdateFileAttrs)
				attrs.setLastModified(System.currentTimeMillis());
		}
		
		abstract void writeBody(int b) ;
		
		@Override
		public void close(){
			if (isClosed)
				return;
			isClosed = true;
			try {
				super.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			diskDriver.releaseBytesOfFile(fileId);
		}
		
	}

	private static final class AppendedOutputStream extends MyAbstractOutputStream{
	
		AppendedOutputStream(IDiskDriver diskDriver, int fileId, FileAttrs attrs, boolean needUpdateFileAttrs) {
			super(diskDriver, fileId,attrs, needUpdateFileAttrs);
		}
		
		@Override
		void writeBody(int b) {
			bytes.add((byte) b);
		}
	}

	private static final class NoAppendedOutputStream extends MyAbstractOutputStream{
		private int index;
	
		NoAppendedOutputStream(IDiskDriver diskDriver, int fileId, FileAttrs attrs, boolean needUpdateFileAttrs) {
			super(diskDriver, fileId, attrs, needUpdateFileAttrs);
			bytes.clear();
			if (needUpdateFileAttrs)
				attrs.setLastModified(System.currentTimeMillis());
		}
		
		@Override
		void writeBody(int b) {
			if (bytes.size()<index)
				bytes.set(index, (byte) b);
			else
				bytes.add((byte)b);
			index++;
		}
	}

	private static final class MyInputStream extends InputStream{
		
		private int index;
		private final IBytesOfFile bytes;
		private final IDiskDriver diskDriver;
		private final int fileId;
		private boolean isClosed;
		
		MyInputStream(IDiskDriver diskDriver, int fileId, int startByteIndex){
			index = startByteIndex;
			this.diskDriver = diskDriver;
			this.fileId = fileId;
			this.bytes = diskDriver.getBytesOfFile(fileId);
		}
		
		@Override
		public int read() throws IOException {
			if (isClosed)
				throw new IOException("Stream already closed");
			int rez = -1;
			if (available() > 0){
				rez = bytes.get(index++) & 0xFF;
			}
			return rez ;
		}
		
		@Override
		public int available() throws IOException {
			return bytes.size() - index;
		}
		
		@Override
		public void close() {
			if (isClosed)
				return;
			isClosed = true;
			try {
				super.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			diskDriver.releaseBytesOfFile(fileId);
		}
		
	}

	void setBytes(int fileId, int startWritePosition, byte[] bytes) {
		setBytes(fileId, startWritePosition, bytes, 0, bytes.length);
	}
	
	void setBytes(String fileName, int startWritePosition, byte[] bytes) {
		if (!exists(fileName))
			throw new IllegalArgumentException();
		setBytes(fileName2FileId.get(fileName), startWritePosition, bytes, 0, bytes.length);
	}

	void setBytes(int fileId, int startWritePosition, byte[] bytes, int from,
			int length) {
		if (bytes == null)
			throw new NullPointerException();
		if (startWritePosition < 0 || from < 0 || length < 0
				|| from + length > bytes.length)
			throw new IllegalArgumentException("startWritePosition="
					+ startWritePosition + " from=" + from + " length="
					+ length);
		IBytesOfFile bytesOfFile = diskDriver.getBytesOfFile(fileId);
		if (startWritePosition > bytesOfFile.size())
			throw new IllegalArgumentException();
		for (int writeIndex = startWritePosition, i = from; i < from + length; writeIndex++, i++) {
			if (writeIndex < bytesOfFile.size())
				bytesOfFile.set(writeIndex, bytes[i]);
			else
				bytesOfFile.add(bytes[i]);
		}
		if (fileId != FileId2FileAttrsMapper.FILE_ID)
			attrsOf(fileId).setLastModified(System.currentTimeMillis());
	}

}
