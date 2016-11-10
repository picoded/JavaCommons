package picoded.JCache.embedded;

import picoded.file.*;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;

import java.io.File;
import java.io.IOException;

/// Elasticsearch : The embdedded cache killer
public class EmbeddedElasticsearchServer {
	
	/// The actual elasticsearch server node
	public final Node elasticNode;
	
	/// The actual data directory used by elastisearch
	public final File homeDirectory;
	
	///
	/// @param  The cluster name
	/// @param  The HTTP API port range if required, use null for default 9200/9300
	/// @param  Persistent inHomeDir location, use NULL to use pure in-memory (not recommended)
	/// @param  Restrict the server to local cluster mode, meaning no cluster networking
	/// 
	public EmbeddedElasticsearchServer(String clustername, String portRange, File inHomeDir,
		boolean localCluster) {
		Settings.Builder elasticsearchSettings = Settings.builder();
		
		//
		// Setup config file
		//
		if (clustername != null && clustername.length() > 0) {
			elasticsearchSettings.put("cluster.name", clustername);
		}
		
		if (portRange == null) {
			// Automate the port assignment : 9200 (http) and 9300 (protocall)
			elasticsearchSettings.put("http.port", "9200-9300");
			elasticsearchSettings.put("http.enabled", true);
		} else {
			elasticsearchSettings.put("http.port", portRange);
			elasticsearchSettings.put("http.enabled", true);
		}
		
		// Node inHomeDir
		if (inHomeDir != null) {
			// Save the directory
			homeDirectory = inHomeDir;
			
			// Ensure data folder is valid
			File dataDir = (new File(inHomeDir, "data"));
			dataDir.mkdirs();
			
			// Save the file path to config
			elasticsearchSettings.put("path.home", inHomeDir.getAbsolutePath());
			elasticsearchSettings.put("path.data", dataDir.getAbsolutePath());
		} else {
			homeDirectory = null;
		}
		
		// Local mode only
		if (localCluster) {
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
	
	//
	// The more useful client wrapper
	//
	public ElasticsearchClient ElasticsearchClient() {
		return new ElasticsearchClient(client());
	}
	
	//
	// Indicate if the elastic node is clsoed
	//
	public boolean isClosed() {
		return elasticNode.isClosed();
	}
	
	//
	// Closes the elastic node if it was not already closed
	//
	public void close() {
		if (!isClosed()) {
			try {
				elasticNode.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	//
	// Closes and delete the elastic node data
	//
	public void closeAndDelete() {
		close();
		
		if (homeDirectory == null) {
			throw new RuntimeException("No data directory set by constructor");
		}
		
		try {
			FileUtil.deleteDirectory(homeDirectory);
		} catch (IOException e) {
			throw new RuntimeException(
				"Could not delete data directory of embedded elasticsearch server", e);
		}
	}
}
