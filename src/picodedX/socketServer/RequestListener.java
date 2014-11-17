package picoded.socketServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

///
/// @version 0.1 Experimental
///
/// Base class for creating a socket listener.
///
/// This primarily performs a System.out for the input stream till it is terminated, via a requestEvent function.
/// This function is meant to be over-ridden when extending for the specific respective functionality
/// 
public class RequestListener implements Runnable {
	
	/// the requestListener parent socket
	public Socket requestSocket = null;
	
	/// the argument array used to initiate this class
	public Object[] initArgumentArray = null;
	
	/// Initiates the class with nothing defined
	public RequestListener() {
		
	}
	
	/// Initiates the class, with the request socket
	public RequestListener(Socket requestSocket, Object[] initArgumentArray) {
		this.requestSocket = requestSocket;
		this.initArgumentArray = initArgumentArray;
	}
	
	/// The actually RequestListener runner, that processes each input/output request
	public void requestRunner(InputStream input, OutputStream output, Object[] argumentArray) throws IOException {
		long time = System.currentTimeMillis();
		System.out.println("[Request Recieved: " + time+"]");

		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			//Teminator for http headers
			if(inputLine.equals("")) { //blank: terminates header tag
				break;
			}
			
			System.out.println(inputLine);
		}
		output.write(("HTTP/1.1 200 OK\n\nWorkerRunnable: " +
						  "RequestListener Demo @ UnixTimestamp: " +
						  time +
						  "").getBytes());
		output.close();
		in.close();
		input.close();
		
		System.out.println("[Request Closed]");
	}
	
	/// The runnable run function, used internally by SocketThreadPool
	public void run() {
		try {
			InputStream input  = requestSocket.getInputStream();
			OutputStream output = requestSocket.getOutputStream();
			
			this.requestRunner(input, output, initArgumentArray);
			
			output.close();
			input.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
