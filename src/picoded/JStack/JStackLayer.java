package picoded.JStack;

// Picoded imports

/// JStackLayer provides the interface which JSql, and JCache extends from to support
public class JStackLayer {
	
	// Store the actual tablePrefix
	protected String tablePrefix = null;
	
	/// Returns the JStack configured tablePrefix, this is optional
	/// Note that tablePrefix refers to the prefix used in the storage on DB. This is not use for sharding
	public String getTablePrefix() {
		return (tablePrefix == null) ? "" : tablePrefix;
	}
	
	/// Sets the tablePrefix, this can only be done once, throws an exception on repeated attempts
	public void setTablePrefix(String name) {
		if (name != null && (name = name.trim()).length() <= 0) {
			name = null;
		}
		
		if (tablePrefix.equals(name)) {
			//does nothing, already valid
		} else if (tablePrefix != null) {
			//Existing tablePrefix already set, and is different : throw exception
			throw new RuntimeException("invalid settablePrefix(" + name + "), tablePrefix is already set to: "
				+ tablePrefix);
		}
	}
	
}