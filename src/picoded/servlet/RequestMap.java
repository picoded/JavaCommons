package picoded.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.collections4.map.AbstractMapDecorator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import picoded.core.struct.GenericConvertMap;
import picoded.core.conv.ConvertJSON;

/**
 * Class map, that handles the request map, and does the conversion between request arrays, and request values
 *
 * Also implments teh generic convert class
 *
 * @TODO: Optimize the class to do the conversion between String[] to String only ON DEMAND, and to cache the result
 **/
public class RequestMap extends AbstractMapDecorator<String, Object> implements
	GenericConvertMap<String, Object> {
	
	// Upload settings (for now)
	//------------------------------------------------------------------------------
	private static final int MEMORY_THRESHOLD = 1024 * 1024 * 4; // 4MB
	
	//
	// The following is ignored, as MAX request size should be configured by server not application
	//
	// private static final int MAX_FILE_SIZE = 1024 * 1024 * 40; // 40MB
	// private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // 50MB
	
	//------------------------------------------------------------------------------
	// Constructor
	//------------------------------------------------------------------------------
	
	/**
	 * blank constructor
	 **/
	public RequestMap() {
		super(new HashMap<String, Object>());
	}
	
	/**
	 * basic proxy constructor
	 **/
	public RequestMap(Map<String, Object> proxy) {
		super((proxy != null) ? proxy : new HashMap<String, Object>());
	}
	
	/**
	 * basic proxy constructor
	 **/
	public RequestMap(HttpServletRequest req) {
		super(mapConvert(req.getParameterMap()));
		multipartProcessing(req);
	}
	
	/**
	 * http map proxy builder
	 **/
	protected static RequestMap fromStringArrayValueMap(Map<String, String[]> proxy) {
		return new RequestMap(mapConvert(proxy));
	}
	
	//------------------------------------------------------------------------------
	// Utility functions (internal)
	//------------------------------------------------------------------------------
	
	/**
	 * Does the conversion from string array to string,
	 * Used internally for all the map conversion
	 **/
	private static String stringFromArray(String[] in) {
		if (in == null || in.length == 0) {
			return null;
		}
		
		if (in.length == 1) {
			return in[0];
		}
		
		return ConvertJSON.fromList(Arrays.asList(in));
	}
	
	/**
	 * Does the conversion of a Map<String,String[]> to a Map<String,String>
	 **/
	private static Map<String, Object> mapConvert(Map<String, String[]> in) {
		HashMap<String, Object> ret = new HashMap<String, Object>();
		
		for (Map.Entry<String, String[]> entry : in.entrySet()) {
			ret.put(entry.getKey(), stringFromArray(entry.getValue()));
		}
		return ret;
	}
	
	/**
	 * Processes the multipart object
	 **/
	private boolean multipartProcessing(HttpServletRequest request) {
		
		// Only work when there is multi part
		if (!ServletFileUpload.isMultipartContent(request)) {
			return false;
		}
		
		// configures upload settings
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// sets memory threshold - beyond which files are stored in disk
		factory.setSizeThreshold(MEMORY_THRESHOLD);
		
		// sets temporary location to store files (Already done by default)
		// factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
		
		// Get the servlet file uploader handler class
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		//
		// The following is ignored, as MAX request size should be configured by server not application
		//
		// // sets maximum size of upload file
		// upload.setFileSizeMax(MAX_FILE_SIZE);
		// // sets maximum size of request (include file + form data)
		// upload.setSizeMax(MAX_REQUEST_SIZE);
		//
		
		try {
			@SuppressWarnings("unchecked")
			List<FileItem> formItems = upload.parseRequest(request);
			
			if (formItems != null && formItems.size() > 0) {
				for (FileItem item : formItems) {
					// Field name to handle
					String fieldname = item.getFieldName();
					
					// processes only fields that are not form fields
					if (item.isFormField()) {
						
						// Field value to populate OR append
						String fieldvalue = item.getString();
						
						// Get the cache
						Object cache = get(fieldname);
						
						// Insert the cache if null
						if (cache == null) {
							// Puts in directly
							put(fieldname, fieldvalue);
						} else {
							// Puts in as array?
							List<Object> listCache = getObjectList(fieldname, null);
							
							// Get the list representation instead
							if (listCache == null) {
								listCache = new ArrayList<Object>();
								listCache.add(cache); // Add the previous value as first in list
							}
							
							// List append the new value
							listCache.add(fieldvalue);
							
							// Put the modified the list
							put(fieldname, listCache);
						}
					} else {
						
						//
						// Get the fileArray cache
						//
						Object cache = get(fieldname);
						RequestFileArray fileArray = null;
						if (cache == null || !(cache instanceof RequestFileArray)) {
							fileArray = new RequestFileArray();
						} else {
							fileArray = (RequestFileArray) cache;
						}
						
						//
						// Import the item in
						//
						fileArray.importFileItem(item);
						
						//
						// Save the file map
						//
						put(fieldname, fileArray);
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		// Finish processing
		return true;
	}
	
}
