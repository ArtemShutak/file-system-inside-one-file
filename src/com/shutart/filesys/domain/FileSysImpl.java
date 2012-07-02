package com.shutart.filesys.domain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.shutart.filesys.domain.FileId2FileAttrsMapper.FileAttrs;

public class FileSysImpl implements IFileSystem {
	
	private final IDiskDriver diskDriver;
	private final FileName2FileIdMapper fileName2FileId = new FileName2FileIdMapper(this);
	private final FileId2FileAttrsMapper fileId2FileAttrs = new FileId2FileAttrsMapper(this);

	public FileSysImpl(IDiskDriver diskDriver) {
		this.diskDriver = diskDriver;
	}

	private final FileAttrs getDefaultAttrs(){return new FileAttrs(false, FSConstans.START_LAST_MODIF_VAL);}
	
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
		diskDriver.deleteFile(fileId);
		fileId2FileAttrs.deleteAttrs(fileId);
		fileName2FileId.deleteFileName(fileId);		
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
		if (!exists(file))
			initFile(file);
		if (!isWritable(file))
			throw new FileNotFoundException();
		final Integer fileId = fileName2FileId.get(file.getName());
		if (fileId == null)
			throw new IllegalArgumentException();
		if (append) {
			return new AppendedOutputStream(diskDriver, fileId, attrsOf(fileId));
		} else {
			return new NoAppendedOutputStream(diskDriver, fileId, attrsOf(fileId));
		}
	}
	
	@Override
	public InputStream getNewInputStream(IFile file, int startByteIndex)
			throws FileNotFoundException {
		if (!exists(file))
			throw new FileNotFoundException();
		final Integer fileId = fileName2FileId.get(file.getName());
		if (fileId == null)
			throw new IllegalArgumentException();
		
		return new MyInputStream(diskDriver, fileId, startByteIndex);
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
	
	private FileAttrs attrsOf(Integer fileId) {
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
		int newFileId = diskDriver.initNewFileAndGetFileId();
		fileName2FileId.put(fileName, newFileId);
		FileAttrs attrs = getDefaultAttrs();
		attrs.setLastModified(System.currentTimeMillis());
		fileId2FileAttrs.put(newFileId, attrs);
		return true;
	}

	@Override
	public boolean exists(IFile file) {
		if (file == null)
			throw new NullPointerException();
		return exists(file.getName());
	}

	private boolean exists(String fileName) {
		if (fileName == null)
			throw new NullPointerException();
		return fileName2FileId.get(fileName) != null;
	}

	private static abstract class MyAbstractOutputStream extends OutputStream{
		protected final BytesOfFile bytes;
		
		private final IDiskDriver diskDriver;
		private final int fileId;
		private final FileAttrs attrs;
		private boolean isClosed;
		
		MyAbstractOutputStream(IDiskDriver diskDriver, int fileId, FileAttrs attrs){
			this.diskDriver = diskDriver;
			this.fileId = fileId;
			this.bytes = diskDriver.getBytesOfFile(fileId);
			this.attrs = attrs;
		}
		
		@Override
		public final void write(int b) throws IOException {
			if (isClosed)
				throw new IOException("Stream already closed");
			writeBody(b);
			attrs.setLastModified(System.currentTimeMillis());
		}
		
		abstract void writeBody(int b) ;
		
		@Override
		public void close() throws IOException{
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
	
		AppendedOutputStream(IDiskDriver diskDriver, int fileId, FileAttrs attrs) {
			super(diskDriver, fileId,attrs);
		}
		
		@Override
		void writeBody(int b) {
			bytes.add((byte) b);
		}
	}

	private static final class NoAppendedOutputStream extends MyAbstractOutputStream{
		private int index;
		private boolean firstOperation = true;
	
		NoAppendedOutputStream(IDiskDriver diskDriver, int fileId, FileAttrs attrs) {
			super(diskDriver, fileId, attrs);
		}
		
		@Override
		void writeBody(int b) {
			if (firstOperation){
				firstOperation = false;
				bytes.clear();
			}
			if (bytes.size()<index)
				bytes.set(index, (byte) b);
			else
				bytes.add((byte)b);
			index++;
		}
	}

	private static final class MyInputStream extends InputStream{
		
		private int index;
		private final BytesOfFile bytes;
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
			if (index < bytes.size())
				rez = bytes.get(index++);
			return rez ;
		}
		
		@Override
		public void close() throws IOException{
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
}
