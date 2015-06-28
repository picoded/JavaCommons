package picoded.webUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
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
import picoded.conv.StringEscape;

public class RequestHttpUtils {
	
	public static org.apache.http.client.methods.HttpUriRequest apache_HttpUriRequest_fromRequestType( HttpRequestType reqType, String reqURL ) {
		switch (reqType) {
			case TYPE_GET:
				return new HttpGet(reqURL);
			case TYPE_POST:
				return new HttpPost(reqURL);
			case TYPE_PUT:
				return new HttpPut(reqURL);
			case TYPE_DELETE:
				return new HttpDelete(reqURL);
			case TYPE_OPTION:
				return new HttpOptions(reqURL);
		}
		
		return null;
	}
	
	public static List<NameValuePair> parameterMapToList( Map<String, String[]> inMap ) {
		// Create a List to hold the NameValuePairs to be passed to the PostMethod
		List<NameValuePair> listNameValuePairs = new ArrayList<NameValuePair>();

		// Iterate the parameter names
		for (String stringParameterName : inMap.keySet()) {
			// Iterate the values for each parameter name
			String[] stringArrayParameterValues = inMap.get(stringParameterName);
			for (String stringParamterValue : stringArrayParameterValues) {
				// Create a NameValuePair and store in list
				NameValuePair nameValuePair = new BasicNameValuePair(stringParameterName, stringParamterValue);
				listNameValuePairs.add(nameValuePair);
			}
		}
		
		return listNameValuePairs;
	}
	
}
