package com.shutart.onefilefs.test;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.shutart.filesys.domain.IFile;
import com.shutart.filesys.domain.IFileSystem;

public class FIleSystemTest {
	
	private IFileSystem fileSys;
	private OutputStream out;
	private InputStream in;

	@Before
	public void setUp() throws Exception {
		fileSys = new MemoryFileSystem();
	}

	@After
	public void tearDown() throws Exception {
		fileSys.clear();
		if (out != null) 
			out.close();
		if (in != null)
			in.close();
	}
	

	//tests for fileSystem
	@Test
	public void fsGetEmptyFileTest() {
		IFile file = fileSys.getFileByName("simpleTestFile");
		assertTrue(file.isEmpty());
	}

	@Test
	public void fsClearTest(){
		fileSys.getFileByName("simpleTestFile");
		assertFalse(fileSys.isEmpty());
		fileSys.clear();
		assertTrue(fileSys.isEmpty());
	}
	
	@Test
	public void fsDeleteFileByNameTest() {
		IFile file = fileSys.getFileByName("simpleTestFile");
		fileSys.deleteFile(file.getName());
		assertTrue(fileSys.isEmpty());
	}
	
	@Test
	public void fsDeleteFileTest() {
		IFile file = fileSys.getFileByName("simpleTestFile");
		fileSys.deleteFile(file);
		assertTrue(fileSys.isEmpty());
	}
	
	@Test
	public void fsFalseContainsFileTest() {
		assertFalse(fileSys.containsFile("simpleTestFile"));
	}
	
	@Test
	public void fsTrueContainsFileTest() {
		fileSys.getFileByName("simpleTestFile");
		assertTrue(fileSys.containsFile("simpleTestFile"));
	}
	
	//test for file
	@Test
	public void getFileNameTest() {
		String fileName = "simpleTestFile";
		IFile file = fileSys.getFileByName(fileName);
		assertEquals(fileName, file.getName());
	}
	
	@Test
	public void getNotNullOutStreamTest() {
		IFile file = fileSys.getFileByName("simpleTestFile");
		out = file.getOutputStream();
		assertNotNull(out);
	}

	@Test
	public void getNotNullInStreamTest() {
		IFile file = fileSys.getFileByName("simpleTestFile");
		in = file.getInputStream();
		assertNotNull(in);
	}
	
	@Test
	public void readWriteTest() {
		IFile file = fileSys.getFileByName("simpleTestFile");
		
		out = file.getOutputStream();
		PrintWriter pw = new PrintWriter(new BufferedOutputStream(out));
		String testStr1 = "hello";
		String testStr2 = "people!";
		pw.println(testStr1);
		pw.println(testStr2);
		pw.flush();
		assertFalse(file.isEmpty());
		
		in = file.getInputStream();
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		try {
			assertEquals(r.readLine(), testStr1);
			assertEquals(r.readLine(), testStr2);
			assertNull(r.readLine());
		} catch (IOException e) {
			fail();
		}
	}
}
