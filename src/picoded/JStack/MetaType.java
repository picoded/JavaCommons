package picoded.JStack;

/// Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;

import org.aspectj.weaver.patterns.ThisOrTargetAnnotationPointcut;

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
	/// 1      - Disabled
	public static int TYPE_NULL = 1;
	/// 2      - Mixed
	public static int TYPE_MIXED = 1;
	/// 3      - [Mixed]
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
	protected int valueType = 1;
	
	private static String metaTypePrefix = "type_";
	
	/// Configured type
	public int valueType() {
		return valueType;
	}
	
	/// The meta config string, applicable to only certain meta types
	protected String valueConfig = null;
	
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
	
	public static MetaType fromTypeString(String configName) {
		String name = configName.toLowerCase();
		if (name.contains(metaTypePrefix)) {
			name = name.substring(name.indexOf('_') + 1, name.length());
		}
		
		switch (name) {
		case "disabled":
			return new MetaType(MetaType.TYPE_DISABLED);
		case "mixed":
			return new MetaType(MetaType.TYPE_MIXED);
		case "mixed_array":
			return new MetaType(MetaType.TYPE_MIXED_ARRAY);
		case "uuid":
			return new MetaType(MetaType.TYPE_UUID);
		case "metatable":
			return new MetaType(MetaType.TYPE_METATABLE);
		case "json":
			return new MetaType(MetaType.TYPE_JSON);
		case "uuid_array":
			return new MetaType(MetaType.TYPE_UUID_ARRAY);
		case "metatable_array":
			return new MetaType(MetaType.TYPE_METATABLE_ARRAY);
		case "json_array":
			return new MetaType(MetaType.TYPE_JSON_ARRAY);
		case "integer":
			return new MetaType(MetaType.TYPE_INTEGER);
		case "long":
			return new MetaType(MetaType.TYPE_LONG);
		case "double":
			return new MetaType(MetaType.TYPE_DOUBLE);
		case "float":
			return new MetaType(MetaType.TYPE_FLOAT);
		case "integer_array":
			return new MetaType(MetaType.TYPE_INTEGER_ARRAY);
		case "long_array":
			return new MetaType(MetaType.TYPE_LONG_ARRAY);
		case "double_array":
			return new MetaType(MetaType.TYPE_DOUBLE_ARRAY);
		case "float_array":
			return new MetaType(MetaType.TYPE_FLOAT_ARRAY);
		case "string":
			return new MetaType(MetaType.TYPE_STRING);
		case "text":
			return new MetaType(MetaType.TYPE_TEXT);
		case "string_array":
			return new MetaType(MetaType.TYPE_STRING_ARRAY);
		case "text_array":
			return new MetaType(MetaType.TYPE_TEXT_ARRAY);
		}
		return null;
	}
	
	public static MetaType fromTypeObject(Object type) {
		MetaType mType = null;
		if(type instanceof String) {
			mType = MetaType.fromTypeString(type.toString());
			
			if( mType == null ) {
				throw new RuntimeException("Invalid MetaTable type for: "+type.toString());
			}
		} else if(type instanceof MetaType) {
			mType = (MetaType)type;
		}
		return mType;
	}
}
