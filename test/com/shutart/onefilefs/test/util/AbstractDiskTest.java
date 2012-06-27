package com.shutart.onefilefs.test.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.junit.rules.ExpectedException;

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

}
