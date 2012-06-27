package com.shutart.onefilefs.test.util;

import static org.junit.Assert.*;

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
	public void simpleSetAndGetTest() {
		int pageNumber = 0;
		byte[] pageContent = {2, 37};
		disk.setPageContent(pageNumber, pageContent);
		assertArrayEquals(pageContent, disk.getPageContent(pageNumber));
	}
	
	@Test
	public void simpleSetAndGetTest2() {
		int startPageNumber = 6;
		int endPageNumber = 9;
		byte[] pageContent = {2, 37, 8, 56, 30, 87, 47, 93};
		disk.setPagesContent(startPageNumber, endPageNumber, pageContent);
		assertArrayEquals(pageContent, disk.getPagesContent(startPageNumber, endPageNumber));
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
		disk.getPagesContent(0, NUMBER_OF_PAGES);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4GetTest2() {
		disk.getPagesContent(NUMBER_OF_PAGES-2, NUMBER_OF_PAGES+10);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4GetTest3() {
		disk.getPagesContent(-1, NUMBER_OF_PAGES-1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void throwException4GetTest4() {
		disk.getPagesContent(NUMBER_OF_PAGES-1, 0);
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
		byte[] b = {};
		disk.setPagesContent(0, NUMBER_OF_PAGES, b );
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SetTest2() {
		byte[] b = {};
		disk.setPagesContent(NUMBER_OF_PAGES-2, NUMBER_OF_PAGES+10, b );
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void throwException4SetTest3() {
		byte[] b = {};
		disk.setPagesContent(-1, NUMBER_OF_PAGES-1, b );
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void throwException4SetTest4() {
		byte[] b = {};
		disk.setPagesContent(NUMBER_OF_PAGES-1, 0, b );
	}
	
	@Test(expected = NullPointerException.class)
	public void throwException4SimpleSetTest5() {
		disk.setPageContent(NUMBER_OF_PAGES-1, null);
	}
	
	@Test(expected = NullPointerException.class)
	public void throwException4SimpleSetTest6() {
		disk.setPagesContent(0, NUMBER_OF_PAGES-1, null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void throwException4SimpleSetTest7() {
		byte[] bytes = {1,2,3};//error length
		disk.setPageContent(0, bytes );
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void throwException4SimpleSetTest8() {
		byte[] bytes = {1,2,3};//error length
		disk.setPagesContent(0, 1, bytes);
	}

}
