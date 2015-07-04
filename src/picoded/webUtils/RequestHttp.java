package picoded.webUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import picoded.webUtils.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.cookie.*;
import org.apache.http.entity.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.cookie.*;
import org.apache.http.protocol.*;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.protocol.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import javax.websocket.*;
import java.net.URI;
import picoded.FunctionalInterface.*;
//import java.util.function;

@ClientEndpoint
public class RequestHttp {

	public String _baseURL = "http://localhost";
	
	/// Creates an instance with the target base URL
	public RequestHttp(String requestURL) {
		_baseURL = requestURL;
	}
	
	///////////////////////////////////////////////////////////
	// Websocket functions
	///////////////////////////////////////////////////////////
	protected WebSocketContainer container = null;
	protected Session websocketSession = null;
	protected Void_String websocketMessageHandler = null;
	
	protected boolean _sendAndWait_block = false;
	protected String _sendAndWait_message = null;
	
	/// 
	
	/// Ensures that the websocket is connected
	public void websocketConnect() {
		try {
			if( container == null ) {
				container = ContainerProvider.getWebSocketContainer();
			}
			
			if( websocketSession == null ) {
				websocketSession = container.connectToServer(this, new URI(_baseURL) );
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Response message
	public void addMessageHandler(Void_String msgHandler) {
		this.websocketMessageHandler = msgHandler;
	}
	
	/// Sends message
	public void sendMessage(String message) {
		this.websocketSession.getAsyncRemote().sendText(message);
	}
	
	/// Sends and waits for a response, needs a more efficent way?
	public String sendAndWait(String message) {
		_sendAndWait_block = true;
		_sendAndWait_message = null;
		
		try {
			sendMessage(message);
			while( _sendAndWait_message == null ) {
				Thread.sleep(0, 250000);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			_sendAndWait_block = false;
		}
		
		return _sendAndWait_message;
	}
	
	/// Time out in milliseconds?
	public String sendAndWait(String message, long timeout) {
		throw new RuntimeException("TO IMPLEMENT");
	}

	/// Actual onOpen reciever for the socket protocol
	@OnOpen
	public void _onOpen(Session userSession) {
		this.websocketSession = userSession;
	}
	
	/// Actual onClose reciever for the socket protocol
	@OnClose
	public void _onClose(Session userSession, CloseReason reason) {
		this.websocketSession = null;
	}
	
	@OnMessage
	public void _onMessage(String message) {
		if( _sendAndWait_block ) {
			_sendAndWait_message = message;
			_sendAndWait_block = false;
		} else if (this.websocketMessageHandler != null) {
			this.websocketMessageHandler.accept(message);
		}
	}
	
	///////////////////////////////////////////////////////////
	// Static core functions?
	///////////////////////////////////////////////////////////

	/// Performs a raw HTTP request, this assumes that the function caller has fully handled
	/// all the various basic aspects of the request, like GET parameters, etc.
	protected static ResponseHttp raw(
		HttpRequestType requestType, //request type 
		String requestURL, //Request URL, with GET parameters if needed
		Map<String, String[]> headerMap, //Header map to values
		HttpEntity apacheRequestEntity //POST / PUT payload, if applicable
	) throws IOException {
		// Prepae the HttpUriRequest, based on request type
		HttpUriRequest methodToMakeRequest = RequestHttpUtils.apache_HttpUriRequest_fromRequestType(requestType, requestURL);
		
		// Add in headers as applicable
		
		// Create the http client, and get the response
		HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().disableAuthCaching().build();
		
		// Add the http request entity?
		if(apacheRequestEntity != null) {
			((HttpEntityEnclosingRequestBase)methodToMakeRequest).setEntity(apacheRequestEntity);
		}
		// Call and return
		HttpResponse response = httpClient.execute(methodToMakeRequest);
		
		return new ResponseHttp(response);
	}
	
	/// Performs the most basic of get requests
	public static ResponseHttp get( String requestURL ) throws IOException {
		return raw(HttpRequestType.TYPE_GET, requestURL, null, null);
	}
	
	/// Performs a basic post request
	public static ResponseHttp post( String requestURL, Map<String,String[]> postMap ) throws IOException {
		List<NameValuePair> listNameValuePairs = RequestHttpUtils.parameterMapToList(postMap);
		return raw(HttpRequestType.TYPE_POST, requestURL, null, new UrlEncodedFormEntity(listNameValuePairs) );
	}
	
}
