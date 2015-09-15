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

public class StreamRedirectThread_test {
    
    private StreamRedirectThread srt = null;
    private InputStream input = null;
    private OutputStream output = null;
    private File outputFile = null;
	
	@Before
	public void setUp() throws FileNotFoundException {
		File outputFolder = new File("./test-files/tmp/utils/StreamRedirectThread/");
		// makes the output directory tmporary folder as needed
		outputFolder.mkdirs();
		outputFile = new File(outputFolder, "outputFile.txt");
		
		input = new FileInputStream("./test-files/test-specific/utils/StreamRedirectThread/inputFile.txt");
		output = new FileOutputStream(outputFile);
		
		srt = new StreamRedirectThread(input, output);
	}
	
	@Test @SuppressWarnings("unchecked")
	public void runTest() throws IOException, FileNotFoundException {
		srt.start();
		
		assertTrue(outputFile.exists());
		assertEquals(IOUtils.toByteArray(input), IOUtils.toByteArray(new FileInputStream(outputFile)));
	}

}
