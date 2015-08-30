package picoded.webTemplateEngines.JSML;

import java.io.*;
import java.util.*;

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
	
	private String _defaultResourceFolderName = "resources";
	
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
	
	public void setBlankData(){
		//generate empty data
	}
	
	public void setDummyData(){
		//go through the definition and generate random data for each field
	}
	
	private File[] getFilesInRootFolder(){
		File rootFolder = new File(_resourceFolderPath);
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
			return bodyPrefixString;
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
		String pdfResult = "";
		if(!_formDefinitionString.isEmpty()){
			pdfResult = formGen.generatePDFReadyHTML(_formDefinitionString, data);
		}else{
			pdfResult = formGen.generatePDFReadyHTML(_formDefinitionMap, data);
		}
		
		StringBuilder ret = new StringBuilder(pdfResult);
		
		String fullPrefix = getFullPrefix();
		String fullSuffix = getFullSuffix();
		ret.insert(0, fullPrefix);
		ret.append(fullSuffix);
		
		return ret;
	}
	
	public byte[] generatePDF(Map<String, Object> data, boolean isDisplayMode){
		String pdfResult = "";
		if(!_formDefinitionString.isEmpty()){
			pdfResult = formGen.generatePDFReadyHTML(_formDefinitionString, data);
		}else{
			pdfResult = formGen.generatePDFReadyHTML(_formDefinitionMap, data);
		}
		
		if(pdfResult == null || pdfResult.isEmpty()){
			throw new RuntimeException("generatePDF() -> pdfResult is empty, there was an error in generatePDFReadyHTML()");
		}
		
		String pdfFilePath = _resourceFolderPath + "/generatedPDF.pdf";
		
		String styleSheet = readStyleSheet();
		pdfResult = styleSheet + pdfResult;
		 
		PDFGenerator.generatePDFfromRawHTML(pdfFilePath, pdfResult, "file:///" + _resourceFolderPath);
		
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
