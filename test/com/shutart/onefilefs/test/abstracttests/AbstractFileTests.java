package com.shutart.onefilefs.test.abstracttests;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.shutart.filesys.domain.FSConstans;
import com.shutart.filesys.domain.IFile;

public abstract class AbstractFileTests {

	private static final String PATH_NAME = "/testPath/";
	private static final String SIMPLE_NAME = "simpleTestFile";
	private static final String FILE_NAME = PATH_NAME+SIMPLE_NAME;
	private IFile file;
	private OutputStream out;
	private InputStream in;
	
	@Before
	public void setUp() throws Exception {
		file = newFile(FILE_NAME);
		if (file.exists()){
			file.delete();
			assertFalse(file.delete());
		}
	}
	
	protected abstract IFile newFile(String name);

	protected abstract void diskDriverReleaseDisk() ;

	@After
	public void tearDown() throws Exception {
		file.delete();
		if (out != null)
			out.close();
		if (in != null)
			in.close();
		if (file.exists()){
			assertTrue(file.delete());
			Thread.sleep(1000);
//TODO
			assertFalse(file.exists());
		}
		diskDriverReleaseDisk();
	}

	@Test
	public void fileNotExistTest() {
		assertFalse(file.exists());
		assertFalse(file.isWritable());
		assertTrue(file.length()==0);
		assertTrue(file.lastModified() == FSConstans.START_LAST_MODIF_VAL);
	}
	
	@Test
	public void createFileTest() {
		assertFalse(file.exists());
		long startTime = System.currentTimeMillis();
		assertTrue(file.createNewFile());
		assertTrue(startTime <= file.lastModified());
		assertTrue(file.exists());
		assertTrue(file.isWritable());
		assertTrue(file.length()==0);
	}

	//TODO
//	private void printDisk() {
//		for (int i = 0; i < disk.getNumberOfPages(); i++) {
//			System.out.println(Arrays.toString(disk.getPageContent(i)));
//		}
//	}

	@Test
	public void fileCreateAndDeleteTest() {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());
//		
		try {
			out = file.getNewOutputStream();
		} catch (FileNotFoundException e1) {
			fail();
		}
		try {
			out.write(10);
			out.flush();
			out.close();
		} catch (IOException e) {
			fail();
		}
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue(file.delete());
		
		assertFalse(file.exists());
		assertFalse(file.isWritable());
		assertTrue(file.length()==0);
		assertTrue(file.lastModified() == FSConstans.START_LAST_MODIF_VAL);
	}
	
	@Test
	public void fileCreateAndDeleteTest2() {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());
