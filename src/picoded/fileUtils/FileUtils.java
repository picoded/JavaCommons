package picoded.fileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.StringUtils;

public class FileUtils extends org.apache.commons.io.FileUtils{
	
	public static List<String> getFileNamesFromFolder(File inFile, String separator, String rootFolderName){
		List<String> keyList = new ArrayList<String>();
		
		if(StringUtils.isNullOrEmpty(rootFolderName)){
			rootFolderName = "";
		}
		
		if(StringUtils.isNullOrEmpty(separator)){
			separator = "/";
		}
		
		if(inFile.isDirectory()){
			File[] innerFiles = inFile.listFiles();
			for(File innerFile:innerFiles){
				if(innerFile.isDirectory()){
					String parentFolderName = innerFile.getName();
					if(!rootFolderName.isEmpty()){
						parentFolderName = rootFolderName + separator + parentFolderName;
					}
					keyList.addAll(getFileNamesFromFolder(innerFile, parentFolderName, separator));
				}else{
					keyList.addAll(getFileNamesFromFolder(innerFile, rootFolderName, separator));
				}
			}
		}else{
			String fileName = inFile.getName();
			fileName = fileName.substring(0, fileName.lastIndexOf('.'));
			String prefix = "";
			if(!rootFolderName.isEmpty()){
				prefix += rootFolderName + separator;
			}
			
			keyList.add(prefix + fileName);
		}
		
		return keyList;
	}
	
}