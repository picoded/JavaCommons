package picoded.webUtils;

import java.util.Map;

import picoded.enums.HttpRequestType;
import picoded.webUtils._RequestHttp.RequestHttp_apache;

///
/// Sometimes you just want to do a simple HTTP Request and response
/// that JUST WORKS. Without needing to handle like a dozen over import types,
/// or complex setup (staring at you apache).
///
/// Aka: KISS (for) the user programmer for this class
///
/// KISS: Keep It Simple Stupid
///
public class RequestHttp {

	// public String _baseURL = "http://localhost";
	// 
	// /// Creates an instance with the target base URL
	// public RequestHttp(String requestURL) {
	// 	_baseURL = requestURL;
	// }
	// 
	// ///////////////////////////////////////////////////////////
	// // Websocket functions
	// ///////////////////////////////////////////////////////////
	// protected WebSocketContainer container = null;
	// protected Session websocketSession = null;
	// protected Void_String websocketMessageHandler = null;
	// 
	// protected boolean _sendAndWait_block = false;
	// protected String _sendAndWait_message = null;
	// 
	// /// 
	// 
	// /// Response message
	// public void addMessageHandler(Void_String msgHandler) {
	// 	this.websocketMessageHandler = msgHandler;
	// }
	// 
	// /// Sends and waits for a response, needs a more efficent way?
	// public String sendAndWait(String message) {
	// 	_sendAndWait_block = true;
	// 	_sendAndWait_message = null;
	// 	
	// 	try {
	// 		sendMessage(message);
	// 		while( _sendAndWait_message == null ) {
	// 			Thread.sleep(0, 250000);
	// 		}
	// 	} catch(Exception e) {
	// 		throw new RuntimeException(e);
	// 	} finally {
	// 		_sendAndWait_block = false;
	// 	}
	// 	
	// 	return _sendAndWait_message;
	// }
	// 
	// /// Time out in milliseconds?
	// public String sendAndWait(String message, long timeout) {
	// 	throw new RuntimeException("TO IMPLEMENT");
	// }
	// 
	// ///////////////////////////////////////////////////////////
	// // Core websocket functions
	// ///////////////////////////////////////////////////////////
	// 
	// /// Ensures that the websocket is connected
	// public void websocketConnect() {
	// 	try {
	// 		if( container == null ) {
	// 			container = ContainerProvider.getWebSocketContainer();
	// 		}
	// 		
	// 		if( websocketSession == null ) {
	// 			websocketSession = container.connectToServer(this, new URI(_baseURL) );
	// 		}
	// 	} catch(Exception e) {
	// 		throw new RuntimeException(e);
	// 	}
	// }
	// 
	// /// Sends message
	// public void sendMessage(String message) {
	// 	this.websocketSession.getAsyncRemote().sendText(message);
	// }
	// 
	// /// Actual onOpen reciever for the socket protocol
	// @OnOpen
	// public void _onOpen(Session userSession) {
	// 	this.websocketSession = userSession;
	// }
	// 
	// /// Actual onClose reciever for the socket protocol
	// @OnClose
	// public void _onClose(Session userSession, CloseReason reason) {
	// 	this.websocketSession = null;
	// }
	// 
	// @OnMessage
	// public void _onMessage(String message) {
	// 	if( _sendAndWait_block ) {
	// 		_sendAndWait_message = message;
	// 		_sendAndWait_block = false;
	// 	} else if (this.websocketMessageHandler != null) {
	// 		this.websocketMessageHandler.accept(message);
	// 	}
	// }
	// 
	// ///////////////////////////////////////////////////////////
	// // Static core functions?
	// ///////////////////////////////////////////////////////////
	// protected static ResponseHttp asyncHttp_raw(
	// 	HttpRequestType requestType, 
	// 	String requestURL, //Request URL, with GET parameters if needed
	// 	Map<String, String> headers,
	// 	Map<String, String[]> postParams
	// ) {
	// 	AsyncHttpClient.BoundRequestBuilder req = RequestHttpUtils.asyncHttpClient_fromRequestType( requestType, requestURL, true );
	// 	
	// 	//set headers
	// 	if(headers != null && headers.size() > 0) {
	// 		for(String key : headers.keySet()){
	// 			req.addHeader(key,  headers.get(key));
	// 		}
	// 	}
	// 	
	// 	//set form params
	// 	if(postParams != null && postParams.size() > 0){
	// 		List<Param> formParams = new ArrayList<Param>();
	// 		for(String postKey : postParams.keySet()){
	// 			String[] postVals = postParams.get(postKey);
	// 			for(String postVal : postVals){
	// 				formParams.add(new Param(postKey, postVal));
	// 			}
	// 		}
	// 		req.setFormParams(formParams);
	// 	}
	// 	
	// 	StreamBuffer sb = new StreamBuffer();
	// 	OutputStream os = sb.getOutputStream();
	// 	InputStream is = sb.getInputStream();
	// 	
	// 	final ResponseHttp ret = new ResponseHttp();
	// 	ret.setInputStream(is);
	// 	
	// 	final ArrayList<HttpResponseBodyPart> bodyParts = new ArrayList<HttpResponseBodyPart>();
	// 	
	// 	AsyncHandler<ResponseHttp> asyncHandler = new AsyncHandler<ResponseHttp>() {
	// 		@Override
	// 		public STATE onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
	// 			byte[] bytes = content.getBodyPartBytes();
	// 			
	// 			try{
	// 				os.write(bytes);
	// 				os.flush();
	// 			} catch (Exception ex){
	// 				throw new RuntimeException(ex);
	// 			}
	// 			
	// 			ret.completedHeaders.compareAndSet(false, true);
	// 			return STATE.CONTINUE;
	// 		}
	// 		
	// 		@Override
	// 		public STATE onStatusReceived(final HttpResponseStatus status) throws Exception {
	// 			ret.setStatusCode(status.getStatusCode());
	// 			ret.setResponseStatus(status);
	// 			return STATE.CONTINUE;
	// 		}
	// 		
	// 		@Override
	// 		public STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
	// 			ret.setHeaders(headers.getHeaders());
	// 			return STATE.CONTINUE;
	// 		}
	// 		
	// 		@Override
	// 		public void onThrowable(Throwable t) {
	// 			throw new RuntimeException(t);
	// 		}
	// 		
	// 		@Override
	// 		public ResponseHttp onCompleted() throws Exception {
	// 			sb.close();
	// 			ret.completedHeaders.compareAndSet(false, true);
	// 			
	// 			return ret;
	// 		}
	// 	};
	// 	
	// 	ret.completedResponse = req.execute(asyncHandler);
	// 	return ret;
	// }
	
