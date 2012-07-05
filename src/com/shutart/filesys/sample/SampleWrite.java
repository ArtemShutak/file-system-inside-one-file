package com.shutart.filesys.sample;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.shutart.filesys.domain.FSConstans;
import com.shutart.filesys.domain.FileImpl;
import com.shutart.filesys.domain.IDisk;
import com.shutart.filesys.domain.IDiskDriver;
import com.shutart.filesys.domain.IFile;
import com.shutart.filesys.domain.IFileSystem;
import com.shutart.filesys.driverimpl.DiskDriverImpl;
import com.shutart.filesys.filesysimpl.FileSysImpl;
import com.shutart.filesys.onefiledisk.OneFileDisk;

public class SampleWrite {

	static final String SAMPLE_FILE_NAME = "samplePath/sampleFile";
	static final int NUMBERS = 10;
	private static final String SAMPLE_STRING = "Some string for testing ";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println(SampleWrite.class.getSimpleName() + " START");
		IFileSystem fs = getFileSystem(true);
		IFile file = new FileImpl(SAMPLE_FILE_NAME, fs);
		long startTime = System.currentTimeMillis();		
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(file.getNewOutputStream()));
		for (int i = 0; i < NUMBERS; i++) {
			out.writeUTF(sampleStringNum(i));
		}
		out.flush();
		out.close();
		System.out.println("Time:" + (System.currentTimeMillis()-startTime)/1000.0);
		System.out.println("FINISH");
	}

	static String sampleStringNum(int i) {
		return SAMPLE_STRING + "_"+i+"\n";
	}

	static IFileSystem getFileSystem(boolean isNewDisk) {
		IDisk disk = OneFileDisk.getInstance("testPath/" + FSConstans.DISK_NAME,
				FSConstans.DISK_NUMBER_OF_PAGES, FSConstans.DISK_PAGE_SIZE);
		IDiskDriver diskDriver = DiskDriverImpl.getDriver4Disk(disk);
		return new FileSysImpl(diskDriver, isNewDisk );
	}

}
