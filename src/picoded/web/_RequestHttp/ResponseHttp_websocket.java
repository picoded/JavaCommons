package picoded.web._RequestHttp;

import java.net.URI;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.websocket.*;

import picoded.struct.HashMapList;
import picoded.web.ResponseHttp;

/// Extends the basic implmentation to support websocket
///
/// NOTE: This class is considered EXPERIMENTAL! (you been warned)
///
/// NOTE: while the API is deisgned to be thread safe in the future.
/// the current implmentation is not thread safe for usage. 
/// (aka multiple processes using a single ResponseHttp object)
///
/// NOTE: The @ClientEndpoint is disabled as JBOSS searches for this and hooks to it, causing major errors
///
/// @ClientEndpoint
public class ResponseHttp_websocket implements ResponseHttp {
	
	///////////////////////////////////////////////////////////
	// Constructor
	///////////////////////////////////////////////////////////
	
	/// Creates an instance with the target base URL, and connect
	public ResponseHttp_websocket(String requestURL, Consumer<String> handler) {
		
		// Setup the handler
		if (handler != null) {
			setMessageHandler(handler);
		}
		
		// Does the connection (if url given)
		if (requestURL != null) {
			websocketConnect(requestURL);
		}
	}
	
	///////////////////////////////////////////////////////////
	// Internal vars
	///////////////////////////////////////////////////////////
	
	protected volatile WebSocketContainer _container = null;
	protected volatile Session _websocketSession = null;
	
	protected volatile boolean _sendAndWait_block = false;
	protected volatile String _sendAndWait_message = null;
	
	protected volatile Consumer<String> _websocketMessageHandler = null;
	
	///////////////////////////////////////////////////////////
	// Websocket user functions
	///////////////////////////////////////////////////////////
	
	/// indicates if the connection is a websocket
	@Override
	public boolean isWebsocket() {
		return true;
	}
	
	/// indicates if the websocket is currently connected
	@Override
	public boolean isWebsocketConnected() {
		return (_websocketSession != null);
	}
	
	/// Closes the websocket, if it is not closed yet
	/// @TODO : To implement
	@Override
	public void websocketClose() {
		throw new UnsupportedOperationException("Not yet properly implmented");
	}
	
	/// Set/Replaces the existing message handler lisenter. 
	/// This allows a replacement without throwing an exception
	///
	/// @Params    handler, the message handler listener
	///
	/// @Returns   the previous handler if set, if replace is enabled
	@Override
	public Consumer<String> replaceMessageHandler(Consumer<String> handler) {
		Consumer<String> old = _websocketMessageHandler;
		_websocketMessageHandler = handler;
		return old;
	}
	
	/// Gets the currently set message handler
	@Override
	public Consumer<String> getMessageHandler() {
		return _websocketMessageHandler;
	}
	
	/// sends a message via websocket
	@Override
	public void sendMessage(String message) {
		if (_websocketSession == null) {
			throw new RuntimeException("WebSocket is already closed");
		}
		_websocketSession.getAsyncRemote().sendText(message);
	}
	
	/// Sends and waits for a response, needs a more efficent way?
	public String sendAndWait(String message) {
		_sendAndWait_block = true;
		_sendAndWait_message = null;
		
		try {
			sendMessage(message);
			while (_sendAndWait_message == null) {
				Thread.sleep(0, 250000);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			_sendAndWait_block = false;
		}
		
		return _sendAndWait_message;
	}
	
	///////////////////////////////////////////////////////////
	// Websocket system functions
	///////////////////////////////////////////////////////////
	
	/// Ensures that the websocket is connected to _baseURL
	public void websocketConnect(String requestURL) {
		try {
			if (_container == null) {
				_container = ContainerProvider.getWebSocketContainer();
			}
			
			if (_websocketSession == null) {
				_websocketSession = _container.connectToServer(this, new URI(requestURL));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Actual onOpen reciever for the websocket protocol
	@OnOpen
	public void _onOpen(Session userSession) {
		_websocketSession = userSession;
	}
	
	/// Actual onClose reciever for the websocket protocol
	@OnClose
	public void _onClose(Session userSession, CloseReason reason) {
		_websocketSession = null;
	}
	
	/// Actual onMessage reciever for websocket protocol
	@OnMessage
	public void _onMessage(String message) {
		if (_sendAndWait_block) {
			_sendAndWait_message = message;
			_sendAndWait_block = false;
		} else if (_websocketMessageHandler != null) {
			_websocketMessageHandler.accept(message);
		}
	}
	
	///////////////////////////////////////////////////////////
	// Disable standard http support
	// @TODO : Support basic headers / cookeis
	///////////////////////////////////////////////////////////
	
	public String toString() {
		return null;
	}
}
