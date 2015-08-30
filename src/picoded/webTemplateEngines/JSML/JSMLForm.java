package picoded.webTemplateEngines.JSML;

import java.io.*;
import java.util.*;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.lesscss.deps.org.apache.commons.io.FileUtils;

import picoded.conv.ConvertJSON;
import picoded.conv.GUID;
import picoded.conv.MapValueConv;
import picoded.fileUtils.PDFGenerator;
import picoded.webTemplateEngines.FormGenerator.*;

public class JSMLForm {
	
	FormGenerator formGen = null;
	
	//folder will contain
	//formDeclare.json
	//formData.json (or metatable)
	//bodyPrefix.html
	//bodySuffix.html
	//formStyle.css
	
	private String _formDefinitionString = "";
	
	private String _formFolderPath = "";
	
	private String _resourceFolderPath = "";
	
	private String _contextPath = "";
	
	private String _contextIdentifier = "${FormContextPath}";
	
	private String _defaultResourceFolderName = "resources";
	
	private String _tempFolderName = "";
	
	private String _svgPrefix = "data:image/svg+xml;base64,";
	
	//file name suffixes
	private String _bodyPrefix = "prefix";
	
	private String _bodySuffix = "suffix";
	
	private String _styleSheetSuffix = "style";
	
	private Map<String, Object> _formDefinitionMap = null;
	
	public JSMLForm(){
		formGen = new FormGenerator();
	}
	
	public JSMLForm(File formFolder, String uriContext, File tmpFolder){
		formGen = new FormGenerator();
		
		setFormFolder(formFolder);
		setResourceFolder(tmpFolder);
		setContextPath(uriContext);
	}
	
	public JSMLForm(String formFolderPath, String uriContext, String tmpFolderPath){
		formGen = new FormGenerator();
		
		setFormFolder(formFolderPath);
		setResourceFolder(tmpFolderPath);
		setContextPath(uriContext);
	}
	
	public String formDefinitionString(){
		return _formDefinitionString;
	}
	
	public void setFormFolder(String inFormFolderPath){
		_formFolderPath = inFormFolderPath;
	}
	
	public void setFormFolder(File inFormFolder){
		_formFolderPath = inFormFolder.getPath();
	}
	
	public void setResourceFolder(String inResourceFolderPath){
		_resourceFolderPath = inResourceFolderPath;
		validateResourceFolder();
	}
	
	public void setResourceFolder(File inResourceFolder){
		_resourceFolderPath = inResourceFolder.getPath();
		validateResourceFolder();
	}
	
	public void setContextPath(String inContextPath){
		_contextPath = inContextPath;
	}
	
	private void validateResourceFolder(){
		if(_resourceFolderPath.isEmpty()){
			if(!_formFolderPath.isEmpty()){
				_resourceFolderPath = _formFolderPath + "/" + _defaultResourceFolderName;
			}else{
				throw new RuntimeException("validateResourceFolder() -> unable to validate resourcefolder without valid resource folder name");
			}
		}
		
		File resourceFile = new File(_resourceFolderPath);
		
		if(!resourceFile.exists()){
			resourceFile.mkdirs();
		}
	}
	
	public void getDefinition(){
		File declareFile = new File(_formFolderPath + "/formDeclare.json");
		
		if(declareFile.exists()){
			try{
				String declareFileString = FileUtils.readFileToString(declareFile);
				setDefinition(declareFileString);
			}catch(Exception e){
				throw new RuntimeException("getDefinition() -> "+e.getMessage());
			}
		}
	}
	
	public void setDefinition(String inFormDefinition){
		_formDefinitionString = sanitiseString(inFormDefinition, "");
		
	}
	
	public void setDefinition(Map<String, Object> inFormDefinition){
		_formDefinitionMap = sanitiseMap(inFormDefinition);
	}
	
