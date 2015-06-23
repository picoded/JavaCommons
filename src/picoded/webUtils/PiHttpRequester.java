package picoded.webUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.cookie.*;
import org.apache.http.entity.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.cookie.*;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.protocol.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.amazonaws.util.StringUtils;

import picoded.servlet.CorePage;
import picoded.webUtils.HttpRequestType;

public class PiHttpRequester{
	
	public PiHttpResponse sendGetRequest(String requestHostName, 
											String context, 
											Map<String, String> getParams, 
											Map<String, String> headerMap, 
											Map<String, String> cookieMap)
	{
		return sendRequest(HttpRequestType.TYPE_GET, requestHostName, context, getParams, null, null, headerMap, cookieMap);
	}
	
	public PiHttpResponse sendPostRequest(String requestHostName, 
											String context,
											Map<String, String> postParams,
											Map<String, String> headerMap, 
											Map<String, String> cookieMap)
	{
		return sendRequest(HttpRequestType.TYPE_POST, requestHostName, context, null, postParams, null, headerMap, cookieMap);
	}
	
	public PiHttpResponse sendPutRequest(String requestHostName, 
											String context,
											Map<String, String> putParams,
											Map<String, String> headerMap, 
											Map<String, String> cookieMap)
	{
		return sendRequest(HttpRequestType.TYPE_PUT, requestHostName, context, null, null, putParams, headerMap, cookieMap);
	}
	
	public PiHttpResponse sendDeleteRequest(String requestHostName, 
											String resourceToDeletePath,
											Map<String, String> headerMap, 
											Map<String, String> cookieMap)
	{
		return sendRequest(HttpRequestType.TYPE_DELETE, requestHostName, resourceToDeletePath, null, null, null, headerMap, cookieMap);
	}
	
	public PiHttpResponse sendRequest(HttpRequestType httpRequestType, 
										String requestHostName, 
										String contextPath, 
										Map<String, String> getParams,
										Map<String, String> postParams,
										Map<String, String> putParams,
										Map<String, String> headerMap, 
										Map<String, String> cookieMap)
	{
		HttpClient httpClient = HttpClients.createDefault();
		CookieStore httpCookieStore = generateCookieStore(cookieMap);
		
		//add cookie store to context for request
		HttpContext localContext = new BasicHttpContext();
		localContext.setAttribute(HttpClientContext.COOKIE_STORE, httpCookieStore);
		
		//create the request
		HttpRequestBase httpRequest = null;
		if(httpRequestType == HttpRequestType.TYPE_GET){
			httpRequest = generateGetRequest(requestHostName, contextPath, getParams, headerMap, cookieMap);
		} else if(httpRequestType == HttpRequestType.TYPE_POST){
			httpRequest = generatePostRequest(requestHostName, contextPath, postParams, headerMap, cookieMap);
		} else if(httpRequestType == HttpRequestType.TYPE_PUT) {
			httpRequest = generatePutRequest(requestHostName, contextPath, putParams, headerMap, cookieMap);
		} else if(httpRequestType == HttpRequestType.TYPE_DELETE) {
			httpRequest = generateDeleteRequest(requestHostName, contextPath, headerMap, cookieMap);
		}
		
		//execute request
		PiHttpResponse piHttpResponse = null;
		piHttpResponse = executeRequest(httpClient, httpRequest, localContext, httpCookieStore);
		
		return piHttpResponse;
	}
	
	private BasicClientCookie generateCookie(Map<String, String> cookieMap){
		BasicClientCookie cookie = null;
		
		if(cookieMap != null){
			Set<String> keys = cookieMap.keySet();
			int keyCount = keys.size();
			int count = 0;
			for(String key : keys){
				if(count == 0){
					cookie = new BasicClientCookie(key, cookieMap.get(key));
				}
				else{
					cookie.setAttribute(key, cookieMap.get(key));
				}
				++count;
			}
		}
		
		return cookie;
	}
	
	private CookieStore generateCookieStore(Map<String, String> cookieMap){
		BasicClientCookie clientCookie = generateCookie(cookieMap);
		CookieStore httpCookieStore = new BasicCookieStore();
		httpCookieStore.addCookie(clientCookie);
		return httpCookieStore;
	}
	
	private String generateGetParams(Map<String, String> getParams){
		StringBuilder sb = new StringBuilder();
		
		if(getParams != null && getParams.size() > 0){
			sb.append("?");
			for(Entry<String, String> kvp : getParams.entrySet()){
				sb.append(kvp.getKey() + "=" + kvp.getValue());
			}
		}
		
		return sb.toString();
	}
	
