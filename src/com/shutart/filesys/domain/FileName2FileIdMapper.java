package com.shutart.filesys.domain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//TODO need rewrite this class
final class FileName2FileIdMapper {
	
	private final Map<String, Integer> fileName2FileId = new HashMap<String, Integer>();

	public FileName2FileIdMapper(IFileSystem fileSys) {
		// TODO Auto-generated constructor stub
	}

	public Integer get(String fileName) {
		return fileName2FileId.get(fileName);
	}

	public void clear() {
		fileName2FileId.clear();
	}

	public void deleteFileName(int fileId) {
		for (Iterator<Entry<String, Integer>> iterator = fileName2FileId.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Integer> type = iterator.next();
			if (type.getValue().equals(fileId)){
				iterator.remove();
				return;
			}
		}
	}

	public void put(String name, int fileId) {
		fileName2FileId.put(name, fileId);
	}

}