	public Map<String, Object> getBlankData(){
		File blankDataFile = new File(_formFolderPath + "/blankData.json");
		String blankDataString = "";
		try{
			blankDataString = FileUtils.readFileToString(blankDataFile);
		}catch(Exception e){
			throw new RuntimeException("getBlankData() ->" + e.getMessage());
		}
		
		Map<String, Object> ret =  ConvertJSON.toMap(blankDataString);
		return ret;
	}
	
	public Map<String, Object> getDummyData(){
		File dummyDataFile = new File(_formFolderPath + "/dummyData.json");
		String dummyDataString = "";
		try{
			dummyDataString = FileUtils.readFileToString(dummyDataFile);
		}catch(Exception e){
			throw new RuntimeException("getDummyData() ->" + e.getMessage());
		}
		
		Map<String, Object> ret =  ConvertJSON.toMap(dummyDataString);
		return ret;
	}
	
	private File[] getFilesInRootFolder(){
		File rootFolder = new File(_formFolderPath);
		if(rootFolder.exists()){
			return rootFolder.listFiles();
		}else{
			return null;
		}
	}
	
	private File getFileInRootFolder(String fileWord){
		File[] files = getFilesInRootFolder();
		if(files != null && files.length > 0){
			for(File file : files){
				if(file.getName().toLowerCase().contains(fileWord.toLowerCase())){
					return file;
				}
			}
			return null;
		}else{
			return null;
		}
	}
	
	public String readBodyPrefix(String prefixName){
		File bodyPrefixFile = getFileInRootFolder(prefixName);
		try{
			String bodyPrefixString = FileUtils.readFileToString(bodyPrefixFile);
			return bodyPrefixString;
		}catch(Exception e){
			throw new RuntimeException("readBodyPrefix() -> " + e.getMessage());
		}
	}
	
