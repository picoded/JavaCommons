{
	"type":"for",
	"field":"textfield",
	"label":"TextField",
	"removeLabel":"false",
	
	"HTML_wrapperAttributes":"singleWrapperAttrib='value' secondSinglWrappereAttrib='value2'",
	"HTML_childWrapperAttributes":"singleChildWrapperAttrib='value'",
	"HTML_labelAttributes":"singleLabelAttrib='value'",
	"HTML_inputAttributes":"singleInputAttrib='value'",
	
	"HTML_wrapperAttributesMap": {
		"mapWrapperAttribute" : "20",
		"mapWrapperSecondAttribute" :"40"
	},
	"HTML_childWrapperAttributesMap": {
		"mapChildWrapperAttribute" : "childProperty"
	},
	
	"HTML_labelAttributesMap": {
		"mapLabelAttribute" : "labelValue"
	},
	
	"children":[
		{
			"type":"title",
			"label":"Title Label",
			"HTML_inputAttributesMap": {
				"inputAtribute" : "inputProperty"
			},
			"text":"Title"
		},
		{
			"type":"text",
			"field":"data"
		}
	]
}