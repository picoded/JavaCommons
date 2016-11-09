package picoded.JCache;

/// JSql type options enum, see JSql.sqlType for its usage
public enum JCacheType {
	/// invalid type (reserved for base class)
	invalid,
	
	/// hazelcast
	hazelcast,
	
	/// redis
	redis,
	
	/// Others
	others
}
