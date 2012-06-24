package com.shutart.filesys.domain;

import java.io.InputStream;
import java.io.OutputStream;

public interface IFile {

	boolean isEmpty();

	String getName();

	OutputStream getOutputStream();

	InputStream getInputStream();

}
