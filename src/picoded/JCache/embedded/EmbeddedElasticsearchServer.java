package picoded.JCache.embedded;

import picoded.fileUtils.*;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;

import java.io.File;
import java.io.IOException;

/// Elasticsearch : The embdedded cache killer
public class EmbeddedElasticsearchServer {
	
	/// The actual elasticsearch server node
	protected final Node elasticNode;
	
	/// The actual data directory used by elastisearch
	protected final File dataDirectory;
	
	///
	/// @param  The cluster name
	/// @param  The HTTP API port if required, use -1 to disable, use 0 for auto
	/// @param  Persistent storage location, use NULL to use pure in-memory (not recommended)
	/// @param  Restrict the server to local cluster mode, meaning no cluster networking
	/// 
	public EmbeddedElasticsearchServer(String clustername, int port, File storage, boolean localCluster) {
		Settings.Builder elasticsearchSettings = Settings.builder();
		
		//
		// Setup config file
		//
		if( clustername != null && clustername.length() > 0 ) {
			elasticsearchSettings.put("cluster.name", clustername);
		}
		
		if( port < 0 ) {
			elasticsearchSettings.put("http.enabled", "false");
		} else if( port > 0 ) {
			elasticsearchSettings.put("http.port", port);
			elasticsearchSettings.put("http.enabled", "true");
		} else { // (port == 0)
			// Automate the port assignment : 9200 and above
			// elasticsearchSettings.put("http.port", port);
			elasticsearchSettings.put("http.enabled", "true");
		}
		
		// Node storage
		if( storage != null ) {
			elasticsearchSettings.put("path.data", storage.getAbsolutePath() );
			dataDirectory = storage;
		} else {
			dataDirectory = null;
		}
		
		// Local mode only
		if( localCluster ) {
			elasticsearchSettings.put("node.local", "false");
		} else {
			elasticsearchSettings.put("node.local", "true");
		}
		
		//
		// Setup elastic node
		//
		
		// Apply config, build node, and start
		elasticNode = new Node(elasticsearchSettings.build());
		elasticNode.start();
		
	}
	
	//
	// Elastic search client 
	//
	public Client client() {
		return elasticNode.client();
	}
	
	public boolean isClosed() {
		return elasticNode.isClosed();
	}
	
	public void close() {
		if(!isClosed()) {
			try {
				elasticNode.close();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void closeAndDelete() {
		close();
		
		if( dataDirectory == null ) {
			throw new RuntimeException("No data directory set by constructor");
		}
		
		try {
			FileUtils.deleteDirectory(dataDirectory);
		} catch (IOException e) {
			throw new RuntimeException("Could not delete data directory of embedded elasticsearch server", e);
		}
	}
}
