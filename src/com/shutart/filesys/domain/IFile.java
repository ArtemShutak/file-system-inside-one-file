package com.shutart.filesys.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IFile {
	/**
	 * @return absolute name of the file
	 */
	String getName();

	/**
	 * @return
	 * @see {@link File#getName()}
	 */
	String getSimpleName();
	
	String getPath();
	
	/**
	 * @return {@link #getNewInputStream(false)}
	 * @throws FileNotFoundException 
	 */
	OutputStream getNewOutputStream() throws FileNotFoundException;
	OutputStream getNewOutputStream(boolean append) throws FileNotFoundException;

	/**
	 * @return {@link #getNewOutputStream(0)}
	 * @throws FileNotFoundException 
	 */
	InputStream getNewInputStream() throws FileNotFoundException;
	InputStream getNewInputStream(long startByteIndex) throws FileNotFoundException;
	
//	boolean canRead(); 
	boolean canWrite();

	boolean delete();

	long length();

	long lastModified();
	boolean setLastModified(long time);

	boolean setReadOnly();
	boolean setReadOnly(boolean isReadOnly);

//	int compareTo(Object o);
//	boolean	equals(Object obj); 
//	int	hashCode() ;

//	boolean renameTo(File dest);
	boolean createNewFile();
	boolean exists();
}

