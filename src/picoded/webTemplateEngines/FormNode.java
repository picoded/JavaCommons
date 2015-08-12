package picoded.webTemplateEngines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import picoded.conv.ConvertJSON;
import picoded.conv.RegexUtils;
import picoded.conv.GenericConvert;
import picoded.struct.GenericConvertMap;
import picoded.struct.CaseInsensitiveHashMap;

/// FormNode serves as a map accessor to the form defination structure,
/// with various utility functions, for Wrapper, and Input interface writers
///
/// @TODO Change class extension to use AbstractMapDecorator, so that it proxy the valeus from the soruce instead
public class FormNode extends CaseInsensitiveHashMap<String, Object> implements GenericConvertMap<String, Object> {
	
	//
	// Values function
	//
	// Contains functions that provide values such as prefix / suffixes 
	// that maybe changed / made configurable in future
	//
	//------------------------------------------------------------------------
	
	/// @returns {String} prefix for the label auto class
	public String prefix_label() {
		return "pfl_";
	}
	
	/// @returns {String} prefix for the input auto class
	public String prefix_input() {
		return "pfi_";
	}
	
	/// @returns {String} prefix for the wrapper container auto class
	public String prefix_wrapper() {
		return "pfw_";
	}
	
	/// @returns {String} prefix for the child container auto class
	public String prefix_childWrapper() {
		return "pfc_";
	}
	
	//
	// Utility helper functions
	//
	// Consists mainly of helper functions used by the various interface
	// implmentation for Html string generation
	//
	//------------------------------------------------------------------------
	
	/// Applies a custom prefix, and suffix to a string split by a seperator
	/// 
	/// @params {String}  value   - The value to split and add prefix/suffix
	/// @params {String}  split   - The string to split the input value
	/// @params {String}  prefix  - The prefix to add to each split value 
	/// @params {String}  suffix  - The suffix to add to each split value 
	/// 
	/// @returns {StringBuilder}  - The rebuilt value set, with prefix and suffix added
	public static StringBuilder addPrefixAndSuffix(String value, String split, String prefix, String suffix) {
		StringBuilder ret = new StringBuilder();
		
		if( value != null && value.length() > 0 ) {
			String[] valueArr = value.split(split);
			boolean firstElement = true;
			
			for(int a=0; a<valueArr.length; ++a) {
				if(valueArr[a] == null || valueArr[a].length() <= 0) {
					continue;
				}
				
				if( !firstElement ) {
					firstElement = false;
				} else {
					ret.append(split);
				}
				
				if( prefix != null ) {
					ret.append(prefix);
				}
				ret.append(value);
				if( suffix != null ) {
					ret.append(suffix);
				}
			}
		}
		
		return ret;
	}
	
	/// Shorten varient of addPrefixAndSuffix, where split is " ", and suffix is null
	/// 
	/// @params {String}  value   - The value to split and add prefix/suffix
	/// @params {String}  prefix  - The prefix to add to each split value
	/// 
	/// @returns {StringBuilder}  - The rebuilt value set, with prefix and suffix added
	public static StringBuilder addPrefix(String value, String prefix) {
		return addPrefixAndSuffix(value, " ", prefix, null);
	}
	
	/// Generates the standard node parameter map for input. This is useful for shared default behaviour
	///
	/// @params {String}  inputBaseClass   - The input class to add, before the automated classes
	/// @params {Map<String,String>}  map  - The return map to setup, if null it returns a new CaseInsensitiveHashMap
	///
	/// @returns {Map<String,String>} - the return parameter map
	public Map<String,String> defaultInputParameterMap( String inputBaseClass, Map<String,String> map ) {
		if( map == null ) {
			map = new CaseInsensitiveHashMap<String,String>();
		}
		
		//
		// Default class handling
		//-----------------------------------
		
		StringBuilder tmpSB = new StringBuilder(inputBaseClass);
		String tmp = null;
		if( (tmp = getString( JsonKeys.AUTO_CLASS, null )) != null && tmp.length() > 0 ) {
			if(tmpSB.length() > 0) {
				tmpSB.append(" ");
			}
			
			tmpSB.append( addPrefix(tmp, prefix_label()) );
		}
		
		if( (tmp = getString( JsonKeys.INPUT_CLASS, null )) != null && tmp.length() > 0 ) {
			if(tmpSB.length() > 0) {
				tmpSB.append(" ");
			}
			
			tmpSB.append( tmp );
		}
		map.put( "class", tmpSB.toString() );
		
		//
		// Style injection handling
		//-----------------------------------
		if( (tmp = getString( JsonKeys.INPUT_CSS, null )) != null && tmp.length() > 0 ) {
			map.put("style", tmp);
		}
		
		//
		// Input ID handling
		//-----------------------------------
		if( (tmp = getString( JsonKeys.INPUT_ID, null )) != null && tmp.length() > 0 ) {
			map.put("id", tmp);
		}
		
		//
		// Fieldname handling
		//-----------------------------------
		if( (tmp = getFieldName()) != null && tmp.length() > 0 ) {
			map.put("name", tmp);
		}
		
		return map;
	}
	
