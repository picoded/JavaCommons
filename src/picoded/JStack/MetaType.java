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

/// Internal usage, used to setup the various meta indexes types
public class MetaType {
	
	///
	/// Static index values type
	///--------------------------------------------------------------------------
	
	/// 0      - Disabled
	public static int TYPE_DISABLED = 0;
	/// 1      - Mixed
	public static int TYPE_MIXED = 1;
	/// 2      - [Mixed]
	public static int TYPE_MIXED_ARRAY = 2;
	
	/// 11     - UUID
	public static int TYPE_UUID = 11;
	/// 12     - MetaTable
	public static int TYPE_METATABLE = 12;
	/// 13     - JSON based object
	public static int TYPE_JSON = 13;
	
	/// 21     - [UUID]
	public static int TYPE_UUID_ARRAY = 21;
	/// 22     - [MetaTable]
	public static int TYPE_METATABLE_ARRAY = 22;
	/// 23     - [JSON]
	public static int TYPE_JSON_ARRAY = 23;
	
	/// 31     - Integer
	public static int TYPE_INTEGER = 31;
	/// 32     - Long
	public static int TYPE_LONG = 32;
	/// 33     - Double
	public static int TYPE_DOUBLE = 33;
	/// 34     - Float
	public static int TYPE_FLOAT = 34;
	
	/// 41     - [Integer]
	public static int TYPE_INTEGER_ARRAY = 41;
	/// 42     - [Long]
	public static int TYPE_LONG_ARRAY = 42;
	/// 43     - [Double]
	public static int TYPE_DOUBLE_ARRAY = 43;
	/// 44     - [Float]
	public static int TYPE_FLOAT_ARRAY = 44;
	
	/// 51     - String
	public static int TYPE_STRING = 51;
	/// 52     - Text
	public static int TYPE_TEXT = 52;
	/// 61     - [String]
	public static int TYPE_STRING_ARRAY = 61;
	/// 62     - [Text]
	public static int TYPE_TEXT_ARRAY = 62;
	
	///
	/// Static index values type
	///--------------------------------------------------------------------------
	
	/// The meta type int values
	int valueType = 1;
	
	/// Configured type
	public int valueType() {
		return valueType;
	}
	
	/// The meta config string, applicable to only certain meta types
	String valueConfig = null;
	
	/// Configured string
	public String valueConfig() {
		return valueConfig;
	}
	
	/// Note this constructor is not meant to be used directly.
	public MetaType(int inType, String inConfig) {
		valueType = inType;
		valueConfig = inConfig;
	}
	
	/// Note this constructor is not meant to be used directly.
	public MetaType(int inType) {
		valueType = inType;
	}
	
	/// Note this constructor is not meant to be used directly.
	public MetaType() {
		
	}
}