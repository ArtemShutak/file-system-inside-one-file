package com.shutart.filesys.domain;

public final class FileNameUtil {

	public static final char SEPARATOR = '/';

	private FileNameUtil() {}
	
	public static String normalizeFileName(String fileName){
		if (fileName.lastIndexOf(SEPARATOR)==-1)
			fileName = SEPARATOR + fileName;
		return fileName;
		
	}

	public static String getSimpleName(String fileName) {
		int index = fileName.lastIndexOf(SEPARATOR);
		return fileName.substring(index+1);
	}

	public static String getPath(String fileName) {
		int index = fileName.lastIndexOf(SEPARATOR);
		return fileName.substring(0, index+1);
	}
}
