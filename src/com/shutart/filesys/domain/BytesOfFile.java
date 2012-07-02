package com.shutart.filesys.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class BytesOfFile {

	private final IDisk disk;
	private final DiskIndex index;
	private List<Integer> numbersOfDiskPages = new ArrayList<Integer>();

	BytesOfFile(IDisk disk, DiskIndex index, int indexOfFirstFilePage) {
		if (indexOfFirstFilePage < 0)
			throw new IllegalArgumentException("indexOfFirstFilePage="
					+ indexOfFirstFilePage);
		this.disk = disk;
		this.index = index;
		numbersOfDiskPages.add(indexOfFirstFilePage);
		initNumbersOfDiskPages(indexOfFirstFilePage);
	}

	private void initNumbersOfDiskPages(int indexOfFirstFilePage) {
		final int size = size();
		final int from = disk.getPageSize()-4;
		final int to = disk.getPageSize();
		for (int i = disk.getPageSize()-8, curNumOfPage = 0; 
				i < size; 
				curNumOfPage++, i+=disk.getPageSize()-4){
			try {
				byte[] buf = disk.getPageContent(numbersOfDiskPages.get(curNumOfPage),from , to );
				DataInputStream r = new DataInputStream(new ByteArrayInputStream(buf));
				numbersOfDiskPages.add(r.readInt());
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public int size() {
		try {
			byte[] buf = disk.getPageContent(numbersOfDiskPages.get(0), 0, 4);
			DataInputStream r = new DataInputStream(new ByteArrayInputStream(
					buf));
			return r.readInt();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void setSize(int newSize) {
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream(4);
			DataOutputStream r = new DataOutputStream(byteArray );
			r.writeInt(newSize); 
			disk.setPageContent(numbersOfDiskPages.get(0), 0, byteArray.toByteArray());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void add(byte b) {
		int size = size();//absoluteIndex4NewElem
		int[] pageNumAndInnerIndex = calculatePageNumAndInnerIndex(size);
		int relativePageNum = pageNumAndInnerIndex[0];
		int innerIndex = pageNumAndInnerIndex[1];
		if (innerIndex != 0){
			disk.setByte(numbersOfDiskPages.get(relativePageNum), innerIndex, b);
		}else{
			assert relativePageNum == numbersOfDiskPages.size();
			int newPageNum = index.getFreePageNumAndTake();
			numbersOfDiskPages.add(newPageNum);
			setReferenceOnNewPage(numbersOfDiskPages.get(relativePageNum - 1), newPageNum);
			disk.setByte(numbersOfDiskPages.get(relativePageNum), innerIndex, b);
		}
		setSize(size + 1);
	}

//	private boolean thisPageCanHasThisInnerIndex(int pageNum, int innnerIndex) {
//		if (pageNum == 1)
//			return innnerIndex < disk.getPageSize() - 8;
//		return innnerIndex < disk.getPageSize() - 4;
//	}

	private void setReferenceOnNewPage(int page4ContainsRefer, int newPageNum) {
		try {
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream(4);
			DataOutputStream r = new DataOutputStream(byteArray);
			r.writeInt(newPageNum); 
			disk.setPageContent(page4ContainsRefer, disk.getPageSize()-4, byteArray.toByteArray());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void clear() {
		for (int lastElemIndex = numbersOfDiskPages.size()-1; 
				lastElemIndex > 0; lastElemIndex--) {
			int pageNum = numbersOfDiskPages.remove(lastElemIndex);
			index.setPageAsFree(pageNum);
		}
		setSize(0);
		assert numbersOfDiskPages.size() == 1;
	}


	public void set(int index, byte b) {
		if (index >= size())
			throw new IndexOutOfBoundsException("index:" + index + " >= "
					+ " size:" + size());
		int[] pageNumAndInnerIndex = calculateRealPageNumAndInnerIndex(index);
		disk.setByte(pageNumAndInnerIndex[0], pageNumAndInnerIndex[1], b);
	}

	public int get(int i) {
		if (i < 0 || i >= size())
			throw new IndexOutOfBoundsException("index:" + i + " >= "
					+ " size:" + size());
		int[] pageNumAndInnerIndex = calculateRealPageNumAndInnerIndex(i);
		return disk.getByte(pageNumAndInnerIndex[0], pageNumAndInnerIndex[1]);
	}

	private int[] calculatePageNumAndInnerIndex(int abstractByteNumber) {
//		if (abstractByteNumber < (disk.getPageSize()-8)){
//			return new int[]{0, abstractByteNumber};
//		}else{
			/*
			 * i - is abstractByteNumber
			 * i = pageNum*(pageSize-4) - 4 + innerIndex 
			 * AND
			 * innerIndex < pageSize-4 (from 0 to pageSize-5)
			 * 
			 * this pageNum is index for field 'numbersOfDiskPages' 
			 * causes: (pageSize - 4). 4 bytes (1 int) - it's reference on next page
			 * (...)*(...) - 4 . 1 int on 'size'
			 */
			int pageNum = (abstractByteNumber+4) / (disk.getPageSize() - 4);
			int innnerIndex = (abstractByteNumber+4) % (disk.getPageSize() - 4);
			
			return new int[]{pageNum, innnerIndex};
//		}
	}
	
	private int[] calculateRealPageNumAndInnerIndex(int i) {
		int[] pageNumAndInnerIndex = calculatePageNumAndInnerIndex(i);
		int pageNumberOfFileSlice = pageNumAndInnerIndex [0];
		int innerIndex = pageNumAndInnerIndex[1];
		int realPageNumber = getRealPageNumberOnDiskByRelativeIndex(pageNumberOfFileSlice);
		return new int[]{realPageNumber, innerIndex};
	}

	private int getRealPageNumberOnDiskByRelativeIndex(int relativeIndex) {
		return numbersOfDiskPages.get(relativeIndex);
	}

}
