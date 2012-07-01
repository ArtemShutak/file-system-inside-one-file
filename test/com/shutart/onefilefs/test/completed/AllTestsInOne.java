package com.shutart.onefilefs.test.completed;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsInOne{
	
   public static Test suite(){ 
     TestSuite suite = new TestSuite("All tests");

     suite.addTest(new JUnit4TestAdapter(RealFileTest.class));
     suite.addTest(new JUnit4TestAdapter(FileImplTestsWithMemoryFileSystem.class));
     suite.addTest(new JUnit4TestAdapter(MemoryDiskTest.class));
     suite.addTest(new JUnit4TestAdapter(OneFileDiskTest.class));
     suite.addTest(new JUnit4TestAdapter(DiskDriverImplSpecOverMemoryDisk.class));

     return suite; 
   }
}