	private HttpGet generateGetRequest(String requestHostName, 
										String contextPath, 
										Map<String, String> getParams, 
										Map<String, String> headerMap, 
										Map<String, String> cookieMap)
	{
		String _httpParams = generateGetParams(getParams);
		HttpGet httpGet = new HttpGet(requestHostName + "/" + contextPath + _httpParams);
		
		if(headerMap != null && headerMap.size() > 0){
			for(Entry<String, String> kvp : headerMap.entrySet()){
				httpGet.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		if(cookieMap != null && cookieMap.size() > 0){
			for(Entry<String, String> kvp : cookieMap.entrySet()){
				httpGet.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		
		return httpGet;
	}
	
	private HttpPost generatePostRequest(String requestHostName, 
											String contextPath, 
											Map<String, String> postParams,
											Map<String, String> headerMap, 
											Map<String, String> cookieMap)
	{
		HttpPost httpPost = new HttpPost(requestHostName + "/" + contextPath);
		
		List<NameValuePair> requestPairs = new ArrayList<NameValuePair>();
		if(postParams != null){
			for(Entry<String, String> kvp : postParams.entrySet()){
				requestPairs.add(new BasicNameValuePair(kvp.getKey(), kvp.getValue()));
			}
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(requestPairs));
		} catch (UnsupportedEncodingException ex) {
			System.out.println("Unsupported Encoding Exception:" +ex.getMessage());
		}
		if(headerMap != null && headerMap.size() > 0){
			for(Entry<String, String> kvp : headerMap.entrySet()){
				httpPost.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		if(cookieMap != null && cookieMap.size() > 0){
			for(Entry<String, String> kvp : cookieMap.entrySet()){
				httpPost.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		
		return httpPost;
	}
	
	private HttpPut generatePutRequest(String requestHostName, 
										String contextPath, 
										Map<String, String> putParams, 
										Map<String, String> headerMap, 
										Map<String, String> cookieMap)
	{
		HttpPut httpPut = new HttpPut(requestHostName + "/" + contextPath);
		List<NameValuePair> requestPairs = new ArrayList<NameValuePair>();
		if(putParams != null){
			for(Entry<String, String> kvp : putParams.entrySet()){
				requestPairs.add(new BasicNameValuePair(kvp.getKey(), kvp.getValue()));
			}
		}
		try {
			httpPut.setEntity(new UrlEncodedFormEntity(requestPairs));
		} catch (UnsupportedEncodingException ex) {
			System.out.println("Unsupported Encoding Exception:" +ex.getMessage());
		}
		if(headerMap != null && headerMap.size() > 0){
			for(Entry<String, String> kvp : headerMap.entrySet()){
				httpPut.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		if(cookieMap != null && cookieMap.size() > 0){
			for(Entry<String, String> kvp : cookieMap.entrySet()){
				httpPut.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		
		return httpPut;
	}
	
	private HttpDelete generateDeleteRequest(String requestHostName, 
												String resourcePathToDelete,
												Map<String, String> headerMap, 
												Map<String, String> cookieMap)
	{
		HttpDelete httpDelete = new HttpDelete(requestHostName + "/" + resourcePathToDelete);
		if(headerMap != null && headerMap.size() > 0){
			for(Entry<String, String> kvp : headerMap.entrySet()){
				httpDelete.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		if(cookieMap != null && cookieMap.size() > 0){
			for(Entry<String, String> kvp : cookieMap.entrySet()){
				httpDelete.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		
		return httpDelete;
	}
	
	private PiHttpResponse executeRequest(HttpClient httpClient, HttpRequestBase httpRequest, HttpContext localContext, CookieStore httpCookieStore){
		HttpResponse resp = null;
		PiHttpResponse piHttpResponse = null;
		
		try {
			resp = httpClient.execute(httpRequest, localContext);
			
			HashMap<String, String> piCookies = null;
			HashMap<String, String> piHeaders = null;
			
			if(resp != null){
				piCookies = new HashMap<String, String>();
				piHeaders = new HashMap<String, String>();
				
				for(Cookie cookie : httpCookieStore.getCookies()){
					piCookies.put(cookie.getName(), cookie.getValue());
				}
				for(Header header : resp.getAllHeaders()){
					piHeaders.put(header.getName(), header.getValue());
				}
			}
			
			HttpEntity respEntity = resp.getEntity();
			InputStream respResponseBody = null;
			if(respEntity != null){
				respResponseBody = respEntity.getContent();
			}

			piHttpResponse = new PiHttpResponse(piHeaders, piCookies, respResponseBody);
			
		} catch (ClientProtocolException ex) {
			System.out.println("ClientProtocolException: " +ex.getMessage());
		} catch (IOException ex) {
			System.out.println("IOException: " +ex.getMessage());
		} catch (Exception ex) {
			System.out.println("Exception: " +ex.getMessage());
		} finally {
			
		}
		
		return piHttpResponse;
	}
}