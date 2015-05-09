package picoded.JStack;

/// Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;

/// Picoded imports
import picoded.JSql.*;
import picoded.JCache.*;

/// hazelcast
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

///
public class MetaTable {
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// Internal JStackObj
	protected JStack JStackObj = null;
	
	/// Internal table name, before prefix?
	protected String tableName = "MetaTable";
	
	/// Setup the metatable with the given stack
	public MetaTable(JStack inStack, String inTableName) {
		JStackObj = inStack;
		tableName = inTableName;
	}
	
	///
	/// Internal config vars
	///--------------------------------------------------------------------------
	
	/// Object ID field type
	protected String objColumnType = "VARCHAR(50)";
	
	/// Key name field type
	protected String keyColumnType = "VARCHAR(50)";
	
	/// Type collumn type
	protected String typeColumnType = "tinyint";
	
	/// Index collumn type
	protected String indexColumnType = "tinyint";
	
	/// String value field type
	/// @TODO: Investigate performance issues for this approach
	protected String numColumnType = "DECIMAL(22,12)";
	
	/// String value field type
	protected String strColumnType = "VARCHAR(500)";
	
	///
	/// JStack setup functions
	///--------------------------------------------------------------------------
	
	protected void JSqlSetup(JSql sql) throws JSqlException {
		
		sql.createTableQuerySet( //
			JStackObj.namespace + tableName, //
			new String[] { //
			"oID", //objID
				"kID", //key storage
				"idx", //index collumn
				"typ", //type collumn
				"nVl", //numeric value (if applicable)
				"sVl" //string value (if applicable)
			}, //
			new String[] { //
			objColumnType, //
				keyColumnType, //
				indexColumnType, //
				typeColumnType, //
				numColumnType, //
				strColumnType //
			} //
			).execute();
		
		//sql.createTableIndexQuerySet(
		
		//)
	}
}