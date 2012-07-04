package com.shutart.filesys.domain;

public interface IBytesOfFile {

	int size();

	void add(byte b);

	void clear();

	void set(int index, byte b);

	int get(int i);

}