package picoded.webUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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

import com.ning.http.client.*;

import picoded.struct.StreamBuffer;
import picoded.struct.StreamBuffer.SBInputStream;
import picoded.struct.StreamBuffer.SBOutputStream;

import picoded.webUtils._RequestHttp.RequestHttp_apache;

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
	
	/// Response message
	public void addMessageHandler(Void_String msgHandler) {
		this.websocketMessageHandler = msgHandler;
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
	
	/// Time out in milliseconds?
	public String sendAndWait(String message, long timeout) {
		throw new RuntimeException("TO IMPLEMENT");
	}
	
	///////////////////////////////////////////////////////////
	// Core websocket functions
	///////////////////////////////////////////////////////////
	
	/// Ensures that the websocket is connected
	public void websocketConnect() {
		try {
			if (container == null) {
				container = ContainerProvider.getWebSocketContainer();
			}
			
			if (websocketSession == null) {
				websocketSession = container.connectToServer(this, new URI(_baseURL));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Sends message
	public void sendMessage(String message) {
		this.websocketSession.getAsyncRemote().sendText(message);
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
		if (_sendAndWait_block) {
			_sendAndWait_message = message;
			_sendAndWait_block = false;
		} else if (this.websocketMessageHandler != null) {
			this.websocketMessageHandler.accept(message);
		}
	}
	
	///////////////////////////////////////////////////////////
	// Static core functions?
	///////////////////////////////////////////////////////////
	protected static ResponseHttp asyncHttp_raw(HttpRequestType requestType, String requestURL, //Request URL, with GET parameters if needed
		Map<String, String> headers, Map<String, String[]> postParams) {
		AsyncHttpClient.BoundRequestBuilder req = RequestHttpUtils.asyncHttpClient_fromRequestType(requestType,
			requestURL, true);
		
		//set headers
		if (headers != null && headers.size() > 0) {
			for (String key : headers.keySet()) {
				req.addHeader(key, headers.get(key));
			}
		}
		
		//set form params
		if (postParams != null && postParams.size() > 0) {
			List<Param> formParams = new ArrayList<Param>();
			for (String postKey : postParams.keySet()) {
				String[] postVals = postParams.get(postKey);
				for (String postVal : postVals) {
					formParams.add(new Param(postKey, postVal));
				}
			}
			req.setFormParams(formParams);
		}
		
		StreamBuffer sb = new StreamBuffer();
		OutputStream os = sb.getOutputStream();
		InputStream is = sb.getInputStream();
		
		final ResponseHttp ret = new ResponseHttp();
		ret.setInputStream(is);
		
		final ArrayList<HttpResponseBodyPart> bodyParts = new ArrayList<HttpResponseBodyPart>();
		
		AsyncHandler<ResponseHttp> asyncHandler = new AsyncHandler<ResponseHttp>() {
			@Override
			public STATE onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
				byte[] bytes = content.getBodyPartBytes();
				
				try {
					os.write(bytes);
					os.flush();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				
				ret.completedHeaders.compareAndSet(false, true);
				return STATE.CONTINUE;
			}
			
			@Override
			public STATE onStatusReceived(final HttpResponseStatus status) throws Exception {
				ret.setStatusCode(status.getStatusCode());
				ret.setResponseStatus(status);
				return STATE.CONTINUE;
			}
			
			@Override
			public STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
				ret.setHeaders(headers.getHeaders());
				return STATE.CONTINUE;
			}
			
			@Override
			public void onThrowable(Throwable t) {
				throw new RuntimeException(t);
			}
			
			@Override
			public ResponseHttp onCompleted() throws Exception {
				sb.close();
				ret.completedHeaders.compareAndSet(false, true);
				
				return ret;
			}
		};
		
		ret.completedResponse = req.execute(asyncHandler);
		return ret;
	}
	
	public static ResponseHttp get(String requestURL, Map<String, String> headers) throws IOException {
		return asyncHttp_raw(HttpRequestType.GET, requestURL, headers, null);
	}
	
	/// Performs a basic post request
	public static ResponseHttp post(String requestURL, Map<String, String[]> postMap) throws IOException {
		return asyncHttp_raw(HttpRequestType.POST, requestURL, null, postMap);
	}
	
	public static ResponseHttp post(String requestURL, Map<String, String> headers, Map<String, String[]> postMap)
		throws IOException {
		return asyncHttp_raw(HttpRequestType.POST, requestURL, headers, postMap);
	}
	
}
