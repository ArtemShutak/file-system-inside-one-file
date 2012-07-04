package com.shutart.filesys.sample;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.shutart.filesys.domain.FileImpl;
import com.shutart.filesys.domain.IFile;
import com.shutart.filesys.domain.IFileSystem;

class SampleRead {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println(SampleRead.class.getSimpleName() + " START");
		IFileSystem fs = SampleWrite.getFileSystem(false);
		IFile file = new FileImpl(SampleWrite.SAMPLE_FILE_NAME, fs);
		
		long startTime = System.currentTimeMillis();
		DataInputStream in = new DataInputStream(new BufferedInputStream(file.getNewInputStream()));
		for (int i = 0; i < SampleWrite.NUMBERS; i++) {
			String st = in.readUTF();
			System.out.println(st);
			if (! st.equals(SampleWrite.sampleStringNum(i)) )
					throw new IllegalStateException();
		}
		in.close();
		System.out.println("Time:" + (System.currentTimeMillis()-startTime)/1000.0);
		System.out.println("FINISH");
	}
	
}
