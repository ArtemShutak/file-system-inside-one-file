package com.shutart.onefilefs.test.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.shutart.filesys.domain.BytesOfFile;
import com.shutart.filesys.domain.IDisk;
import com.shutart.filesys.domain.IDiskDriver;

public abstract class AbstractDiskDriverSpec {
	
	private static final int NUMBER_OF_PAGES = 10;
	private static final int PAGE_SIZE = 16;
	private static final int SIZE_OF_FILE_ID_ARRAY_IN_PAGES;
	private static final int SIZE_OF_INDEX_IN_PAGES;

	private static final byte[] FORMATTED_DISK_INDEX_PATTERN = {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1,  0,  0,  0,  0,  0,  0,  0,  0,
		 0,  0,  0,  0,  1,  1,  1,  1,  1,  1
	};
	
	private int indexOfFirstFreePageInPatternArray() {
		return PAGE_SIZE*SIZE_OF_FILE_ID_ARRAY_IN_PAGES + firstFreePageNum();
	}
	
	private int firstFreePageNum() {
		return SIZE_OF_INDEX_IN_PAGES;
	}

	static{
		int mod = NUMBER_OF_PAGES % (PAGE_SIZE/4);
		SIZE_OF_FILE_ID_ARRAY_IN_PAGES = (NUMBER_OF_PAGES/(PAGE_SIZE/4)) + (mod == 0 ? 0 : 1);
		int mod2 = NUMBER_OF_PAGES % PAGE_SIZE;
		SIZE_OF_INDEX_IN_PAGES = SIZE_OF_FILE_ID_ARRAY_IN_PAGES + (NUMBER_OF_PAGES/PAGE_SIZE) +  (mod2  == 0 ? 0 : 1);
	}

	private IDiskDriver dd;
	private IDisk disk;

	protected abstract IDisk getDisk(int numberOfPages, int pageSize) ;
	protected abstract IDiskDriver getDiskDriver(IDisk disk) ;
	
	@Before
	public void setUp() throws Exception {
		disk = getDisk(NUMBER_OF_PAGES, PAGE_SIZE);
		dd = getDiskDriver(disk);
		dd.formatDisk();
	}


	@After
	public void tearDown() throws Exception {
		dd.formatDisk();
		diskDriverReleaseDisk(disk);
	}

	protected abstract void diskDriverReleaseDisk(IDisk disk) ;
	@Test
	public void testIndexStateAfterFormatDisk() {
		byte[] bytesOfIndex = readBytesOfDiskIndex();
		assertArrayEquals(FORMATTED_DISK_INDEX_PATTERN, bytesOfIndex);
	}

	private byte[] readBytesOfDiskIndex() {
		byte[] bytesOfIndex = new byte[FORMATTED_DISK_INDEX_PATTERN.length];
		int pageNumber = 0;
		int innerIndex = 0;
		for (int i = 0; i < bytesOfIndex.length; i++) {
			bytesOfIndex[i] = disk.getByte(pageNumber, innerIndex);
			innerIndex++;
			if (innerIndex >= PAGE_SIZE ){
				pageNumber++;
				innerIndex = 0;
			}
		}
		return bytesOfIndex;
	}
	
	@Test
	public void testInitNewFileMethod() {
		int newFileId = dd.initNewFileAndGetFileId();
		assertEquals(0, newFileId);
		
		byte[] copyOfIndex = copyOfIndexPattern();
		copyOfIndex[0] = 0;
		copyOfIndex[1] = 0;
		copyOfIndex[2] = 0;
		copyOfIndex[3] = 4;
		copyOfIndex[PAGE_SIZE*3 + 4] = 0;
		
		assertArrayEquals(copyOfIndex, readBytesOfDiskIndex());
	}
	
	@Test
	public void testInitNewFileMethod4times() {
		initNewFileAndDeleteSeveralTimes(4);
	}
	
	private void initNewFileAndDeleteSeveralTimes(int times) {
		final byte[] copyOfIndex = copyOfIndexPattern();
		for (int i = 0, freePageNum = firstFreePageNum(); i < times; i++, freePageNum++) {
			int fileId = dd.initNewFileAndGetFileId();
			assertEquals(i, fileId);
			copyOfIndex[i*4] = 0;
			copyOfIndex[i*4 + 1] = 0;
			copyOfIndex[i*4 + 2] = 0;
			copyOfIndex[i*4 + 3] = (byte) freePageNum;
			copyOfIndex[indexOfFirstFreePageInPatternArray() + i] = 0;
			assertArrayEquals(copyOfIndex, readBytesOfDiskIndex());
			dd.deleteFile(fileId);		
			
			copyOfIndex[i*4] =     -1;
			copyOfIndex[i*4 + 1] = -1;
			copyOfIndex[i*4 + 2] = -1;
			copyOfIndex[i*4 + 3] = -1;
			copyOfIndex[indexOfFirstFreePageInPatternArray() + i] = 1;
			assertArrayEquals(copyOfIndex, readBytesOfDiskIndex());
		}
	}
	