	/// Helps escape html dom parameter quotes, in an "optimal" way
	///
	/// @params {String}   val   - The string to be quote escaped
	///
	/// @returns {String}  - The quote escaped value, with either single or double quotes, or quotes with escaped quotes
	protected static String escapeParameterQuote( String val ) {
		boolean hasSingleQuote = val.contains("\'");
		boolean hasDoubleQuote = val.contains("\"");
		
		if( hasSingleQuote && hasDoubleQuote ) {
			//No choice, escape double quotes, and use them
			return "\""+val.replaceAll("\\\\", "\\\\").replaceAll("\"","\\\"")+"\"";
		} else if( hasDoubleQuote ) {
			return "\'"+val+"\'";
		} else if( hasSingleQuote ) {
			return "\""+val+"\"";
		} //else { //quoteless, use single quotes
		return "\'"+val+"\'";
	}

	/// Generates a HTML node, with its prefix and suffix. Using its type, and parameters
	///
	/// @params {String}              nodeType            - HTML DOM type to generate, such as DIV, or INPUT
	/// @params {Map<String,Object>}  parameterMap        - Parameters map to input into the DOM
	/// @params {String}              rawParameterString  - Additional raw parameters to be added (optional)
	///
	/// @returns {StringBuilder[2]}  - A pair of StringBuilder representing the prefix and suffix nodes
	public static StringBuilder[] htmlNodeGenerator( String nodeType, Map<String,String> parameterMap, String rawParameterString ) {
		StringBuilder domNode = new StringBuilder("<"+nodeType);
		String innerHtml = null;
		
		if( parameterMap != null ) {
		for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
				// Setup rest of parameters
				domNode //
					.append(" ") //
					.append(entry.getKey()) //
					.append("=") //
					.append( escapeParameterQuote( GenericConvert.toString(entry.getValue(), "") ) ); //
			}
		}
		
		if( rawParameterString != null && rawParameterString.length() > 0 ) {
			domNode.append(" ").append(rawParameterString);
		}
		domNode.append(">");
		
