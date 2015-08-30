package picoded.webTemplateEngines.JSML;

import java.io.*;
import java.util.*;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.lesscss.deps.org.apache.commons.io.FileUtils;

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
	}
	
	public JSMLForm(String formFolderPath, String uriContext, String tmpFolderPath){
		formGen = new FormGenerator();
		
		setFormFolder(formFolderPath);
		setResourceFolder(tmpFolderPath);
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
	
	public void setDefinition(String inFormDefinition){
		_formDefinitionString = inFormDefinition;
	}
	
	public void setDefinition(Map<String, Object> inFormDefinition){
		_formDefinitionMap = inFormDefinition;
	}
	
	public Map<String, Object> getBlankData(){
		//generate empty data
		return null;
	}
	
	public Map<String, Object> getDummyData(){
		//go through the definition and generate random data for each field
		return null;
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
	
	public String readBodyPrefix(){
		File bodyPrefixFile = getFileInRootFolder(_bodyPrefix);
		try{
			String bodyPrefixString = FileUtils.readFileToString(bodyPrefixFile);
			return bodyPrefixString;
		}catch(Exception e){
			throw new RuntimeException("readBodyPrefix() -> " + e.getMessage());
		}
	}
	
	public String readBodySuffix(){
		File bodyPrefixFile = getFileInRootFolder(_bodySuffix);
		try{
			String bodyPrefixString = FileUtils.readFileToString(bodyPrefixFile);
			return bodyPrefixString;
		}catch(Exception e){
			throw new RuntimeException("readBodySuffix() -> " + e.getMessage());
		}
	}
	
	public String readStyleSheet(){
		File bodyPrefixFile = getFileInRootFolder(_styleSheetSuffix);
		try{
			String bodyPrefixString = FileUtils.readFileToString(bodyPrefixFile);
			return "<style>" + bodyPrefixString + "</style>";
		}catch(Exception e){
			throw new RuntimeException("readStyleSheet() -> " + e.getMessage());
		}
	}
	
	public String getFullPrefix(){
		String bodyPrefix = readBodyPrefix();
		String styleDeclare = readStyleSheet();
		
		return styleDeclare + bodyPrefix;
	}
	
	public String getFullSuffix(){
		String bodySuffix = readBodySuffix();
		
		return bodySuffix;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> sanitiseMap(Map<String, Object> inMap){
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
	private List<Object> sanitiseList(List<Object> inList){
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
	
	private String sanitiseString(String inString, String name){
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
		return null;
	}
	
	public StringBuilder generateHTML(Map<String, Object> data, boolean isDisplayMode){
		StringBuilder ret = new StringBuilder();
		
		if(!_formDefinitionString.isEmpty()){
			ret = formGen.build(_formDefinitionString, data, isDisplayMode);
		}else{
			ret = formGen.build(_formDefinitionMap, data, isDisplayMode);
		}
		
		String fullPrefix = getFullPrefix();
		String fullSuffix = getFullSuffix();
		ret.insert(0, fullPrefix);
		ret.append(fullSuffix);
		
		return ret;
	}
	
	public StringBuilder generateDisplay(Map<String, Object> data){
		StringBuilder ret = new StringBuilder();
		if(!_formDefinitionString.isEmpty()){
			ret = formGen.build(_formDefinitionString, data, true);
		}else{
			ret = formGen.build(_formDefinitionMap, data, true);
		}
		
		String fullPrefix = getFullPrefix();
		String fullSuffix = getFullSuffix();
		ret.insert(0, fullPrefix);
		ret.append(fullSuffix);
		
		return ret;
	}
	
	public byte[] generatePDF(Map<String, Object> data, boolean isDisplayMode){
		StringBuilder ret = new StringBuilder();
		if(!_formDefinitionString.isEmpty()){
			ret = formGen.build(_formDefinitionString, data, true);
		}else{
			ret = formGen.build(_formDefinitionMap, data, true);
		}
		
		if(ret == null){
			throw new RuntimeException("generatePDF() -> pdfResult is empty, there was an error in generatePDFReadyHTML()");
		}
		
		String pdfFilePath = _resourceFolderPath + "/generatedPDF.pdf";
		
		String styleSheet = readStyleSheet();
		ret.insert(0, styleSheet);
		 
		PDFGenerator.generatePDFfromRawHTML(pdfFilePath, ret.toString(), "file:///" + _resourceFolderPath);
		
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
