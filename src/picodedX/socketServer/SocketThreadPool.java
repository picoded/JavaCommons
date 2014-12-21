package picoded.socketServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.IllegalArgumentException;

import picoded.socketServer.RequestListener;

///
/// @version 0.1 Experimental
///
/// Provides a socket thread pool
///
public class SocketThreadPool implements Runnable {

	protected int serverPort = 9000;
	protected ServerSocket serverSocket = null;

	protected boolean isStopped = false;
	protected Thread runningThread = null;

	protected Class reqListenerClass = null;
	protected Object[] reqInitArgumentArray = null;

	protected ExecutorService threadPool = null;

	/// Intializes the SocketThreadPool
	/// 
	/// with the given RequestListener Class object, and the initial argument array (to pass parameters)
	/// and a thread pool to run for, this is used to provided finer control over the multi-thread pool (default 10, if null)
	///
	/// This function is automatically called by the constructor
	private void initSocketThreadPool(ExecutorService threadPool, int port, Class reqListenerClass,
	                  Object[] initArgumentArray) {
		this.threadPool = threadPool;

		if (this.threadPool == null) {
			this.threadPool = Executors.newFixedThreadPool(10);
			;
		}

		this.serverPort = port;
		this.reqListenerClass = reqListenerClass;
		this.reqInitArgumentArray = initArgumentArray;

		if (this.reqListenerClass == null) {
			throw new IllegalArgumentException("Request Listener Class Parameter cannot be NULL");
		}
	}

	/// Intializes the SocketThreadPool
	/// 
	/// with the given RequestListener Class object, and the initial argument array (to pass parameters)
	/// and a thread pool to run for, this is used to provided finer control over the multi-thread pool (default 10)
	public SocketThreadPool(ExecutorService threadPool, int port, Class reqListenerClass, Object[] initArgumentArray) {
		this.initSocketThreadPool(threadPool, port, reqListenerClass, initArgumentArray);
	}

	/// Intializes the SocketThreadPool
	/// 
	/// with the given RequestListener Class object, and the initial argument array (to pass parameters)
	public SocketThreadPool(int port, Class reqListenerClass, Object[] initArgumentArray) {
		this.initSocketThreadPool(null, port, reqListenerClass, initArgumentArray);
	}

	/// Simplified version of SocketThreadPool, without argument array
	public SocketThreadPool(int port, Class reqListenerClass) {
		this.initSocketThreadPool(null, port, reqListenerClass, null);
	}

	/// Simplified DEMO version of SocketThreadPool (uses stock RequestListener)
	public SocketThreadPool(int port) {
		this.initSocketThreadPool(null, port, RequestListener.class, null);
	}

	/// Runs the actual server program, and start accepting requests, starts branching from the current thread
	public void run() {
		synchronized (this) {
			if (this.runningThread != null) {
				throw new RuntimeException("Server already has a running base thread");
			}
			this.runningThread = Thread.currentThread(); //branch
		}
		openServerSocket();

		while (!isStopped()) {
			Socket clientSocket = null;
			try {
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				if (isStopped()) {
					//System.out.println("Server Stopped.") ;
					return;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}

			//Run the request, and start a RequestListener instance for it
			Object initObj = null;
			try {
				initObj = this.reqListenerClass.newInstance();
			} catch (InstantiationException ex) {
				throw new RuntimeException(ex);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}

			if (initObj == null) {
				throw new RuntimeException("Failed to initiate RequestListener Argument Class Instance");
			} else if (!(initObj instanceof RequestListener)) {
				throw new RuntimeException("RequestListener Argument Class Instance is not instance of RequestListener");
			}

			RequestListener reqObj = (RequestListener) initObj;

			reqObj.requestSocket = clientSocket;
			reqObj.initArgumentArray = this.reqInitArgumentArray;

			this.threadPool.execute(reqObj);

			// This DOES NOT work / do what it may seem to do : Causes a crash
			//try {
			//	clientSocket.close(); //finishes it
			//} catch(IOException e) {
			//	throw new RuntimeException(e);
			//}
		}
		this.threadPool.shutdown();
		//System.out.println("Server Stopped.") ;
	}

	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop() {
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port " + serverPort, e);
		}
	}
}