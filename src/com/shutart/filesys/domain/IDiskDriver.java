package com.shutart.filesys.domain;


public interface IDiskDriver {

	int initNewFileAndGetFileId();
	void deleteFile(int fileId);

	BytesOfFile getBytesOfFile(int fileId);

	void formatDisk();
	void releaseBytesOfFile(int fileId);

}