	// public static ResponseHttp get(String requestURL, Map<String, String> headers) throws IOException {
	// 	return asyncHttp_raw( HttpRequestType.GET, requestURL, headers, null );
	// }
	// 
	// /// Performs a basic post request
	// public static ResponseHttp post( String requestURL, Map<String,String[]> postMap ) throws IOException {
	// 	return asyncHttp_raw(HttpRequestType.POST, requestURL, null, postMap);
	// }
	// 
	// public static ResponseHttp post( String requestURL, Map<String, String> headers, Map<String,String[]> postMap ) throws IOException {
	// 	return asyncHttp_raw(HttpRequestType.POST, requestURL, headers, postMap);
	// }
	
	//--------------------------------------------------------
	// X request operations
	//--------------------------------------------------------
	
	/// Performs GET request with parameters, cookies and headers
	public static ResponseHttp byType( //
		HttpRequestType requestType, //
		String requestURL, //
		Map<String,String[]> parametersMap, // 
		Map<String,String[]> cookiesMap, // 
		Map<String,String[]> headersMap // 
	) { //
		return RequestHttp_apache.callRequest( requestType, requestURL, parametersMap, cookiesMap, headersMap );
	}
	
	//--------------------------------------------------------
	// GET request operations
	//--------------------------------------------------------
	
	/// Performs GET request : in the most basic form
	public static ResponseHttp get( String requestURL ) {
		return byType( HttpRequestType.GET, requestURL, null, null, null );
	}
	
	/// Performs GET request : with parameters
	public static ResponseHttp get( String requestURL, Map<String,String[]> parametersMap ) {
		return byType( HttpRequestType.GET, requestURL, parametersMap, null, null );
	}
	
	/// Performs GET request with parameters, cookies and headers
	public static ResponseHttp get( //
		String requestURL, //
		Map<String,String[]> parametersMap, // 
		Map<String,String[]> cookiesMap, // 
		Map<String,String[]> headersMap // 
	) { //
		return byType( HttpRequestType.GET, requestURL, parametersMap, cookiesMap, headersMap );
	}
	
	//--------------------------------------------------------
	// POST request operations
	//--------------------------------------------------------
	
	/// Performs (form) POST request : with parameters
	public static ResponseHttp post( String requestURL, Map<String,String[]> parametersMap ) {
		return byType( HttpRequestType.POST, requestURL, parametersMap, null, null );
	}
	
	/// Performs (form) POST request with parameters, cookies and headers
	public static ResponseHttp post( //
		String requestURL, //
		Map<String,String[]> parametersMap, // 
		Map<String,String[]> cookiesMap, // 
		Map<String,String[]> headersMap // 
	) { //
		return byType( HttpRequestType.POST, requestURL, parametersMap, cookiesMap, headersMap );
	}
	
	//--------------------------------------------------------
	// PUT request operations
	//--------------------------------------------------------
	
	/// Performs (form) PUT request : with parameters
	public static ResponseHttp put( String requestURL, Map<String,String[]> parametersMap ) {
		return byType( HttpRequestType.PUT, requestURL, parametersMap, null, null );
	}
	
	/// Performs (form) PUT request with parameters, cookies and headers
	public static ResponseHttp put( //
		String requestURL, //
		Map<String,String[]> parametersMap, // 
		Map<String,String[]> cookiesMap, // 
		Map<String,String[]> headersMap // 
	) { //
		return byType( HttpRequestType.PUT, requestURL, parametersMap, cookiesMap, headersMap );
	}
	
	//--------------------------------------------------------
	// PUT request operations
	//--------------------------------------------------------
	
	/// Performs delete request
	public static ResponseHttp delete( String requestURL ) {
		return byType( HttpRequestType.DELETE, requestURL, null, null, null );
	}
	
	/// Performs DELETE request : with parameters
	/// Note: parameters are treated the same way as GET request
	public static ResponseHttp delete( String requestURL, Map<String,String[]> parametersMap ) {
		return byType( HttpRequestType.DELETE, requestURL, parametersMap, null, null );
	}
	
	/// Performs delete request with parameters, cookies and headers
	/// Note: parameters are treated the same way as GET request
	public static ResponseHttp delete( //
		String requestURL, //
		Map<String,String[]> parametersMap, // 
		Map<String,String[]> cookiesMap, // 
		Map<String,String[]> headersMap // 
	) { //
		return byType( HttpRequestType.DELETE, requestURL, parametersMap, cookiesMap, headersMap );
	}
}
