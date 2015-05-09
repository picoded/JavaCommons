package picoded.JStack;

// Picoded imports

/// JStackLayer provides the interface which JSql, and JCache extends from to support
public class JStackLayer {
	
	// Store the actual namespace
	protected String namespace = null;
	
	// Returns the JStack configured namespace, this is optional
	public String getNamespace() {
		return (namespace == null) ? "" : namespace;
	}
	
	// Sets the namespace, this can only be done once, throws an exception on repeated attempts
	public void setNamespace(String name) {
		if (name != null && (name = name.trim()).length() <= 0) {
			name = null;
		}
		
		if (namespace.equals(name)) {
			//does nothing, already valid
		} else if (namespace != null) {
			//Existing namespace already set, and is different : throw exception
			throw new RuntimeException("invalid setNamespace(" + name + "), Namespace is already set to: " + namespace);
		}
	}
	
}