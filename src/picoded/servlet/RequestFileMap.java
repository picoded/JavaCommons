package picoded.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Collection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.collections4.map.AbstractMapDecorator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItem;

import picoded.struct.GenericConvertMap;
import picoded.struct.GenericConvertHashMap;
import picoded.struct.UnsupportedDefaultMap;
import picoded.conv.ConvertJSON;

///
/// Class map, that handles file uploads in RequestMap
///
public class RequestFileMap extends GenericConvertHashMap<String, String>  {
	
	/// Inner DiskFileItem mapping
	protected Map<String,DiskFileItem> diskItemMap = new HashMap<String,DiskFileItem>();
	
	/// Adds a FileItem part
	public void importFileItem(FileItem item) throws IOException{
		
		// In case apache breaks a future version
		if( !(item instanceof DiskFileItem)) {
			throw new RuntimeException("Currently only DiskFileItem is supported");
		}
		
		// Get the disk item
		DiskFileItem diskItem = (DiskFileItem)item;
		
		// filepath
		String filepath = diskItem.getName();
		
		// Transfer to local diskItemMap storage
		diskItemMap.put(filepath, diskItem);
		
		// Get the output stream
		put(filepath, "");
	}
	
	/// Mirrors to diskItemMap
	public void writeToFile(String filePath, File file) {
		try {
			if( diskItemMap.get(filePath) != null ) {
				diskItemMap.get(filePath).write(file);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Converts stream to byyte array
	public InputStream getInputStream(String filePath) {
		try {
			if( diskItemMap.get(filePath) != null ) {
				return diskItemMap.get(filePath).getInputStream();
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/// Converts stream to byyte array
	public byte[] getByteArray(String filePath) {
		if( diskItemMap.get(filePath) != null ) {
			return diskItemMap.get(filePath).get();
		}
		return null;
	}
	
	/// Converts to string object
	public String getString(String filePath) {
		try {
			if( diskItemMap.get(filePath) != null ) {
				return diskItemMap.get(filePath).getString("UTF-8");
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/// Fetch the string object
	public String get(Object key) {
		return getString(key.toString());
	}
}
