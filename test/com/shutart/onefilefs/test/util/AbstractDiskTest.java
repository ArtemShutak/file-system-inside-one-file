package com.shutart.onefilefs.test.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.shutart.filesys.domain.IDisk;

public abstract class AbstractDiskTest {
	
	private static final int NUMBER_OF_PAGES = 10;
	private static final int PAGE_SIZE = 2;
	private IDisk disk;
	
	public abstract IDisk getNewDisk(int numberOfPages, int pageSize);

	@Before
	public void setUp() throws Exception {
		disk = getNewDisk(NUMBER_OF_PAGES, PAGE_SIZE);
	}

	@After
	public void tearDown() throws Exception {
		disk.clear();
		disk.delete();
	}

	@Test
	public void numberOfPagesTest() {
		assertEquals(disk.getNumberOfPages(), NUMBER_OF_PAGES);
	}
	
	@Test
	public void pageSizeTest() {
		assertEquals(disk.getPageSize(), PAGE_SIZE);
	}
	
	@Test
	public void sizeInBytesTest() {
		assertEquals(disk.getSizeInBytes(), PAGE_SIZE*NUMBER_OF_PAGES);
	}
	
	@Test
	public void simpleSetAndGetTest() {
		int pageNumber = 0;
		byte[] pageContent = {2, 37};
		disk.setPageContent(pageNumber, pageContent);
		assertArrayEquals(pageContent, disk.getPageContent(pageNumber));
	}
	
	@Test
	public void simpleSetAndGetTest2() {
		int pageNum = 6;
		byte[] pageContent = {2};
		disk.setPageContent(pageNum, 1, pageContent);
		assertArrayEquals(pageContent, disk.getPageContent(pageNum, 1, 2));
	}
	
	@Test
	public void simpleSetAndGetTest3() {
		int pageNumber = 9;
		int innerIndex = 1;
		byte b = 123;
		disk.setByte(pageNumber, innerIndex, b);
		assertEquals(disk.getByte(pageNumber, innerIndex), b);
	}
	
	//get exceptions
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SimpleGetTest1() {
		disk.getPageContent(NUMBER_OF_PAGES);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SimpleGetTest2() {
		disk.getPageContent(NUMBER_OF_PAGES+10);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SimpleGetTest3() {
		disk.getPageContent(-1);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4GetTest1() {
		disk.getPageContent(0, 1, PAGE_SIZE+1);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4GetTest2() {
		disk.getPageContent(NUMBER_OF_PAGES-2, -1, PAGE_SIZE);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4GetTest3() {
		disk.getPageContent(-1, 0, 1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void throwException4GetTest4() {
		disk.getPageContent(NUMBER_OF_PAGES-1, 2, 0);
	}
	
	//set exceptions
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SimpleSetTest1() {
		byte[] bytes = {};
		disk.setPageContent(NUMBER_OF_PAGES, bytes );
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SimpleSetTest2() {
		byte[] bytes = {};
		disk.setPageContent(NUMBER_OF_PAGES+10, bytes );
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SimpleSetTest3() {
		byte[] bytes = {};
		disk.setPageContent(-1, bytes );
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SetTest1() {
		byte[] b = {1,2};
		disk.setPageContent(0, 1, b );
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SetTest2() {
		byte[] b = {};
		disk.setPageContent(NUMBER_OF_PAGES+1, 1, b );
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SetTest3() {
		byte[] b = {1};
		disk.setPageContent(-1, 1, b );
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void throwException4SetTest4() {
		byte[] b = {};
		disk.setPageContent(NUMBER_OF_PAGES-1, 0, b );
	}
	
	@Test(expected = NullPointerException.class)
	public void throwException4SimpleSetTest5() {
		disk.setPageContent(NUMBER_OF_PAGES-1, null);
	}
	
	@Test(expected = NullPointerException.class)
	public void throwException4SimpleSetTest6() {
		disk.setPageContent(0, NUMBER_OF_PAGES-1, null);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SimpleSetTest7() {
		byte[] bytes = {1,2,3};//error length
		disk.setPageContent(0, bytes );
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SimpleSetTest8() {
		byte[] bytes = {1,2,3};//error length
		disk.setPageContent(0, 1, bytes);
	}
	
	public void setMethodShouldCopyBytesOnDisk_1() {
		byte[] bytes = {1,2};
		byte[] bytesCopy = Arrays.copyOf(bytes, bytes.length);
		int pageNum = 1;
		disk.setPageContent(pageNum , bytes);
		
		bytes[0] = 13;
		
		assertArrayEquals(disk.getPageContent(pageNum), bytesCopy);
	}
	
	public void setMethodShouldCopyBytesOnDisk_2() {
		byte[] bytes = {3};
		byte[] bytesCopy = Arrays.copyOf(bytes, bytes.length);
		int pageNum = 1;
		int from = 2;
		disk.setPageContent(pageNum, from, bytes);
		
		bytes[0] = 13;
		
		assertArrayEquals(disk.getPageContent(pageNum, from, PAGE_SIZE), bytesCopy);
	}
	
	public void getMethodShouldCopyBytesFromDisk_1() {
		byte[] bytes = {1,2};
		int pageNum = 1;
		disk.setPageContent(pageNum , bytes);
		
		bytes = disk.getPageContent(pageNum);
		byte[] bytesCopy = Arrays.copyOf(bytes, bytes.length);
		
		bytes[1] = 7;
		
		assertArrayEquals(disk.getPageContent(pageNum), bytesCopy);
	}
	
	public void getMethodShouldCopyBytesFromDisk_2() {
		byte[] bytes = {7};
		int pageNum = 2;
		int from = 1;
		disk.setPageContent(pageNum, from, bytes);
		
		bytes = disk.getPageContent(pageNum, from, PAGE_SIZE);
		byte[] bytesCopy = Arrays.copyOf(bytes, bytes.length);
		
		bytes[0] = 36;
		
		assertArrayEquals(disk.getPageContent(pageNum, from, PAGE_SIZE), bytesCopy);
	}

}
