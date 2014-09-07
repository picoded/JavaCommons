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
		if(argumentArray.length >= 1 && argumentArray[0] instanceof String) {
			String rawHostPort = (String)argumentArray[0];
			int indxOf;
			
			if( rawHostPort.charAt(0) == '[' ) { //IPv6 block?
				indxOf = rawHostPort.indexOf(']');
				
				if(indxOf > 0) { //gets the IPv6 address
					host = rawHostPort.substring(1, indxOf-1);
				}
				
				rawHostPort = rawHostPort.substring(indxOf+1);
			} else {
				indxOf = rawHostPort.lastIndexOf(':');
				
				if(indxOf > 0) { //gets the IPv6 address
					host = rawHostPort.substring(0, indxOf);
				}
				
				rawHostPort = rawHostPort.substring(indxOf);
			}
			
			if(host == null) {
				throw new IllegalArgumentException("Invalid host address "+argumentArray[0]);
			}
			
			indxOf = rawHostPort.lastIndexOf(':');
			if(indxOf >= 0) {
				try {
					port = Integer.parseInt(rawHostPort.substring(indxOf+1));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid port number "+argumentArray[0]);
				}
			}
		} else {
			host = "127.0.0.1"; //default
		}
		
		// Prepares the socket connection
		//----------------------------------------------------------------------
		try {
			Socket echoSocket = new Socket(host, port);
			
			//Disable keep alive
			echoSocket.setKeepAlive(false);
			requestSocket.setKeepAlive(false);
			
			OutputStream echoOutput = echoSocket.getOutputStream();
			InputStream echoInput = echoSocket.getInputStream();
			
			int clientInputPt;
			int echoInputPt;
			
			long lastActivity = System.currentTimeMillis();
			
			byte[] buffer = new byte[bufferCacheSize];
			boolean closeCheck = false;
			
			System.out.println("[Request Recieved: " + System.currentTimeMillis() +"]");
			
			//loops to duplicate data streams
			while(true) {
				do {
					clientInputPt = Math.min( input.available(), bufferCacheSize );
					
					if(clientInputPt >= 1) {
						clientInputPt = input.read(buffer, 0, clientInputPt);
						System.out.write( buffer, 0, clientInputPt );
						echoOutput.write( buffer, 0, clientInputPt );
					} 
				} while(clientInputPt >= 1);
				
				do {
					echoInputPt = Math.min( echoInput.available(), bufferCacheSize );
					
					if(echoInputPt >= 1) {
						echoInputPt = echoInput.read(buffer, 0, echoInputPt);
						System.out.write( buffer, 0, echoInputPt );
						output.write( buffer, 0, echoInputPt );
					}
				} while(echoInputPt >= 1);
				
				//Checks for any closed sockets
				if( echoSocket.isClosed() || !echoSocket.isConnected() || 
					 echoSocket.isInputShutdown() || echoSocket.isOutputShutdown() ) {
					echoInputPt = -1;
				}
				if( requestSocket.isClosed() || !requestSocket.isConnected() || 
					 requestSocket.isInputShutdown() || requestSocket.isOutputShutdown() ) {
					clientInputPt = -1;
				}
				
				//Flush remaining data before closing
				if(clientInputPt == -1 ||  echoInputPt == -1) {
					if(closeCheck) {
						break;
					}
					closeCheck = true; //allow 1 more iteration, before termination
				} else {
					closeCheck = false;
				}
				
				//Triggers timeout due to lack of activity, if any
				if(clientInputPt <= 0 && echoInputPt  <= 0) {
					if( (lastActivity+noActivityTimeout) < System.currentTimeMillis() ) {
						//timed out, closes "keep-alive" connection
						System.out.println("[Request keep-alive timeout]");
						break;
					}
					
					try {
						Thread.sleep(500); //let others take priority if need be
					} catch(InterruptedException e) {
						throw new RuntimeException(e);
					}
				} else {
					lastActivity = System.currentTimeMillis();
					Thread.yield(); //let others take priority if need be
				}
			}
			
			echoInput.close();
			echoOutput.close();
			echoSocket.close();
			
			System.out.println("[Request Closed]");
			
		} catch (UnknownHostException e) {
			throw new RuntimeException("Unknown host: "+host, e);
		} catch (IOException e) {
			throw new RuntimeException("I/O Exception for host: "+host, e);
		} 
	}
	
}