	public String readBodySuffix(String suffixName){
		File bodySuffixFile = getFileInRootFolder(suffixName);
		try{
			String bodySuffixString = FileUtils.readFileToString(bodySuffixFile);
			return bodySuffixString;
		}catch(Exception e){
			throw new RuntimeException("readBodySuffix() -> " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> sanitiseMap(Map<String, Object> inMap){
		if(inMap == null){
			return inMap;
		}
		Map<String, Object> tempMap = new HashMap<String, Object>(inMap);
		for(String key : inMap.keySet()){
			Object value = inMap.get(key);
			
			if(value instanceof String){
				tempMap.replace(key, sanitiseString((String)value, key));
			}else if(value instanceof List){
				tempMap.replace(key,  sanitiseList((List<Object>)value));
			}else if(value instanceof Map){
				tempMap.replace(key, sanitiseMap((Map<String, Object>)value));
			}
		}
		
		return tempMap;
	}
	
	@SuppressWarnings("unchecked")
	protected List<Object> sanitiseList(List<Object> inList){
		List<Object> tempList = new ArrayList<Object>(inList);
		
		for(int i = 0; i < inList.size(); ++i){
			if(inList.get(i) instanceof String){
				tempList.remove(i);
				tempList.add(i, sanitiseString((String)inList.get(i), ""));
			}else if(inList.get(i) instanceof List){
				tempList.remove(i);
				tempList.add(i, sanitiseList((List<Object>)inList.get(i)));
			}else if(inList.get(i) instanceof Map){
				tempList.remove(i);
				tempList.add(i, sanitiseMap((Map<String, Object>)inList.get(i)));
			}
		}
		
		return tempList;
	}
	
	protected String sanitiseString(String inString, String name){
		String tempString = inString;
		if(tempString.contains(_contextIdentifier)){
			tempString = tempString.replace(_contextIdentifier, _contextPath);
		}
		
		if(tempString.contains(_svgPrefix)){
			tempString = tempString.substring(_svgPrefix.length(), tempString.length());
			
			String svgPrefix = "inSig_";
			String pngPrefix = "outSig_";
			String svgFilePath = _contextPath + "/"+svgPrefix+name+".svg";
			String pngFilePath = _contextPath + "/"+pngPrefix+name+".png";
			
			try{
				svgFileToPngFile(svgFilePath, pngFilePath);
				//after this, pass the signature path back
			}catch(Exception ex){
				
			}
			
			tempString = pngFilePath;
		}
		
		return tempString;
	}
	
	protected String sanitiseStringForPDF(String inString, String name){
		String tempString = inString;
		if(tempString.contains(_contextIdentifier)){
			tempString = tempString.replace(_contextIdentifier+"/", _formFolderPath+"/");
			tempString = tempString.replace(_contextIdentifier, _formFolderPath+"/");
		}
		
		return tempString;
	}
	
	private void svgFileToPngFile( String svgPath, String pngPath ) throws IOException {
		try {
			InputStream svg_istream = new FileInputStream(svgPath);
			TranscoderInput input_svg_image = new TranscoderInput(svg_istream);
			//Step-2: Define OutputStream to PNG Image and attach to TranscoderOutput
			OutputStream png_ostream = new FileOutputStream(pngPath);
			TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);
			// Step-3: Create PNGTranscoder and define hints if required
			PNGTranscoder my_converter = new PNGTranscoder();
			// Step-4: Convert and Write output
			my_converter.transcode(input_svg_image, output_png_image);
			// Step 5- close / flush Output Stream
			png_ostream.flush();
			png_ostream.close();
			svg_istream.close();
			
		} catch(TranscoderException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Map<String, Object> requestParamsToParamsMap(Map<String, Object> inRequestParams){
		return MapValueConv.fromFullyQualifiedKeys(inRequestParams);
	}
	
	public StringBuilder generateHTML(Map<String, Object> data, boolean isDisplayMode){
		StringBuilder ret = new StringBuilder();
		
		try{
			data = sanitiseMap(data);
		}catch(Exception e){
			throw new RuntimeException(e.getMessage());
		}
		
		if(!_formDefinitionString.isEmpty()){
			ret = formGen.build(_formDefinitionString, data, isDisplayMode);
		}else{
			ret = formGen.build(_formDefinitionMap, data, isDisplayMode);
		}
		
		String bodyPrefix = "";
		if(!isDisplayMode){
			bodyPrefix = readBodyPrefix("PrefixHTML");
		}else{
			bodyPrefix = readBodyPrefix("PrefixDisplay");
		}
		String bodySuffix = "";
		if(!isDisplayMode){
			bodySuffix = readBodySuffix("SuffixHTML");
		}else{
			bodySuffix = readBodySuffix("SuffixDisplay");
		}
		ret.insert(0, sanitiseString(bodyPrefix, ""));
		ret.append(sanitiseString(bodySuffix, ""));
		
		return ret;
	}
	
	public byte[] generatePDF(Map<String, Object> data){
		StringBuilder ret = new StringBuilder();
		
		data = sanitiseMap(data);
		
		if(!_formDefinitionString.isEmpty()){
			ret = formGen.build(_formDefinitionString, data, true);
		}else{
			ret = formGen.build(_formDefinitionMap, data, true);
		}
		
		if(ret == null){
			throw new RuntimeException("generatePDF() -> pdfResult is empty, there was an error in generatePDFReadyHTML()");
		}
		
		String pdfFilePath = _resourceFolderPath + "/pdf/"+GUID.base58()+".pdf";
		
		String bodyPrefix = readBodyPrefix("PrefixPDF");
		String bodySuffix = readBodySuffix("SuffixPDF");
		ret.insert(0,  sanitiseStringForPDF(bodyPrefix, ""));
		ret.append(bodySuffix);
		
		PDFGenerator.generatePDFfromRawHTML(pdfFilePath, ret.toString(), _formFolderPath );
		
		//read the pdf file now
		File pdfFile = new File(pdfFilePath);
		byte[] pdfData = null;
		try{
			pdfData = FileUtils.readFileToByteArray(pdfFile);
		}catch(Exception e){
			throw new RuntimeException("generatePDF() -> " + e.getMessage());
		}
		
		return pdfData;
	}
}