	private byte[] copyOfIndexPattern() {
		return Arrays.copyOf(FORMATTED_DISK_INDEX_PATTERN, FORMATTED_DISK_INDEX_PATTERN.length);
	}

	@Test
	public void diskDriverShouldThrowExceptionIfDiskDoNotHaveFreePage() {
		int numOfFreePages = numOfFreePages();
		for (int i = 0; i < numOfFreePages ; i++) {
			dd.initNewFileAndGetFileId();			
		}
		try{
			dd.initNewFileAndGetFileId();
			fail();
		}catch (IllegalStateException e) {
		}
	}

	private int numOfFreePages() {
		return NUMBER_OF_PAGES - SIZE_OF_INDEX_IN_PAGES;
	}

	@Test
	public void testDeleteFileMethod() {
		int fileId = dd.initNewFileAndGetFileId();
		dd.deleteFile(fileId );
		assertArrayEquals(FORMATTED_DISK_INDEX_PATTERN, readBytesOfDiskIndex());
	}
	
	@Test
	public void diskDriverShouldReturnFileIdsByRound() {
		initNewFileAndDeleteSeveralTimes(numOfFreePages());
		for (int i = 0; i < NUMBER_OF_PAGES - numOfFreePages(); i++){
			int fileId = dd.initNewFileAndGetFileId();
			dd.deleteFile(fileId);
		}
		int fileId = dd.initNewFileAndGetFileId();
		assertEquals(0, fileId);
	}
	
	@Test
	public void byteOfFileShouldHaveZeroSize() {
		int fileId = dd.initNewFileAndGetFileId();
		BytesOfFile bytesOfFile = dd.getBytesOfFile(fileId);
		assertEquals(0, bytesOfFile.size());
		dd.releaseBytesOfFile(fileId);
	}
	
	@Test
	public void simpleTestAddAndGetMethodsOfBytesOfFile() {
		int fileId = dd.initNewFileAndGetFileId();
		BytesOfFile bytesOfFile = dd.getBytesOfFile(fileId);
		byte b = (byte) 239;
		bytesOfFile.add(b);
		assertEquals(1, bytesOfFile.size());
		assertEquals(b, bytesOfFile.get(0));
		dd.releaseBytesOfFile(fileId);
	}
	
	@Test (expected = IndexOutOfBoundsException.class)
	public void getMethodShouldThrowExceptionIfIndexMoreSize() {
		int fileId = dd.initNewFileAndGetFileId();
		BytesOfFile bytesOfFile = dd.getBytesOfFile(fileId);
		byte b = 17;
		bytesOfFile.add(b);
		dd.releaseBytesOfFile(fileId);
		
		bytesOfFile.get(1);
	}
	
	@Test (expected = IndexOutOfBoundsException.class)
	public void getMethodShouldThrowExceptionIfIndexLessZero() {
		int fileId = dd.initNewFileAndGetFileId();
		BytesOfFile bytesOfFile = dd.getBytesOfFile(fileId);
		byte b = 13;
		bytesOfFile.add(b);
		dd.releaseBytesOfFile(fileId);
		
		bytesOfFile.get(-1);
	}

	@Test
	public void testAddAndGetMethodsOfBytesOfFile() {
		int fileId = dd.initNewFileAndGetFileId();
		BytesOfFile bytesOfFile = dd.getBytesOfFile(fileId);
		for (int i = 0; i < PAGE_SIZE; i++) {
			bytesOfFile.add((byte) i);
		}
		for (int i = 0; i < PAGE_SIZE; i++) {
			assertEquals(i, bytesOfFile.get(i));
		}
		dd.releaseBytesOfFile(fileId);
	}
	
	@Test
	public void testSetMethodOfBytesOfFile() {
		int fileId = dd.initNewFileAndGetFileId();
		BytesOfFile bytesOfFile = dd.getBytesOfFile(fileId);
		for (int i = 0; i < PAGE_SIZE; i++) {
			bytesOfFile.add((byte) i);
		}
		int index = PAGE_SIZE - 2;
		byte b = 38;
		bytesOfFile.set(index, b);
		assertEquals(b, bytesOfFile.get(index));
		dd.releaseBytesOfFile(fileId);
	}
	
	@Test
	public void testClearMethodOfBytesOfFile() {
		int fileId = dd.initNewFileAndGetFileId();
		BytesOfFile bytesOfFile = dd.getBytesOfFile(fileId);
		for (int i = 0; i < PAGE_SIZE; i++) {
			bytesOfFile.add((byte) i);
		}
		bytesOfFile.clear();
		
		byte[] indexArray = copyOfIndexPattern();
		indexArray[0] = 0;
		indexArray[1] = 0;
		indexArray[2] = 0;
		indexArray[3] = (byte) firstFreePageNum();
		indexArray[indexOfFirstFreePageInPatternArray()] = 0;

		assertArrayEquals(indexArray, readBytesOfDiskIndex());
		dd.releaseBytesOfFile(fileId);
	}
	
}
