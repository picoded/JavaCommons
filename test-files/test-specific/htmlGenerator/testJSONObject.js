{
	"type":"div",
	"children":[
		{
			"type": "title",
			"text": "Your Personal Information",
			"inputCss":"color:red; font-size:25;",
			"children":[
				{
					"type":"dropdown",
					"options":["Dr","Mr", "Mrs", "Mdm", "Miss"],
					"field":"title",
					"label":"Title",
					"size":29
				},
				{
					"type":"text",
					"field":"name",
					"label":"Name (as in NRIC)",
					"size":29
				},
				{
					"type":"text",
					"field":"nricpp",
					"label":"NRIC/ Passport No",
					"size":30
				},
				{
					"type":"dropdown",
					"options":["Singaporean", "Singaporean PR", "Others"],
					"active": "natOther",
					"field":"nat",
					"label":"Nationality",
					"size":30
				},
				{
					"type":"text",
					"field":"natOther",
					"key":"others",
					"size":47,
					"extra":"Please note that with the implementation of FATCA, some services may not be available to a US person. <br> (a) A citizen or lawful permanent resident (including US green card holder) of the US; or <br> (b) A partnership or corporation organised in the US or under the laws of the US or any State thereof, or a trust if: <li>(i) a court within the US would have authority under the applicable law to render orders or judgments concerning substantially all issues regarding the administration of the trust; and </li><li> (ii) one or more US persons have the authority to control all substantial decisions of the trust, or an estate of a decedent that is a citizen or resident of the US.</li>"
				},
				{
					"type":"text",
					"field":"CoB",
					"label":"Country Of Birth",
					"size":29
				},
				{
					"type":"dropdown",
					"field":"gender",
					"label":"Gender",
					"options":["Male", "Female"],
					"size":29
				},
				{
					"type":"text",
					"label":"Relationship to Main Applicant",
					"field":"rltApp",
					"size":29
				},
				{
					"type":"dropdown",
					"label":"Marital Status",
					"options":["Single", "Married", "Others"],
					"active": "marriageOthers",
					"field":"marriage",
					"size":30
				},
				{
					"type":"text",
					"size":47,
					"field":"marriageOthers",
					"key":"others"
				},
				{
					"type":"text",
					"label":"Date of Birth (dd:mm:yyyy)",
					"field":"dob",
					"size":20
				},
				{
					"type":"dropdown",
					"label":"Smoker",
					"options":["Yes", "No"],
					"field":"isSmoker",
					"size":29
				},
				{
					"type":"title",
					"text":"Residential Address",
					"size":19
				},
				{
					"type":"text",
					"label":"Address",
					"field":"address",
					"size":27
				},
				{
					"type":"text",
					"label":"Post Code",
					"field":"postcode",
					"size":27
				},
				{
					"type":"text",
					"label":"Country",
					"field":"country",
					"size":27
				},
				{
					"type":"dropdown",
					"field":"race",
					"label":"Race",
					"options":["Chinese", "Malay", "Indian", "Others"],
					"size":29
				},
				{
					"type":"text",
					"label":"Language Spoken",
					"field":"language",
					"size":29
				},
				{
					"type":"dropdown",
					"label":"Employment Status",
					"field":"employment",
					"options":["Full-time", "Part-time", "Not Employed", "Self Employed", "Retired", "Others"],
					"size":20
				},
				{
					"type":"dropdown",
					"field":"EduLvl",
					"label":"Education Level",
					"options":["Primary", "Secondary", "Pre-Tertiary", "Tertiary and Above"],
					"size":29
				},
				{
					"type":"text",
					"label":"Occupation",
					"field":"occupation",
					"size":29
				},
				{
					"type":"text",
					"label":"Employer",
					"field":"employer",
					"size":29
				},
				{
					"type":"title",
					"text":"Contact",
					"size":19
				},
				{
					"type":"text",
					"label":"Home",
					"field":"contactHome",
					"size":27
				},
				{
					"type":"text",
					"label":"Office",
					"field":"contactOffice",
					"size":27
				},
				{
					"type":"text",
					"label":"HP",
					"field":"contactHandphone",
					"size":27
				},
				{
					"type":"text",
					"label":"Fax",
					"field":"contactFax",
					"size":27
				},
				{
					"type":"text",
					"label":"E-mail",
					"field":"contactEmail",
					"size":27
				},
				{
					"type":"dropdown",
					"label":"Income Range",
					"options":["Below $1,000", "$1,001 to $2,000", "$2,001 to $4,000", "$4,001 to $6,000"
							 , "$6,001 to $8,000", "$8,001 to $10,000", "$10,001 to $15,000", "Above $15,000"
							],
					"field":"Income",
					"size":29
				},
				{
					"type":"title",
					"text":"Politically Exposed Persons (PEP)",
					"size":20
				},
				{
					"type":"title",
					"text":"Are you, your family members or close associates entrusted with prominent public functions whether in Singapore or a foreign country? (E.g. Heads of States and of government, senior politicians, senior government, judicial, or military officials, senior executives of state owned corporations, important political party officials).<br></br>If yes, please complete the Enhanced Due Diligence for PEP Form.",
					"children":[
						{
							"type":"dropdown",
							"options":["Yes", "No"],
							"field":"dueDiligenceYesNo",
							"default":"no",
							"size":26
						}
					]
				}
			]
		}
	]
}