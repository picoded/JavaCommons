package picoded.utils;

import java.io.*;

/// StreamRedirectThread connects an inputstream to an outputstream
/// 
/// This can either be setup and run as a seperate thread,
/// or used incrementally in a larger loop
///
/// @TODO A proper test case. A possible example test case would be
/// opening a read stream, from a test file (hello wolrd). to writing to a new empty
/// test file. Then closing and checking the file contents. Test should run both for incremental,
/// thread.start(), and run().
/// 
public class StreamRedirectThread extends Thread implements Runnable {
	
	// Constructor variables
	//------------------------------------------------------------------
	
	private InputStream input = null;
	private OutputStream output = null;
	
	private int buffer_size = 2048;
	private byte[] incremental_buffer = null;
	
	// Constructor functions
	//------------------------------------------------------------------
	
	/// Setup with the input and output stream. 
	/// Thread name defaults to 'StreamRedirectThread'
	/// 
	/// @params {InputStream}  input
	/// @params {OutputStream} output
	///
	public StreamRedirectThread(InputStream in, OutputStream out) {
		this(null, in, out);
	}
	
	/// Setup with the input and output stream
	/// 
	/// @params {String}       name
	/// @params {InputStream}  input
	/// @params {OutputStream} output
	/// 
	public StreamRedirectThread(String name, InputStream in, OutputStream out) {
		super((name == null || name.length() <= 0) ? "StreamRedirectThread" : name); //Setup with named
		input = in;
		output = out;
		setPriority(Thread.MAX_PRIORITY - 1);
	}
	
	// Redirect incremental, and thread runner
	//------------------------------------------------------------------
	
	/// Does an incemental step in loading the thread
	///
	/// @returns {int}  the amount of bytes transfered in the step, 
	///                 -1 indicates the end of stream
	/// 
	public int runStep() throws IOException {
		if (incremental_buffer == null) {
			incremental_buffer = new byte[buffer_size];
		}
		
		int count = input.read(incremental_buffer, 0, buffer_size);
		if (count > 0) {
			output.write(incremental_buffer, 0, count);
		}
		return count;
	}
	
	/// The full stream redirect function, use the Thread.start() to start the process,
	/// followed by Thread.join() to wait for the process to complete.
	/// 
	/// Calling run() directly instead of start(), runs the process in the current thread instead.
	/// 
	public void run() {
		try {
			byte[] run_buffer = new byte[buffer_size];
			int count;
			
			while ((count = input.read(run_buffer, 0, buffer_size)) >= 0) {
				if (count == 0) {
					Thread.sleep(0);
				} else {
					output.write(run_buffer, 0, count);
				}
			}
			output.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
