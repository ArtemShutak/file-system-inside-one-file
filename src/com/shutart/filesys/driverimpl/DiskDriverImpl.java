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
	
	private final Object monitor = new Object();

	private DiskDriverImpl(IDisk disk) {
		this.disk = disk;
		index = new DiskIndex(disk);
//		this.formatDisk();
	}
	
	@Override
	public int initNewFileAndGetFileId() {
		synchronized (monitor) {
			return index.getFreeFileIdAndTake();			
		}
	}
	
	@Override
	public boolean deleteFile(int fileId) {
		synchronized (monitor) {
			Integer countOfFileUsers = fileId2NumberOfBytesUsers.get(fileId);
			if (countOfFileUsers != null && countOfFileUsers != 0)
				return false;
			IBytesOfFile bytesOfFile = getBytesOfFile(fileId);
			bytesOfFile.clear();
			releaseBytesOfFile(fileId);
			countOfFileUsers = fileId2NumberOfBytesUsers.get(fileId);
			if (countOfFileUsers != null && countOfFileUsers != 0){
				throw new IllegalStateException();
			}
			int firstPageNum = index.getPageNumBy(fileId);
			index.setPageAsFree(firstPageNum);
			index.setFileIdAsFree(fileId);	
			return true;
		}
	}
	
	@Override
	public void releaseBytesOfFile(int fileId) {
		synchronized (monitor) {
			int numberOfUsers = fileId2NumberOfBytesUsers.get(fileId);
			numberOfUsers--;
			if (numberOfUsers == 0){
				fileId2NumberOfBytesUsers.remove(fileId);
				fileId2Bytes.remove(fileId);
			}else{
				fileId2NumberOfBytesUsers.put(fileId, numberOfUsers);
			}
		}
	}
	
	@Override
	public IBytesOfFile getBytesOfFile(int fileId) {
		synchronized (monitor) {
			BytesOfFile rez = fileId2Bytes.get(fileId);
			if (rez == null){
				int indexOfFirstFilePage = index.getPageNumBy(fileId);
				rez = new BytesOfFile(disk, index, indexOfFirstFilePage, monitor);
				fileId2Bytes.put(fileId, rez);
				fileId2NumberOfBytesUsers.put(fileId, 1);
			}else{
				int numOfUsers = fileId2NumberOfBytesUsers.get(fileId);
				numOfUsers++;
				fileId2NumberOfBytesUsers.put(fileId, numOfUsers);
			}
			return rez;
		}
	}
	
	@Override
	public void formatDisk() {
		synchronized (monitor) {
			index.format();			
		}
	}

	
	private static final Map<IDisk, DiskDriverImpl> disk2Driver = new HashMap<IDisk, DiskDriverImpl>();
	public static IDiskDriver getDriver4Disk(IDisk disk) {
		synchronized (disk2Driver) {
			DiskDriverImpl driver = disk2Driver.get(disk);
			if (driver == null){
				driver = new DiskDriverImpl(disk);
				disk2Driver.put(disk, driver);
			}
			return driver;			
		}
	}

	public static void releaseDisk(IDisk disk) {
		synchronized (disk2Driver) {
			DiskDriverImpl driver = disk2Driver.remove(disk);
			driver.releaseBytesOfFile4All();			
		}
	}

	private void releaseBytesOfFile4All() {
//		for (Integer fileId : fileId2Bytes.keySet()) {
//			releaseBytesOfFile(fileId);
//		}
		synchronized (monitor) {
			fileId2Bytes.clear();
			fileId2NumberOfBytesUsers.clear();
			//TODO
			index.releaseAllCounters();
		}
	}
}
