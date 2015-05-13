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
public class MetaTypes {
	
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
	
	/// 21     - [UUID]
	public static int TYPE_UUID_ARRAY = 21;
	/// 22     - [MetaTable]
	public static int TYPE_METATABLE_ARRAY = 22;
	
	/// 31     - Integer
	public static int TYPE_INTEGER = 21;
	/// 32     - Long
	public static int TYPE_LONG = 22;
	/// 33     - Double
	public static int TYPE_DOUBLE = 23;
	/// 34     - Float
	public static int TYPE_FLOAT = 24;
	
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
	public int meta_type = 1;
	
	/// The meta config string, applicable to only certain meta types
	public String meta_config = null;
	
	/// Note this constructor is not meant to be used directly.
	public void MetaTypes(int inType, String inConfig) {
		meta_type = inType;
		meta_config = inConfig;
	}
	
	/// Note this constructor is not meant to be used directly.
	public void MetaTypes(int inType) {
		meta_type = inType;
	}
	
	/// Note this constructor is not meant to be used directly.
	public void MetaTypes() {
		
	}
}