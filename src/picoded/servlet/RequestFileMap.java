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
import picoded.conv.ConvertJSON;

///
/// Class map, that handles file uploads in RequestMap
///
public class RequestFileMap extends HashMap<String, InputStream>  {
	
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
		put(filepath, diskItem.getInputStream());
	}
	
	/// Mirrors to diskItemMap
	public void writeToFile(String filePath, File file) throws Exception {
		diskItemMap.get(filePath).write(file);
	}
	
}
