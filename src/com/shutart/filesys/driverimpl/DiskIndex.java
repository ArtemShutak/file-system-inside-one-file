package com.shutart.filesys.driverimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.shutart.filesys.domain.IDisk;


final class DiskIndex {

	private static final int FREE_FILE_ID = -1;
	
	private final IDisk disk;
	private int firstCandidateToBeFreeFileId;
	private int firstCandidateToBeNumberOfFreePage;

	DiskIndex(IDisk disk) {
		this.disk = disk;
//		this.format();
	}
	
	void releaseAllCounters(){
		firstCandidateToBeFreeFileId = 0;
		firstCandidateToBeNumberOfFreePage = 0;
	}
	
	int getPageNumBy(int fileId) {
		try {
			int[] pageNumAndInnerIndex = calculatePageNumAndInnerIndex4FileId(fileId);
			int pageNumber = pageNumAndInnerIndex[0];
			int from = pageNumAndInnerIndex[1];
			byte[] pageNumAsBytes = disk.getPageContent(pageNumber, from, from + 4);
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(
					pageNumAsBytes));
			return in.readInt();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}


	private void setPageNum4FileId(int fileId, int pageNum){
		try {
			int[] pageNumAndInnerIndex = calculatePageNumAndInnerIndex4FileId(fileId);
			int pageNumber = pageNumAndInnerIndex[0];
			int from = pageNumAndInnerIndex[1];
			
			ByteArrayOutputStream outByteArray = new ByteArrayOutputStream(4);
			DataOutputStream out = new DataOutputStream(outByteArray );
			out.writeInt(pageNum);
			byte[] pageNumAsBytes = outByteArray.toByteArray();

			disk.setPageContent(pageNumber, from, pageNumAsBytes);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * @return free file id (from 0 to {@link IDisk#getNumberOfPages()}-1 )
	 */
	int getFreeFileIdAndTake() {
		for (int fileId = firstCandidateToBeFreeFileId; fileId < disk.getNumberOfPages(); fileId++) {
			if (getPageNumBy(fileId)==FREE_FILE_ID)
				return takeThisFileIdAndGetIndex(fileId);
		}
		for (int fileId = 0; fileId < firstCandidateToBeFreeFileId; fileId++) {
			if (getPageNumBy(fileId) == FREE_FILE_ID)
				return takeThisFileIdAndGetIndex(fileId);
		}
		throw new IllegalStateException("Disk overflow. Disk don't have free fileId");
	}

	private int takeThisFileIdAndGetIndex(int fileId) {
		int pageNum = getFreePageNumAndTake();
		setPageNum4FileId(fileId, pageNum);
		setZeroSize4Page(pageNum);
		
		firstCandidateToBeFreeFileId = fileId + 1;
		if (firstCandidateToBeFreeFileId == disk.getNumberOfPages())
			firstCandidateToBeFreeFileId = 0;
		return fileId;
	}

	private void setZeroSize4Page(int pageNum) {
		disk.setPageContent(pageNum, 0, new byte[] {0, 0, 0, 0});
	}

	void setFileIdAsFree(int fileId) {
		setPageNum4FileId(fileId, FREE_FILE_ID);
	}

	int getFreePageNumAndTake(){
		for (int pageNum = firstCandidateToBeNumberOfFreePage; pageNum < disk.getNumberOfPages(); pageNum++) {
			if (pageIsFree(pageNum))
				return takeThisPagedAndGetIndex(pageNum);
		}
		for (int pageNum = 0; pageNum < firstCandidateToBeNumberOfFreePage; pageNum++) {
			if (pageIsFree(pageNum))
				return takeThisPagedAndGetIndex(pageNum);
		}
		throw new IllegalStateException("Disk overflow. Disk don't have free page.");
	}
	
	private int takeThisPagedAndGetIndex(int pageNum) {
		setPageIsFreeOrNot(pageNum, false);
		firstCandidateToBeNumberOfFreePage = pageNum + 1;
		if (firstCandidateToBeNumberOfFreePage == disk.getNumberOfPages())
			firstCandidateToBeNumberOfFreePage = getIndexSizeInPages();
		return pageNum;
	}

	void setPageAsFree(int pageNum) {
		setPageIsFreeOrNot(pageNum, true);
	}

	void format() {
		for (int fileId = 0; fileId < disk.getNumberOfPages(); fileId++) {
			setFileIdAsFree(fileId);
		}
		for (int pageNum = 0; pageNum < getIndexSizeInPages(); pageNum++){
			setPageIsFreeOrNot(pageNum, false);
		}
		for (int pageNum = getIndexSizeInPages(); pageNum < disk.getNumberOfPages(); pageNum++) {
			setPageAsFree(pageNum);
		}
	}
	

	private boolean pageIsFree(int pageNum){
		int[] pageNumAndInnerIndex = calculatePageNumAndInnerIndex4PageNum(pageNum);
		int pageNumber = pageNumAndInnerIndex[0];
		int innerIndex = pageNumAndInnerIndex[1];
		byte pageIsFree = disk.getByte(pageNumber, innerIndex);
		return pageIsFree != 0;
	}

	private void setPageIsFreeOrNot(int pageNum, boolean isFree){
		int[] pageNumAndInnerIndex = calculatePageNumAndInnerIndex4PageNum(pageNum);
		int pageNumber = pageNumAndInnerIndex[0];
		int innerIndex = pageNumAndInnerIndex[1];
		disk.setByte(pageNumber, innerIndex, (isFree ? (byte) 1 : (byte) 0));
	}

	private int[] calculatePageNumAndInnerIndex4FileId(int fileId) {
		if (disk.getPageSize() % 4 != 0)
			throw new IllegalStateException("Size of page ("
					+ disk.getPageSize()+ ") must be devisable by 4");
		/*
		 * fileId = pageNum*(pageSize/4) + innerIndex 
		 * AND
		 * innerIndex < pageSize/4 (from 0 to (pageSize/4 - 1))
		 */
		int maxNumberOfFileIdInOnePage = disk.getPageSize() / 4;
		int pageNum = fileId / maxNumberOfFileIdInOnePage;
		int innnerIndexDiv4 = fileId % maxNumberOfFileIdInOnePage;
		
		return new int[]{pageNum, innnerIndexDiv4 * 4};
	}
	
	private int[] calculatePageNumAndInnerIndex4PageNum(int pageNum) {
		int pageNum_ = pageNum / disk.getPageSize() + 1 + getSizeOfFileId2PageNumArrayInPages();
		int innnerIndexDiv4 = pageNum % disk.getPageSize();
		
		return new int[]{pageNum_ , innnerIndexDiv4 };

	}

	private int getIndexSizeInPages() {
		int maxPagNumber = disk.getNumberOfPages() - 1;
		return calculatePageNumAndInnerIndex4PageNum(maxPagNumber)[0] + 1;
	}
	private int getSizeOfFileId2PageNumArrayInPages() {
		int maxNumberOfFileId = disk.getNumberOfPages() - 1;
		return calculatePageNumAndInnerIndex4FileId(maxNumberOfFileId)[0];
	}

	
	
}
