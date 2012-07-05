package com.shutart.filesys.domain;



public interface IDiskDriver {

	int initNewFileAndGetFileId();
	boolean deleteFile(int fileId);

	IBytesOfFile getBytesOfFile(int fileId);

	void formatDisk();
	void releaseBytesOfFile(int fileId);

}
