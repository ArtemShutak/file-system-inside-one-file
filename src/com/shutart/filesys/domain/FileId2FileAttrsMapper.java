package com.shutart.filesys.domain;

import java.util.HashMap;
import java.util.Map;

final class FileId2FileAttrsMapper {
	
	private final Map<Integer, FileAttrs> fileId2Attrs = new HashMap<Integer, FileId2FileAttrsMapper.FileAttrs>();

	public FileId2FileAttrsMapper(FileSysImpl fileSysImpl) {
		// TODO Auto-generated constructor stub
	}

	public FileAttrs getAttrsOf(int fileId) {
		FileAttrs attrs = fileId2Attrs.get(fileId);
		return attrs;
	}
	
//	всю информацию для данного класса необходимо синхронизировать с диском
	final static class FileAttrs{

		private boolean isReadOnly;
		private long lastModified;
		
		FileAttrs(boolean isReadOnly, long lastModified) {
			this.isReadOnly = isReadOnly;
			this.lastModified = lastModified;
		}

		boolean isReadOnly() {
			return isReadOnly;
		}

		void setReadOnly(boolean isReadOnly) {
			this.isReadOnly = isReadOnly;
		}

		long getLastModified() {
			return lastModified;
		}

		void setLastModified(long time) {
			this.lastModified = time;
		}
		
	}
	public void clear() {
		fileId2Attrs.clear();
	}

	public void deleteAttrs(int fileId) {
		fileId2Attrs.remove(fileId);
	}

	public void put(int newFileId, FileAttrs attrs) {
		fileId2Attrs.put(newFileId, attrs);
	}

}
