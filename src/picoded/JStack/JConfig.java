package picoded.JStack;

import picoded.fileUtils.ConfigFileSet;

///
/// Important note:
///
/// The following documentation is copied from the original jConfig, in servlet commons. AND IS NOT implemented.
/// This class is suppose to bridge ConfigFileSet with JStack layers. to allow hybrid of DB and file config values
/// While white listing / black listing value sets either in FILES only. (like sql connection property)
///
/// Additional feature implementation is parameter substitution
///
/// Example:
/// sys.db.sqlType = mysql
/// sys.db.$(sqlType).host -> is equivalent to -> sys.db.mysql.host
///
/// *******************************************************************************
///
/// [Non-ideal implementation : But it works!]
/// jConfig is a multilayered configuration loader approach. That is able to load
/// configuration options from a multitude of sources. Including but not exclusively
/// to ini, xml files, jSql (sqlite or remote sql) and System.properties.
///
/// jConfig are setup in a "builder pattern chain", and are read [LIFO:Last-In-First-Out]( http://en.wikipedia.org/wiki/LIFO_%28computing%29 )
/// Additionally jConfig, fails silently if a datasource fails (no file)
///
/// All key values pairs are stored and read strings
///
/// *******************************************************************************
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
/// //[CURRENT IMPLEMENTATION : Non-ideal implementation : But it works!]
/// jConfig jcObj = new jConfig().pushJConfigLite( jclObj2 )    //read 2nd
///                              .pushJConfigLite( jclObj1 )    //read 1st
///
/// String sVal = jcObj.getString("configCategory.value", "defaultValue");
/// int iVal = jcObj.getInt("configCategory.ivalue");
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// *******************************************************************************
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
/// //[CONCEPTUAL FORMAT: THIS IS NOT YET IMPLEMENTED]
/// jConfig jcObj = new jConfig().pushXml("./defaults.xml", "subdomain")            //read 4th
///                              .pushJsql( new jSql("./sqliteFile"), "cTable" )    //read 3rd
///                              .pushSystemProperties("appname.config.sub", "sub") //read 2nd
///                              .pushIni("./main.ini")                             //read 1st
///
///                              // unshift does the opposite end of push, inserts at the front, read 5th/last
///                              .unshiftXml("./defaults.xml");
///
/// String sVal = jcObj.getString("configCategory.value", "defaultValue");
/// int iVal = jcObj.getInt("configCategory.ivalue");
///
/// // In addition this facilitates complex configuration setup via the usage of "filter" / "prefix" options
/// // which can be set directly durring a push, or chained in a builder pattern chain
/// jcObj = new jConfig().pushIni("conA.ini","x.y","r") //x.y is the filter, k.z is the prefix
///                      .pushSystemProperties("appname.conB", "x.y.p")
///                      .pushIni("conC.ini").filter("x.y")
///                      .pushXml("conD.xml");
///
/// sVal = jcObj.getStr("x.y.k.z", "finalFallback");
/// // A get operation (such as above) will search the value in the following order
/// // At any step if a non-null result is found. It is returned, and additional steps are skipped
/// // "conD.xml" is searched 1st for "x.y.k.z"
/// // "conC.ini" is searched 2nd for "k.z", as "x.y" is filtered out
/// // "appName.conB" is skipped, as "x.y.p" filter does not match the request "x.y.k.z"
/// // "conA.ini" is searched 3rd for "r.k.z" as "x.y" is filtered out, and "r" is prefixed
/// // "finalFallback" is finally returned when all else fails.
///
/// // jConfig are also able to clone a sub section itself with additional "filters"/"prefix" pre applied, and converted as needed
/// // In event both is given, filters are applied first, then prefixes are added.
///
/// jConfig jaObj = jcObj.createSubSection( "x.y.k" );
/// // This is equivalent to ...
/// // jaObj = new jConfig().pushIni("conA.ini", "", "r.k")
/// //                      //.pushSystemProperties is skipped, as its filters does not match
/// //                      .pushIni("conC.ini", "", "k")
/// //                      .pushXml("conD.xml", "", "x.y.k")
///
/// // or alternatively
/// jConfig jbObj = jcObj.clone().applyFilter( "x.y.k" );
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// *******************************************************************************
///
/// Technical Note: jConfigLite does the actual interfacing with the various datasource,
/// including loading and caching of data. While jConfig handles the fallback lookup
/// along the value source chain.
///
/// *******************************************************************************
///
/// TODO list
/// * [future] Conceptual concept & example code to write / export configuration files
///
public class JConfig extends ConfigFileSet {
	
}
