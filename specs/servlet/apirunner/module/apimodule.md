# API Module

ApiModule, represents a standardised interface in which a module is defined.

This is used in conjuncture of ApiBuilder, in creating standard API module endpoints.

These modules in addition, will have configurable options, such as defaults in level of authentication security, etc.

The core function it will need to support is as followed

## configSetup, and dStack commands

### configSetup
A module is required to take in a configSetup, this ideally should only be called once. By the constructor.
+ api        : ApiBuilder object to build endpoints and filters on
+ prefixPath : API prefix path before all the modules specific endpoints
+ config     : Configuration map object, used to adjust module behaviour

## AccessFilter

When supported, configuration object can include the field "AccessFilter".

This generates API filter on the API Runner system, for the particular endpoint.

This will require the API runner to be configured with CommonsPage, where it will authenticate against the user API various permission settings. Modules who support this may also by default come with several predifined combination sets.

In most cases, default would be read/write being SuperUsers group only.

The format for AccessFilter would be as followed

``` {.json}
// Config object
{
	// Object map of filters, key will represent 
	// a sub path for the filter. 
	//
	// This can include wildcard "*" or regex .*
	//
	// In event multiple paths overlap, they are evaluated
	// as a combination filter "AND"
	"AuthFilter" : {

		// Wildcard filter, apply to all endpoints for this module
		//-----------------------------------------------------------
		"*" : {
			type : "allow", // default, for a defined filter
			mode : "group", // use group level checking

			// allow super user or office admin group
			name : ["SuperUsers", "OfficeAdmin"] 
			arrayMode : "or" // by default array mode is OR
		},

		// Hypothtical members only endpoint
		//-----------------------------------------------------------
		"members/only/data" : {

			// Array mode also apply to sub filters
			arrayMode : "and",

			// Sub filters are a means to define a list 
			// of additional filters for a single endpoint
			subFilters : [

				// Allow any user with an OID wildcard
				{
					type : "allow",
					mode : "user",
					oid : "*" 
				}, 

				// Disallow one specific user
				{
					type : "disallow",
					mode : "user",
					oid : "36rPYZyAKAXtfFEXZ68pgE" 
				},

				// Group blocking
				{
					type : "disallow",
					mode : "group",

					// If both OID and name is provided,
					// they are evaluated as an OR clause
					name : "BannedUsers",
					oid : "4231YZyAKAXtfFEXZ68pgE"
				}
			]
		},

		// Public endpoint
		//-----------------------------------------------------------
		"public/data" : {
			type : "allow",
			mode : "public"
		}
	}
}


```