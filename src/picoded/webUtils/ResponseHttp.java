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


public class ResponseHttp {
	
	protected HttpResponse response = null;
	protected ResponseHttp(HttpResponse inResponse) {
		response = inResponse;
	}
	
	/// Gets the response code
	public int statusCode() {
		return response.getStatusLine().getStatusCode();
	}
	
	/// Gets the response content
	public InputStream inputStream() {
		try {
			return response.getEntity().getContent();
		} catch (Exception e) {
			throw new RuntimeException(e);
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
