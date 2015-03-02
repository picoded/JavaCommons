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
import java.lang.Math;
import java.lang.Integer;
import java.lang.IllegalArgumentException;
import java.net.UnknownHostException;

import picoded.socketServer.RequestListener;

///
/// @version 0.1 Experimental
///
/// Simple proxy class, that just relays the connection to the target ip:port, via TCP?
/// 
public class SimpleProxy extends RequestListener {
	
	private static int bufferCacheSize = 64; //small linecache size buffer that hopefully the cpu can optimize
	
	/// Argument array consists of the following arguments
	///
	/// argumentArray[0]  - ip:port address string
	@Override
	public void requestRunner(InputStream input, OutputStream output, Object[] argumentArray) throws IOException {
		
		// Default host and port
		String host = null;
		int port = 80;
		
		// Closes connection if there is no data for this time frame, in milliseconds
		long noActivityTimeout = 30000;
		
		// Extract out the host and port number from the first argument
		//----------------------------------------------------------------------
		if (argumentArray.length >= 1 && argumentArray[0] instanceof String) {
			String rawHostPort = (String) argumentArray[0];
			int indxOf;
			
			if (rawHostPort.charAt(0) == '[') { //IPv6 block?
				indxOf = rawHostPort.indexOf(']');
				
				if (indxOf > 0) { //gets the IPv6 address
					host = rawHostPort.substring(1, indxOf - 1);
				}
				
				rawHostPort = rawHostPort.substring(indxOf + 1);
			} else {
				indxOf = rawHostPort.lastIndexOf(':');
				
				if (indxOf > 0) { //gets the IPv6 address
					host = rawHostPort.substring(0, indxOf);
				}
				
				rawHostPort = rawHostPort.substring(indxOf);
			}
			
			if (host == null) {
				throw new IllegalArgumentException("Invalid host address " + argumentArray[0]);
			}
			
			indxOf = rawHostPort.lastIndexOf(':');
			if (indxOf >= 0) {
				try {
					port = Integer.parseInt(rawHostPort.substring(indxOf + 1));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid port number " + argumentArray[0]);
				}
			}
		} else {
			host = "127.0.0.1"; //default
			port = 80;
		}
		
		// Prepares the socket connection
		//----------------------------------------------------------------------
		try {
			
			//Connects to the target server
			Socket serverSocket = new Socket(host, port);
			
			//Disable keep alive, if no activity timeout <= 0
			//if(noActivityTimeout <= 0) {
			serverSocket.setKeepAlive(false);
			requestSocket.setKeepAlive(false);
			//}
			
			//gets the target input and output streams
			OutputStream serverOutput = serverSocket.getOutputStream();
			InputStream serverInput = serverSocket.getInputStream();
			
			int clientInputLength;
			int serverInputLength;
			
			long lastActivity = System.currentTimeMillis();
			
			byte[] buffer = new byte[bufferCacheSize];
			
			byte[] last4bytes = new byte[] { 0, 0, 0, 0 }; //check for http termination
			boolean reachedEndOfRequest = false; //reaches end of request in accordence to http specs?
			boolean closeCheck = false;
			boolean hasData = false;
			boolean serverBegunResponse = false;
			
			System.out.println("[Request Recieved: " + System.currentTimeMillis() + "]");
			
			// loops to duplicate data streams between client and server
			// 
			// how i wish this could be optimized as zero copy eith PF_RNG
			//--------------------------------------------------------------------
			while (true) {
				hasData = false;
				
				// Gets the client data input
				//-----------------------------
				do {
					clientInputLength = Math.min(input.available(), bufferCacheSize);
					if (clientInputLength >= 1) {
						clientInputLength = input.read(buffer, 0, clientInputLength);
						hasData = true;
						
						System.out.write(buffer, 0, clientInputLength);
						serverOutput.write(buffer, 0, clientInputLength);
						
						// Checks for HTTP request termination opportunistically
						// this is based on the following.
						//
						// http://stackoverflow.com/questions/13353592/while-reading-from-socket-how-to-detect-when-the-client-is-done-sending-the-requ
						// NOTE: Content-Length checks is ignored
						if (buffer[clientInputLength - 1] == -1) { //termination of request header
							reachedEndOfRequest = true;
						} else { //normal data, terminates all checks
						
							// Copies the last 4 byte (for checks)
							//---------------------------------------
							if (clientInputLength >= 4) { //copies the last 4 byte entirely
								System.arraycopy(buffer, //original data
								   clientInputLength - 4, //starts from 4th last character 
								   last4bytes, //store into buffer for checks
								   0, //fill entirely the checking buffer
								   4 //store the 4 bytes of data
								   );
							} else { //copies it in remaining part
								System.arraycopy(last4bytes, //previously buffered data (if any)
								   clientInputLength, //offset to copy from
								   last4bytes, //store into buffer for checks
								   0, //push data to zero position
								   4 - clientInputLength //by the existing data
								   );
								
								System.arraycopy(buffer, //original data
								   0, //stores all (less then 4)
								   last4bytes, //store into buffer for checks
								   4 - clientInputLength, //from the last copied byte onwards
								   clientInputLength //last byte length
								   );
							}
							
							// Check the last 4 byte
							//------------------------
							if (last4bytes[0] == '\r' && last4bytes[1] == '\n' && last4bytes[2] == '\r'
							   && last4bytes[3] == '\n') {
								reachedEndOfRequest = true;
							} else {
								reachedEndOfRequest = false;
							}
						}
					}
				} while (clientInputLength >= 1);
				
				// Gets the server data output
				//-------------------------------
				do {
					serverInputLength = Math.min(serverInput.available(), bufferCacheSize);
					
					if (serverInputLength >= 1) {
						serverInputLength = serverInput.read(buffer, 0, serverInputLength);
						serverBegunResponse = true;
						hasData = true;
						
						System.out.write(buffer, 0, serverInputLength);
						output.write(buffer, 0, serverInputLength);
					}
				} while (serverInputLength >= 1);
				
				// Checks for any closed sockets
				//---------------------------------
				if (serverSocket.isClosed() || !serverSocket.isConnected() || serverSocket.isInputShutdown()
				   || serverSocket.isOutputShutdown()) {
					serverInputLength = -1;
				}
				if (requestSocket.isClosed() || !requestSocket.isConnected() || requestSocket.isInputShutdown()
				   || requestSocket.isOutputShutdown()) {
					clientInputLength = -1;
				}
				
				// Runs loop atleast once again, before actually closing the socket (just in case)
				//-----------------------------------------------------------------------------------
				if (clientInputLength == -1 || serverInputLength == -1
				   || (reachedEndOfRequest == true && serverBegunResponse == true)) {
					
					if (closeCheck) {
						break;
					}
					closeCheck = true; //allow 1 more iteration, before termination
				} else {
					closeCheck = false;
				}
				
				// Triggers timeout due to lack of activity, if any
				//-----------------------------------------------------------
				if (hasData == false) {
					if ((lastActivity + noActivityTimeout) < System.currentTimeMillis()) {
						//timed out, closes "keep-alive" connection
						System.out.println("[Request keep-alive timeout]");
						break;
					}
				} else {
					lastActivity = System.currentTimeMillis();
				}
				
				try {
					Thread.sleep(1); //let others take priority if need be
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			
			serverInput.close();
			serverOutput.close();
			serverSocket.close();
			
			System.out.println("[Request Closed]");
			
		} catch (UnknownHostException e) {
			throw new RuntimeException("Unknown host: " + host, e);
		} catch (IOException e) {
			throw new RuntimeException("I/O Exception for host: " + host, e);
		}
	}
}
