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


public class RequestHttp {
	
	/// Performs a raw HTTP request, this assumes that the function caller has fully handled
	/// all the various basic aspects of the request, like GET parameters, etc.
	public static ResponseHttp raw(
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
		
		// Call and return
		HttpResponse response = httpClient.execute(methodToMakeRequest);
		
		return new ResponseHttp(response);
	}
	
	/// Performs the most basic of get requests
	public static ResponseHttp get( String requestURL ) throws IOException {
		return raw(HttpRequestType.TYPE_GET, requestURL, null, null);
	}
	
}