		return new StringBuilder[] { domNode,  new StringBuilder("</"+nodeType+">") };
	}
	
	/// A combination of defaultInputParameterMap, and htmlNodeGenerator
	///
	/// @params {String}  nodeType          - HTML DOM type to generate, such as DIV, or INPUT
	/// @params {String}  inputBaseClass    - The input class to add, before the automated classes
	/// @params {Map<String,String>}  map   - The parameter map to setup, if null it uses a new CaseInsensitiveHashMap
	///
	/// @returns {StringBuilder[2]}  - A pair of StringBuilder representing the prefix and suffix nodes
	public StringBuilder[] defaultHtmlInput( String nodeType, String nodeClass, Map<String,String> parameterMap ) {
		return htmlNodeGenerator( nodeType, defaultInputParameterMap( nodeClass, parameterMap), null );
	}
	
	//
	// Constructor reliant vars
	//
	//------------------------------------------------------------------------
	
	/// List of children FormNode to be generated / used?
	protected List<FormNode> _children = new ArrayList<FormNode>();
	
	/// List of input data values, used to generate with multiple input fields
	protected List< Map<String,Object> > _inputValues = new ArrayList<Map<String,Object>> ();
	
	/// The root form generator
	protected FormGenerator _formGenerator = null;
	
	//
	// Constructor
	//
	//------------------------------------------------------------------------
	
	/// Blank constructor, useful for unit testing
	public FormNode() { }
	
	/// Internal reuse constructor, used to load the very config values
	///
	/// @params {Map<String,Object>}  mapObject       - The map object, used to generate this nodes defination
	/// @params {List<Map<String,Object>>} inputData  - The provided input data values
	@SuppressWarnings("unchecked")
	private void innerConstructor(FormGenerator root, Map<String, Object> mapObject, List<Map<String,Object>> inputData){
		// Setup the form generator
		this._formGenerator = root;
		
		// Fill up stored map
		this.putAll( mapObject );
		
		// Put in the inputData list
		this._inputValues = inputData;
		
		if( this.containsKey("children") ) {
			Object childrenRaw = this.get("children");
			this.remove("children");
			
			if( !(childrenRaw instanceof List) ) {
				throw new IllegalArgumentException("'children' parameter found in defination was not a List: "+childrenRaw);
			}
			
			// Iterate each child object
			for(Object child : ((List<Object>)childrenRaw)) {
				
				if( !(child instanceof Map) ) {
					throw new IllegalArgumentException("'children' List item found in defination was not a Map: "+child);
				}
				
				_children.add( new FormNode( _formGenerator, (Map<String,Object>)child, inputData ) );
			}
		}
	}
	
	/// Constructor varient using array of input data
	///
	/// @params {Map<String,Object>}  mapObject       - The map object, used to generate this nodes defination
	/// @params {List<Map<String,Object>>} inputData  - The provided input data values
	public FormNode(FormGenerator root, Map<String, Object> mapObject, List<Map<String,Object>> inputData) {
		innerConstructor(root, mapObject, inputData);
	}
	
	/// Constructor using a single input data node
	///
	/// @params {Map<String,Object>}  mapObject       - The map object, used to generate this nodes defination
	/// @params {Map<String,Object>}  inputData       - The provided input data values
	public FormNode(FormGenerator root, Map<String, Object> mapObject, Map<String, Object> inputData){
		List<Map<String,Object>> inputDataArr = new ArrayList<Map<String,Object>>();
		inputDataArr.add(inputData);
		innerConstructor(root, mapObject, inputDataArr);
	}
	
	//
	// Generates the HTML output via input or wrapper code
	//
	//------------------------------------------------------------------------
	
	/// Gets and returns the input html representing the current FormNode
	///
	/// @params {boolean} displayOnly  - Returns the varient for read only display mode (eg: PDF)
	///
	/// @returns {StringBuilder} Full StringBuilder representing the output result
	public StringBuilder inputHtml(boolean displayMode) {
		// Gets type
		String nodeType = getString(JsonKeys.INPUT_TYPE, getString(JsonKeys.TYPE, "*"));
		
		// Gets the input generator
		FormInputInterface func = _formGenerator.inputInterface(displayMode, nodeType);
		
		// The node 
		return func.apply(this);
	}
	
	/// Gets and return the full wrapper html, via the wrapper function.
	/// Which in turn will call the resepective inputHtml() and/or child nodes
	///
	/// @params {boolean} displayOnly  - Returns the varient for read only display mode (eg: PDF)
	///
	/// @returns {StringBuilder} Full StringBuilder representing the output result
	public StringBuilder fullHtml(boolean displayMode) {
		// Gets type
		String nodeType = getString(JsonKeys.WRAPPER_TYPE, getString(JsonKeys.TYPE, "*"));
		
		// Gets the input generator
		FormWrapperInterface func = _formGenerator.wrapperInterface(displayMode, nodeType);
		
		// The node 
		return func.apply(this);
	}
	
	/// [private function used to avoid overload conflict, and to resolve it]
	/// 
	/// Generates the children wrapper html, using the function to generate html strings inbetween if needed
	/// Note that given int is the child index number before the spacer. And spacer is ignored for last child.
	/// The spacer function is also ignored if its null
	///
	/// @params  {boolean}                      displayOnly  - Returns the varient for read only display mode (eg: PDF)
	/// @params  {Function<int,StringBuilder>}  spacer       - lamda function to call to add a spacer string in between child nodes
	///
	/// @returns {StringBuilder} Full StringBuilder representing the output result
	private StringBuilder _fullChildrenHtml(boolean displayMode, Function<Integer,StringBuilder> spacer) {
		StringBuilder ret = new StringBuilder();
		List<FormNode> childList = children();
		
		// Skip if no childList (avoid null error)
		if( childList == null ) {
			return ret;
		}
		
		// Iterate child list, and generate for each node
		int childListSize = childList.size();
		for( int a=0; a<childListSize; ++a ) {
			
			// Add the child full html
			ret.append( childList.get(a).fullHtml(displayMode) );
			
			// Not last child, add spacer
			if( spacer != null && (a+1)<childListSize ) {
				ret.append( spacer.apply(a) );
			}
		}
		
		return ret;
	}

	/// Generates the children wrapper html, using the function to generate html strings inbetween if needed
	/// Note that given int is the child index number before the spacer. And spacer is ignored for last child.
	/// The spacer function is also ignored if its null
	///
	/// @params  {boolean}                      displayOnly  - Returns the varient for read only display mode (eg: PDF)
	/// @params  {Function<int,StringBuilder>}  spacer       - lamda function to call to add a spacer string in between child nodes
	///
	/// @returns {StringBuilder} Full StringBuilder representing the output result
	public StringBuilder fullChildrenHtml(boolean displayMode, Function<Integer,StringBuilder> spacer) {
		return _fullChildrenHtml(displayMode, spacer);
	}
	
	/// Varient of formChildrenFullHtml, without spacer function
	///
	/// @params  {boolean}                      displayOnly  - Returns the varient for read only display mode (eg: PDF)
	///
	/// @returns {StringBuilder} Full StringBuilder representing the output result
	public StringBuilder fullChildrenHtml(boolean displayMode) {
	 	return _fullChildrenHtml(displayMode, null);
	}
	
	/// Varient of formChildrenFullHtml, with fixed spacer function 
	///
	/// @params  {boolean}  displayOnly  - Returns the varient for read only display mode (eg: PDF)
	/// @params  {String}   spacer       - spacer string in between child nodes
	///
	/// @returns {StringBuilder} Full StringBuilder representing the output result
	public StringBuilder fullChildrenHtml(boolean displayMode, String spacerString) {
		final StringBuilder spacerBuilder = new StringBuilder(spacerString);
		Function<Integer,StringBuilder> spacer = (n) -> { return spacerBuilder; };
		return _fullChildrenHtml(displayMode, spacer);
	}
	
	//
	// Variables access, and getter functions
	//
	//------------------------------------------------------------------------
	
	/// Returns all the generated children form nodes
	/// 
	/// @returns {List<FormNode>}  the list of formnodes
	public List<FormNode> children() {
		return _children;
	}
	
	/// Returns the defined label value. This is used to generate the wrapper text if needed
	public String label(){
		return containsKey(JsonKeys.LABEL) ? getString(JsonKeys.LABEL) : "";
	}
	
	// /// Gets the value from the input data array
	// public String getValue( String fieldName, int index ) {
	// 	String strRet = null;
	// 	Object objRet = null;
	// 	
	// 	if( _inputValues.get(index) != null ) {
	// 		objRet = _inputValues.get(index).get(fieldName);
	// 		strRet = GenericConvert.toString(objRet, null);
	// 	}
	// 	
	// 	if( strRet != null ) {
	// 		return strRet;
	// 	}
	// 	
	// 	return getString( JsonKeys.DEFAULT, null );
	// }
	
	/// @TODO
	/// Returns the default value of the object, 
	public String getFieldName(){
		if( containsKey(JsonKeys.FIELD) ) {
			return RegexUtils.removeAllNonAlphaNumeric( getString(JsonKeys.FIELD) ).toLowerCase();
		}
		return null;
	}
	
	/// @TODO
	/// Returns the default value of the object, 
	public String getFieldValue(){
		return GenericConvert.toString(getDefaultValue(getFieldName()));
	}
	
	//
	// To remove
	//
	//------------------------------------------------------------------------
	
	/// @TODO
	/// Returns the default value of the object, 
	public String getFieldValue(String fieldName){
		return null;
	}
	
	/// Returns the default value of the object, 
	/// note that this will get the 0th indexed value.
	// @Deprecated
	public Object getDefaultValue(String fieldName){
		if(_inputValues.get(0) != null && _inputValues.get(0).containsKey(fieldName)){
			return _inputValues.get(0).get(fieldName);
		}
		
		return null;
	}
	
	// @Deprecated
	public void setChildren(List<FormNode> children){
		_children = new ArrayList<FormNode>(children); //copy constructor behaviour
	}
	
	// @Deprecated
	public void addChild(FormNode child){
		_children.add(child);
	}
	
	// @Deprecated
	public int childCount(){
		return _children.size();
	}
	
	// @Deprecated
	public void setPrefilledData(Map<String, Object> prefilledJSONData){
		_inputValues.set(0,prefilledJSONData);
	}
	
	// @Deprecated
	public static List<FormNode> createFromJSONString(FormGenerator root, String jsonString, Map<String, Object> prefilledJSONData){
		List<FormNode> formNodes = new ArrayList<FormNode>();
		
		if(jsonString.charAt(0) == '['){
			List<Object> nodeList = ConvertJSON.toList(jsonString);
			return createFromList(root, nodeList, prefilledJSONData);
		}else{
			Map<String, Object> nodeMap = ConvertJSON.toMap(jsonString);
			FormNode newNode = new FormNode(root, nodeMap, prefilledJSONData);
			formNodes.add(newNode);
			return formNodes;
		}
	}
	
	// @Deprecated
	@SuppressWarnings("unchecked")
	public static List<FormNode> createFromList(FormGenerator root, List<Object> listObject, Map<String, Object> prefilledJSONData){
		List<FormNode> formNodes = new ArrayList<FormNode>();
		
		for(Object obj:listObject){
			Map<String, Object> nodeMapObject = (Map<String, Object>)obj;
			formNodes.add(new FormNode(root, nodeMapObject, prefilledJSONData));
		}
		
		return formNodes;
	}
	
	/// Collapse the various string builder array into a single string builder
	///
	/// @params {StringBuilder[]}  toCollapse  - Array of string builder to collapse together
	/// 
	/// @returns {StringBuilder}  - The final collapsed string builder
	public StringBuilder collapseStringBuilderArray( StringBuilder[] toCollapse ) {
		StringBuilder ret = new StringBuilder();
		for(int a=0; a<toCollapse.length; ++a) {
			ret.append( toCollapse[a] );
		}
		return ret;
	}
	
}