//		
		try {
			out = file.getNewOutputStream(true);
		} catch (FileNotFoundException e1) {
			fail();
		}
		try {
			out.write(10);
			out.flush();
			out.close();
		} catch (IOException e) {
			fail();
		}
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			fail();
		}
		assertTrue(file.delete());
		
		assertFalse(file.exists());
		assertFalse(file.isWritable());
		assertTrue(file.length()==0);
		assertTrue(file.lastModified() == FSConstans.START_LAST_MODIF_VAL);
	}
	
	@Test
	public void setReadOnlyFileTest1() {
		assertFalse(file.setReadOnly());
	}
	
	@Test
	public void setReadOnlyFileTest2() {
		assertTrue(file.createNewFile());
		assertTrue(file.setReadOnly());
		assertFalse(file.isWritable());
	}
	
	
	@Test
	public void doubleReadOnlyFileTest() {
		assertTrue(file.createNewFile());
		assertTrue(file.setWritable(false));
		assertFalse(file.isWritable());
		assertTrue(file.setWritable(true));
		assertTrue(file.isWritable());
	}
	
	@Test
	public void doubleReadOnlyFileTest2() {
		assertTrue(file.createNewFile());
		assertTrue(file.setWritable(false));
		assertFalse(file.isWritable());
		assertTrue(file.setWritable(false));
		assertFalse(file.isWritable());
	}

	
	@Test
	public void badSetLastModifTimeFileTest() {
		long time = 100500;
		assertFalse(file.setLastModified(time));
		assertEquals(FSConstans.START_LAST_MODIF_VAL, file.lastModified());
	}
	
	@Test
	public void setLastModifTimeFileTest() {
		assertTrue(file.createNewFile());
		long time = 100500;
		assertTrue(file.setLastModified(time));
		assertEquals(time, file.lastModified());
	}
	
	@Test
	public void noChangeLastModifTimeFileTest1() {
		assertTrue(file.createNewFile());
		long beforTime = file.lastModified();
		file.setReadOnly();
		assertEquals(file.lastModified(), beforTime);
	}
	
	@Test
	public void noChangeLastModifTimeFileTest2() {
		assertTrue(file.createNewFile());
		long beforTime = file.lastModified();
		file.setWritable(true);
		assertEquals(file.lastModified(), beforTime);
	}
	
	@Test
	public void noChangeLastModifTimeFileTest3() {
		assertTrue(file.createNewFile());
		long beforTime = file.lastModified();
		file.setWritable(false);
		assertEquals(file.lastModified(), beforTime);
	}

	
	@Test
	public void changeLastModifTimeFileTest() {
		assertTrue(file.createNewFile());
		long beforTime = file.lastModified();
		try {
			out = file.getNewOutputStream();
		} catch (FileNotFoundException e1) {
			fail();
		}
		try {
			out.write(new byte[]{1,2,3,4,5});
			out.flush();
		} catch (IOException e) {
			fail();
		}
		assertTrue(beforTime <= file.lastModified());
	}
	
	//get names
	@Test
	public void namesOfFileTest() {
		assertEquals(file.getName(), FILE_NAME);
		assertEquals(file.getPath(), PATH_NAME);
		assertEquals(file.getSimpleName(), SIMPLE_NAME);
	}

	//streams
	@Test
	public void getNotNullOutStreamTest() {
		try {
			out = file.getNewOutputStream();
		} catch (FileNotFoundException e) {
			fail();
		}
		assertNotNull(out);
	}
	
	@Test
	public void throwExcept4OutStreamTest() {
		assertTrue(file.createNewFile());
		assertTrue(file.setReadOnly());
		try {
			out = file.getNewOutputStream();
		} catch (FileNotFoundException e1) {
			return;
		}
		fail();
	}

	@Test
	public void throwExcept4InStreamTest() {
		try {
			file.getNewInputStream();
		} catch (FileNotFoundException e) {
			return;
		}
		fail();
	}

	@Test
	public void readWriteTest() {
		try {
			out = file.getNewOutputStream();
		} catch (FileNotFoundException e1) {
			fail();
		}
		PrintWriter pw = new PrintWriter(new BufferedOutputStream(out));
		String testStr1 = "hello";
		String testStr2 = "people!";
		pw.println(testStr1);
		pw.println(testStr2);
		pw.flush();
		pw.close();
		assertTrue(file.length() > 0);

		try {
			in = file.getNewInputStream();
		} catch (FileNotFoundException e1) {
			fail();
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		try {
			assertEquals(r.readLine(), testStr1);
			assertEquals(r.readLine(), testStr2);
			assertNull(r.readLine());
		} catch (IOException e) {
			fail();
		}
	}
	
	@Test
	public void testAppendedOutputStream() {
		try {
			out = file.getNewOutputStream();
		} catch (FileNotFoundException e1) {
			fail();
		}
		PrintWriter pw = new PrintWriter(new BufferedOutputStream(out));
		String testStr1 = "hello";
		String testStr2 = "people!";
		pw.println(testStr1);
		pw.println(testStr2);
		pw.flush();
		pw.close();
		assertTrue(file.length() > 0);

		try {
			out = file.getNewOutputStream(true);
		} catch (FileNotFoundException e1) {
			fail();
		}
		pw = new PrintWriter(new BufferedOutputStream(out));
		String testStr3 = "HELLO";
		String testStr4 = "PEOPLE!";
		pw.println(testStr3);
		pw.println(testStr4);
		pw.flush();
		pw.close();
		assertTrue(file.length() > 0);
		
		try {
			in = file.getNewInputStream();
		} catch (FileNotFoundException e1) {
			fail();
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		try {
			assertEquals(r.readLine(), testStr1);
			assertEquals(r.readLine(), testStr2);
			assertEquals(r.readLine(), testStr3);
			assertEquals(r.readLine(), testStr4);
			assertNull(r.readLine());
		} catch (IOException e) {
			fail();
		}
	}
	
	@Test
	public void testSimpleSetAndGetBytesMethods(){
		byte[] bytes = new byte[]{1,2,3};
		assertTrue(file.setBytes(0, bytes ));
		assertArrayEquals(bytes, file.getBytes(0, 3));		
	}
	
	@Test
	public void testDoubleSetAndOneGetBytesMethods(){
		byte[] bytes1 = new byte[]{1,2,3};
		assertTrue(file.setBytes(0, bytes1 ));
		byte[] bytes2 = new byte[]{4,5,6,7};
		assertTrue(file.setBytes(bytes1.length, bytes2 ));
		assertArrayEquals(new byte[]{1,2,3,4,5,6,7}, file.getBytes(0, 7));		
	}
	
	@Test
	public void getMethodShouldNotThrowAnExceptionIfLengthParameterMoreThanBounds(){
		byte[] bytes = new byte[]{1,2,3,4};
		assertTrue(file.setBytes(0, bytes));
		assertArrayEquals(new byte[]{3,4}, file.getBytes(2, 100));		
	}
	
	@Test
	public void getMethodShouldReturnEmptyArrayIfFromPositionMoreThanFileSize(){
		byte[] bytes = new byte[]{1,2,3,4};
		assertTrue(file.setBytes(0, bytes));
		assertArrayEquals(new byte[]{}, file.getBytes(10, 100));		
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void getMethodShouldThrowExceptionIfFromPositionLessThanZero(){
		byte[] bytes = new byte[]{1,2,3,4};
		assertTrue(file.setBytes(0, bytes));
		assertArrayEquals(new byte[]{}, file.getBytes(-1, 100));		
	}
	
	@Test
	public void setMethodShouldCreateFile(){
		assertTrue(file.setBytes(0, new byte[]{}));
		assertTrue(file.exists());
	}
	
	@Test
	public void testComplexSetMethod(){
		byte[] bytes = {7,8,9,10,11};
		assertTrue(file.setBytes(0, bytes, 2, 2));
		assertArrayEquals(new byte[]{9, 10}, file.getBytes(0, 2));
	}
	
	@Test
	public void complexSetMethodShouldNotThrowExceptionIfLengthTooBig(){
		byte[] bytes = {7,8,9,10,11};
		assertTrue(file.setBytes(0, bytes, 2, 100));
		assertArrayEquals(new byte[]{9, 10, 11}, file.getBytes(0, 100));
	}
}
