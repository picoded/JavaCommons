package picoded.RESTBuilder.template.site;

import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.RESTFunction;
import picoded.enums.HttpRequestType;
import picoded.JStruct.*;

///
/// Provides a shopping cart and inventory API
/// All in a single API package.
///
public class SimpleShoppingCart {
	
	//---------------------------------------------------------------------------------------
	//
	// Class variabels
	//
	//---------------------------------------------------------------------------------------
	
	/// User login account table system used
	public AccountTable user = null;
	
	/// Inventory owner metatable
	public MetaTable inventoryOwner = null;
	
	/// Inventory listing
	public MetaTable inventoryItems = null;
	
	/// Atomic inventory counting
	public AtomicLongMap inventoryCount = null;
	
	
	
	//---------------------------------------------------------------------------------------
	//
	// Constructor options
	//
	//---------------------------------------------------------------------------------------
	
	/// Empty constructor
	public SimpleShoppingCart() {
		//Does nothing
	}
}
