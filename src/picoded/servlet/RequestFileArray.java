package picoded.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.collections4.map.AbstractMapDecorator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItem;

import picoded.struct.GenericConvertList;
import picoded.struct.GenericConvertArrayList;
import picoded.struct.UnsupportedDefaultMap;
import picoded.core.conv.ConvertJSON;

/**
* Class map, that handles file uploads in a RequestList
**/
public class RequestFileArray extends GenericConvertArrayList<String> {

	/**
	* Inner DiskFileItem mapping
	**/
	protected List<DiskFileItem> diskItemList = new ArrayList<DiskFileItem>();

	/**
	* Adds a FileItem part to the end of the array
	*
	* @param  FileItem to add
	**/
	protected void importFileItem(FileItem item) throws IOException {

		// In case apache breaks a future version
		if (!(item instanceof DiskFileItem)) {
			throw new RuntimeException("Currently only DiskFileItem is supported");
		}

		// Get the disk item
		DiskFileItem diskItem = (DiskFileItem) item;

		// Transfer to local diskItemList storage
		diskItemList.add(diskItem);

		// Add to the representing list
		add("");
	}

	/**
	* Get the underlying apache FileItem implementation
	*
	* @param  Array index
	*
	* @return  The org.apache.commons.fileupload.disk.DiskFileItem
	**/
	public FileItem getFileItem(int idx) {
		return diskItemList.get(idx);
	}

	/**
	* Writes the file content to another file
	*
	* @param  Array index
	* @param  File to write into
	**/
	public void writeToFile(int idx, File file) {
		try {
			if (diskItemList.get(idx) != null) {
				diskItemList.get(idx).write(file);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	* Get the declared file name (if provided)
	*
	* @param  Array index
	*
	* @return  String representing the declared file name (maybe null)
	**/
	public String getName(int idx) {
		try {
			if (diskItemList.get(idx) != null) {
				return diskItemList.get(idx).getName();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	* Get the raw input stream
	*
	* @param  Array index
	*
	* @return Input Stream representing the file
	**/
	public InputStream getInputStream(int idx) {
		try {
			if (diskItemList.get(idx) != null) {
				return diskItemList.get(idx).getInputStream();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	* Get the raw byte array.
	*
	* For extremely large files.
	* Please use getInputStream or writeToFile instead
	*
	* @param  Array index
	*
	* @return Byte Arra representing the file
	**/
	public byte[] getByteArray(int idx) {
		if (diskItemList.get(idx) != null) {
			return diskItemList.get(idx).get();
		}
		return null;
	}

	/**
	* Gets the file, as a UTF-8 decoded string
	*
	* For extremely large files.
	* Please use getInputStream or writeToFile instead
	*
	* @param  Array index
	*
	* @return  String representing the file
	**/
	public String get(int idx) {
		try {
			// Result was previously stored
			String cachedResult = super.get(idx);
			if (cachedResult != null && cachedResult.length() > 0) {
				return cachedResult;
			}

			// Get and cache result
			if (diskItemList.get(idx) != null) {
				cachedResult = diskItemList.get(idx).getString("UTF-8");
			}
			set(idx, cachedResult);
			return cachedResult;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	* Load all the values into a string format
	* This is required for entrySet / values iteration
	**/
	private void loadAll() {
		for (int i = 0; i < diskItemList.size(); ++i) {
			getString(i);
		}
	}

	/**
	* Implmentation of List.iterator()
	**/
	public Iterator<String> iterator() {
		loadAll();
		return super.iterator();
	}

	/**
	* Implmentation of List.listIterator()
	**/
	public ListIterator<String> listIterator() {
		loadAll();
		return super.listIterator();
	}

	/**
	* Implmentation of List.listIterator()
	**/
	public ListIterator<String> listIterator(int index) {
		loadAll();
		return super.listIterator(index);
	}
}
