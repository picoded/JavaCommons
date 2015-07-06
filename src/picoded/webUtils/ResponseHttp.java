package picoded.webUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import picoded.webUtils.*;
import picoded.conv.*;

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

import com.ning.http.client.*;

public class ResponseHttp {
	
	///////////////////////////////////////////////////
	// Async Http Request mode
	///////////////////////////////////////////////////
	
	protected ResponseHttp() {
		
	}
	
	protected Throwable responseException = null;
	protected InputStream _inputStream = null;
	protected int _statusCode = -1;
	protected HttpResponseStatus _httpResponseStatus = null;
	protected Map<String, String> _headers = null;
	
	protected void throwIfResponseException() {
		if(responseException != null) {
			throw new RuntimeException(responseException);
		}
	}
	
	protected AtomicBoolean completedHeaders = new AtomicBoolean(false);
	protected ListenableFuture<ResponseHttp> completedResponse = null;
	
	public void waitForCompletedHeaders() {
		while( completedHeaders.get() == false ) {
			throwIfResponseException();
			Thread.yield();
		}
	}
	
	public void waitForCompletedRequest() {
		try {
			completedResponse.get();
		} catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}
	
	///////////////////////////////////////////////////
	// Apache HttpResponse mode
	///////////////////////////////////////////////////
	
	protected HttpResponse response = null;
	protected ResponseHttp(HttpResponse inResponse) {
		response = inResponse;
	}
	
	/// Gets the response code
	public int statusCode() {
		waitForCompletedHeaders();
		
		if(_statusCode != -1){
			return _statusCode;
		} else {
			return response.getStatusLine().getStatusCode();
		}
	}
	
	protected void setStatusCode(int statusCode) {
		_statusCode = statusCode;
	}
	
	public HttpResponseStatus responseStatus(){
		waitForCompletedHeaders();
		
		return _httpResponseStatus;
	}
	
	protected void setResponseStatus(HttpResponseStatus respStatus){
		_httpResponseStatus = respStatus;
	}
	
	/// Gets the response content
	public InputStream inputStream() {
		waitForCompletedHeaders();
		
		if(_inputStream != null){
			return _inputStream;
		} else {
			try {
				return response.getEntity().getContent();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	protected void setInputStream(InputStream is){
		//write to inputstream here
		_inputStream = is;
	}
	
	public Map<String, String> getHeaders(){
		return _headers;
	}
	
	protected void setHeaders(FluentCaseInsensitiveStringsMap headerMap){
		_headers = new HashMap<String, String>();
		
		if(headerMap != null && headerMap.size() > 0){
			for(String key : headerMap.keySet()){
				List<String> keyValueList = headerMap.get(key);
				StringBuilder strBuilder = new StringBuilder();
				int keyValCount = keyValueList.size();
				for(int i = 0; i < keyValCount; ++i){
					strBuilder.append(keyValueList.get(i));
					if(keyValCount > 1 && i < keyValCount - 1){
						strBuilder.append(",");
					}
				}
				
				_headers.put(key, strBuilder.toString());
			}
		}
	}
	
	/// Gets the response content as a string
	/// @TODO get the response encoding type, and pass "toString"
	private String _cachedString = null;
	public String toString() {
		if( _cachedString != null ) {
			return _cachedString;
		}
		
		try {
			return (_cachedString = IOUtils.toString(inputStream())); //, encoding 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Converts the result string into a map, via JSON's
	public Map<String,Object> toMap() {
		String r = toString();
		if( r == null || r.length() <= 1 ) {
			return null;
		}
		return ConvertJSON.toMap( r );
	}
}
