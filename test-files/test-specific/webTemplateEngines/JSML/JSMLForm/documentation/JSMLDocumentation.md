# Documentation for JSON to HTML converter, or as I like to call it, the JSML engine.


## Simple Explanation
This component takes in Json formatted input, and outputs an html formatted string.
It can also output the data in a pdf ready html format, for use with the flying-saucer library.

	
## Detailed Explanation
The FormGenerator will do the templating and spit out the html string.
Create an instance of FormGenerator, and call applyTemplating.
Use the version of the function that accepts a String and Map as parameters, as it will do any conversion for you automatically.

For pdf output, call generatePDFReadyHtml on the FormGenerator instance.
Use the version that also accepts a String and Map as parameters.

The second parameter, the Map parameter, can be null. It represents any data that you want to use to "auto-fill" the page. It is mainly used in generating the PDF output. The autofill data should be formatted as a json object, with key value pairs for the data. The key should correspond to the field key found in the json file declaring your html page.

	
## Extending the functionality
FormGenerator is designed to be extended. If there are specific html tag types which arent available, you can create your own!

This class uses Functional Interfaces, found in Java 8. 
Creating new functionality is as easy as creating an instance of FormInputInterface / FormWrapperInterface.
For example, 

	private void FormInputInterface new_Input = (node)->{
		//do functionality here
		//node is an instance of FormNode
		
		//dont forget to return a string 
		
		return "This is the input string";
	};

	This will output the string "<div>This is the input string</div>".

Keep in mind that FormInputInterface returns a String, and FormWrapperInterface returns a string array of size 2. YOU SHOULD NOT HAVE TO CREATE A CUSTOM WRAPPER! The default wrapper already returns your basic div tags, which is enough for most things.

After that, you need to add your defined functionality to FormGenerator. Call addCustomFormInputTemplate or addCustomFormWrapperTemplate on the FormGenerator instance. This function returns null if the mapping wasnt available previously, but will return the previous mapping if found, so do keep an eye out for that, and check for !null when calling them.
When calling the add functions, the first parameter is the key for "type" you use in the JSON mapping.
So as you can see from the example, its basically a string outputter.
	
	
## Json Keys
You can refer to JSMLExampleJson.js and JSMLtestData.js for this section.
	
	
## Keys common to all
### Label
This maps to the "label" html tag. The value here will be placed right before the main input.
Example
		
	<label>Label</label>
	<h3>Title</h3>
	
This behaviour is consistent across all json types.
	
### Field
This maps to the "id" html tag. Value here will become the tags id.
Try and keep this unique across the whole json file to keep things easy.

!IMPORTANT!
The field key is what you use to bridge the data json to the declarative json.
Example
		
		In jsonToHtml.js
		{
			"type":"text",
			"field":"textFieldA"
		}
		
		In jsonData.js
		{
			"textFieldA":"Prefilled Data"
		}
		
This will pass the value "Prefilled Data" to the html object with an id of "textFieldA".
This functionality is mainly for outputting to PDF, but will still work for normal html.
So the textfield will have the text "Prefilled Data" inside it, or a dropdown will have a default value selected.
The usefulness comes when you have data stored on the database, and you query it, and output the PDF with the data.
		
### Children
This represents nested Html tags. This key only accepts a JSON Array of JSON Objects!!!
For example
		
		In jsonToHtml.js
		{
			"type":"title",
			"text":"Title with nested tags",
			"children":[
				{
					"type":"title",
					"text":"First nested tag"
				},
				{
					"type":"title",
					"text":"Second nested tag"
				}
			]
		}
		
this will output
		
		<div>
			<h3>Title with nested tags</h3>
			<div>
				<h3>First nested tag</h3>
				<h3>Second nested tag</h3>
			</div>
		</div>
	
	
## Json "type" Key
Default Values for TYPE are

* title (html header)
* dropdown (html select)
* text (html input with type=text)
* dropdownWithOthers (a dropdown with an input type=text that appears for a certain value)
	
	
### title
	This maps to the "h3" html tag.
	
	Title specific keys
	
	1. text
	
	The value mapped to "text" will be the same thing as the value for your h3 tag
	
### dropdown
	This maps to the <select> html tag.
	
	Dropdown specific keys
	
	1. options
	
		"options" represents the choices the user can select in a dropdown menu.
		This key accepts data as a JSON object or JSON array. 
		If object is used, the key is taken as the "id" element in the html tag, and the 
		value is used as the display. 
		If array is used, the value has its whitespace and special characters removed, 
		then lowercased to be used as the "id" tag. 
		The example contains both types so you can take a look.
		
### text
	This maps to the <input> html tag, with element "type" of "text". So basically a text input field.
	Text has no specific keys.

		
### dropdownWithOthers
	This is a special one. 
	It contains both a <select> tag and <input type=text> tag which is hidden by default unless the user selects the specified dropdown option.
	

	1. "options"
	
		This operates the same way as the dropdown "options" key.

	2. "othersOption"
	
		This represents the value out of the options listed that will be used to show 
		the text field.
		For example
	
		{
			"type":"dropdownWithOthers",
			"options":{
				"option1":"Option 1",
				"option2":"Option 2",
				"option3":"Option 3"
			},
			"othersOption":"option3"
		}
		
		This will make it so that a text field will appear only when the user selects 
		Option 3.

	3. "textField"
		
		This is the same as the "field" key, but is used for the text input field 
		element. Give it a separate identifier to the "field" key.

	4. "functionName"
		
		This is the function name you want to give to the injected javascript.
		For example

		{
			"type":"dropdownWithOthers",
			"field":"ddField",
			"textField":"ddTextField",
			"options":{
				"option1":"Option 1",
				"option2":"Option 2",
				"option3":"Option 3"
			},
			"othersOption":"option3",
			"functionName":"OnChangeFunction"
		}

		will translate to
		
		<script> 
			function OnChangeFunction() { 
				var dropDown = document.getElementById("ddField");
				var inputField = document.getElementById("ddTextField");
				if(dropDown.value == "option3"){
					inputField.style.display = "inline";
				}else{
					inputField.style.display = "none";
				}
			};
		</script>
		<select class="pf_select" onchange="OnChangeFunction()" id="ddField">
			<option value="option1">Option 1</option>
			<option value="option2">Option 2</option>
			<option value="option3">Option 3</option>
		</select>
		<input type="text" id="ddTextField">
		
	


	
	
	