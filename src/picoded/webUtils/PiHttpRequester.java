package picoded.webUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.cookie.*;
import org.apache.http.entity.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.cookie.*;
import org.apache.http.protocol.*;
import org.apache.http.client.protocol.*;

import picoded.servlet.CorePage;
import picoded.webUtils.HttpRequestType;

public class PiHttpRequester{
	
	public PiHttpResponse sendRequest(String url, HttpRequestType httpRequestType, Map<String, String> headerMap, Map<String, String> cookieMap, String requestBody){
		HttpClient httpClient = HttpClients.createDefault();
		
		BasicClientCookie clientCookie = generateCookie(cookieMap);
		
		CookieStore httpCookieStore = new BasicCookieStore();
		httpCookieStore.addCookie(clientCookie);
		
		HttpContext localContext = new BasicHttpContext();
		localContext.setAttribute(HttpClientContext.COOKIE_STORE, httpCookieStore);
		
		HttpRequestBase httpRequest = null;
		//this needs to be brought up with eugene
		if(httpRequestType == HttpRequestType.TYPE_GET){
			httpRequest = generateGetRequest(url, headerMap, cookieMap);
		}
		else if(httpRequestType == HttpRequestType.TYPE_POST){
			httpRequest = generatePostRequest(url, headerMap, cookieMap, requestBody);
		}
		
		PiHttpResponse piHttpResponse = null;
		
		
		
		try {
			HttpResponse resp = httpClient.execute(httpRequest, localContext);
			
			System.out.println("PiHttpRequester received response: " +resp);
			
			
			System.out.println("Cookie Store: " +httpCookieStore.getCookies());
			
			
			//possible more secure way to get cookies
			HashMap<String, String> piCookies = new HashMap<String, String>();
			for(Cookie cookie : httpCookieStore.getCookies()){
				piCookies.put(cookie.getName(), cookie.getValue());
			}
			
			//get headers
			HashMap<String, String> piHeaders = new HashMap<String, String>();
			for(Header header : resp.getAllHeaders()){
				piHeaders.put(header.getName(), header.getValue());
			}
			
			
			
			HttpEntity respEntity = resp.getEntity();
			InputStream respResponseBody = respEntity.getContent();
			
			piHttpResponse = new PiHttpResponse(piHeaders, piCookies, respResponseBody);
			
		} catch (ClientProtocolException ex) {
			
		} catch (IOException ex) {
			
		} finally {
			
		}
		
		
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
	
	private HttpGet generateGetRequest(String url, Map<String, String> headerMap, Map<String, String> cookieMap){
		HttpGet httpGet = new HttpGet(url);
		
		//append headerMap
		if(headerMap != null && headerMap.size() > 0){
			for(Entry<String, String> kvp : headerMap.entrySet()){
				httpGet.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		
		//append cookieMap
		if(cookieMap != null && cookieMap.size() > 0){
			for(Entry<String, String> kvp : cookieMap.entrySet()){
				httpGet.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		
		return httpGet;
	}
	
	private HttpPost generatePostRequest(String url, Map<String, String> headerMap, Map<String, String> cookieMap, String requestBody){
		HttpPost httpPost = new HttpPost(url);
		BasicHttpEntity postRequestBody = new BasicHttpEntity();
		InputStream is = IOUtils.toInputStream(requestBody);
		postRequestBody.setContent(is);
		
		//append headerMap
		if(headerMap != null && headerMap.size() > 0){
			for(Entry<String, String> kvp : headerMap.entrySet()){
				httpPost.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		
		//append cookieMap
		if(cookieMap != null && cookieMap.size() > 0){
			for(Entry<String, String> kvp : cookieMap.entrySet()){
				httpPost.addHeader(kvp.getKey(), kvp.getValue());
			}
		}
		
		return httpPost;
	}
}