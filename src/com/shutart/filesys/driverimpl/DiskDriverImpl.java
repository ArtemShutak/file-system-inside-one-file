package com.shutart.filesys.driverimpl;

import java.util.HashMap;
import java.util.Map;

import com.shutart.filesys.domain.IBytesOfFile;
import com.shutart.filesys.domain.IDisk;
import com.shutart.filesys.domain.IDiskDriver;


public class DiskDriverImpl implements IDiskDriver {
	
	private final DiskIndex index;
	private final IDisk disk;
	private final Map<Integer,BytesOfFile> fileId2Bytes = new HashMap<Integer, BytesOfFile>();
	private final Map<Integer, Integer> fileId2NumberOfBytesUsers = new HashMap<Integer, Integer>();

	private DiskDriverImpl(IDisk disk) {
		this.disk = disk;
		index = new DiskIndex(disk);
//		this.formatDisk();
	}
	
	@Override
	public int initNewFileAndGetFileId() {
		return index.getFreeFileIdAndTake();
	}
	
	@Override
	public void deleteFile(int fileId) {
		IBytesOfFile bytesOfFile = getBytesOfFile(fileId);
		bytesOfFile.clear();
		releaseBytesOfFile(fileId);
		Integer numOfBytesUsers = fileId2NumberOfBytesUsers.get(fileId);
		if (numOfBytesUsers != null && numOfBytesUsers != 0){
			
		}
		int firstPageNum = index.getPageNumBy(fileId);
		index.setPageAsFree(firstPageNum);
		index.setFileIdAsFree(fileId);
	}
	
	@Override
	public void releaseBytesOfFile(int fileId) {
		int numberOfUsers = fileId2NumberOfBytesUsers.get(fileId);
		numberOfUsers--;
		if (numberOfUsers == 0){
			fileId2NumberOfBytesUsers.remove(fileId);
			fileId2Bytes.remove(fileId);
		}else{
			fileId2NumberOfBytesUsers.put(fileId, numberOfUsers);
		}
	}
	
	@Override
	public IBytesOfFile getBytesOfFile(int fileId) {
		BytesOfFile rez = fileId2Bytes.get(fileId);
		if (rez == null){
			int indexOfFirstFilePage = index.getPageNumBy(fileId);
			rez = new BytesOfFile(disk, index, indexOfFirstFilePage);
			fileId2Bytes.put(fileId, rez);
			fileId2NumberOfBytesUsers.put(fileId, 1);
		}else{
			int numOfUsers = fileId2NumberOfBytesUsers.get(fileId);
			numOfUsers++;
			fileId2NumberOfBytesUsers.put(fileId, numOfUsers);
		}
		return rez;
	}
	
	@Override
	public void formatDisk() {
		index.format();
	}

	
	private static final Map<IDisk, DiskDriverImpl> disk2Driver = new HashMap<IDisk, DiskDriverImpl>();
	public static IDiskDriver getDriver4Disk(IDisk disk) {
		DiskDriverImpl driver = disk2Driver.get(disk);
		if (driver == null){
			driver = new DiskDriverImpl(disk);
			disk2Driver.put(disk, driver);
		}
		return driver;
	}

	public static void releaseDisk(IDisk disk) {
		DiskDriverImpl driver = disk2Driver.remove(disk);
		driver.releaseBytesOfFile4All();
	}

	private void releaseBytesOfFile4All() {
//		for (Integer fileId : fileId2Bytes.keySet()) {
//			releaseBytesOfFile(fileId);
//		}
		fileId2Bytes.clear();
		fileId2NumberOfBytesUsers.clear();
		//TODO
		index.releaseAllCounters();
	}
}
