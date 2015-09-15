package picodedTests.utils;

import org.junit.*;

import static org.junit.Assert.*;

import picoded.utils.StreamRedirectThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;

public class StreamRedirectThread_test {
    
    private StreamRedirectThread srt = null;
    
    private File inputFile = null;
    private File outputFile = null;
	
	@Before
	public void setUp() throws IOException, FileNotFoundException {
	    // output file folder
		File outputFolder = new File( "./test-files/tmp/utils/StreamRedirectThread/" );
		
		// makes the output directory tmporary folder as needed
		outputFolder.mkdirs();
		
		// output file
		outputFile = new File( outputFolder, "outputFile.txt" );
		
		// input file
		inputFile = new File( "./test-files/test-specific/utils/StreamRedirectThread/inputFile.txt" );
		
		srt = new StreamRedirectThread( new FileInputStream( inputFile ), new FileOutputStream( outputFile ) );
	}
	
	@Test
	public void runTest() throws IOException, FileNotFoundException {
		srt.run();
		
		// output file should be exist
		assertTrue( outputFile.exists() );
		
		// match contents of input and output files
		assertEquals( FileUtils.readFileToString(inputFile), FileUtils.readFileToString(outputFile) );
	}

	@Test
	public void startTest() throws IOException, FileNotFoundException {
		srt.start();
		
		// output file should be exist
		assertTrue( outputFile.exists() );
		
		// match contents of input and output files
		assertEquals( FileUtils.readFileToString(inputFile), FileUtils.readFileToString(outputFile) );
	}

}
