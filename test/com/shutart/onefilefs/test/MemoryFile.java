package com.shutart.onefilefs.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.shutart.filesys.domain.IFile;

public final class MemoryFile implements IFile {

	private final String fileName;
	private final List<Byte> bytes = new ArrayList<Byte>();

	public MemoryFile(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public boolean isEmpty() {
		return bytes.isEmpty();
	}

	@Override
	public String getName() {
		return fileName;
	}

	@Override
	public OutputStream getOutputStream() {
		return new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				bytes.add((byte) b);
			}
		};
	}

	@Override
	public InputStream getInputStream() {
		return new InputStream() {
			private final Iterator<Byte> iter = bytes.iterator();
			
			@Override
			public int read() throws IOException {
				if (! iter.hasNext())
					return -1;
				return iter.next();
			}
		};
	}

}
