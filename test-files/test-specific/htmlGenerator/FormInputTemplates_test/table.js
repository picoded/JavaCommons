{
	"type":"table",
	"field":"clients",
	"tableHeader":"Clients",
	"headers":["Name", "NRIC", "Gender", "Nationality"],
	"children":[
		{
			"type":"div",
			"field":"name"
		},
		{
			"type":"text",
			"field":"nric"
		},
		{
			"type":"dropdown",
			"options":["Male", "Female"],
			"field":"gender"
		},
		{
			"type":"checkbox",
			"options":["Singaporean", "Singaporean PR", "Others"],
			"field":"nationality"
		}
	]
}