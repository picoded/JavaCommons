package picoded.JCache.embedded;

import org.elasticsearch.client.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.update.*;
import org.elasticsearch.action.index.*;
import org.elasticsearch.action.*;
import org.elasticsearch.action.admin.indices.exists.indices.*;
import org.elasticsearch.action.admin.indices.create.*;
import org.elasticsearch.index.*;
import org.elasticsearch.indices.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

///
/// Elasticsearch : Serously what were you guys thinking when making the java API
/// Seriously, what happened to KISS : Keep it simple stupid.
///
public class ElasticsearchClient {
	
	/// The actual elasticsearch server node
	public final Client client;
	public final AdminClient admin;
	
	/// The constructor wrapper to the actual client
	public ElasticsearchClient(Client inClient) {
		client = inClient;
		admin = client.admin();
	}
	
	/// Inserts the JSON data into elastisearch 
	///
	/// @param  Index name to use (required)
	/// @param  Type to use (required)
	/// @param  ID to use, pass null to do an auto generated ID insertion
	/// @param  The map data to insert
	///
	/// @returns The record ID, returns the auto generated ID if it was not previously given.
	public String put(String index, String type, String id, Map<String,Object> data) {
		IndexRequestBuilder req;
		if(id != null && id.length() > 0) {
			req = client.prepareIndex(index,type,id);
		} else {
			req = client.prepareIndex(index,type);
		}
		IndexResponse res = req.setSource(data).get();
		return res.getId();
	}
	
	/// Get what you put in =)
	///
	/// @param  Index name to use (required)
	/// @param  Type to use (required)
	/// @param  ID to use required)
	///
	/// @returns The record map
	public Map<String,Object> get(String index, String type, String id) {
		try {
			GetResponse res = client.prepareGet(index,type,id).get();
			if( res.isExists() ) {
				return res.getSource();
			}
		} catch(IndexNotFoundException e) {
			// Silent index not found, this happens on no value request
		}
		return null;
	}
	
	//----------------------------------------------------------------------------
	//
	// Index handling operation
	//
	//----------------------------------------------------------------------------
	
	/// Check if index exists
	///
	/// @param  Index name to use (required)
	///
	/// @returns Boolean if it exists
	public boolean hasIndex(String index) {
		try {
			return admin.indices().exists( new IndicesExistsRequest(index) ).get().isExists();
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		} catch(ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Create index
	///
	/// @param  Index name to use (required)
	public void createIndex(String index) {
		createIndex(index, 1, 1);
	}
	
	/// Create index
	///
	/// @param  Index name to use (required)
	/// @oaran  Shards count to use
	/// @oaran  Replicas count to use
	public void createIndex(String index, int shards, int replicas) {
		Settings indexSettings = Settings.builder().put("number_of_shards", shards).put("number_of_replicas", replicas).build();
		CreateIndexRequest indexRequest = new CreateIndexRequest(index, indexSettings);
		
		client.admin().indices().create(indexRequest).actionGet();
	}
	
	/// Create index, silence IndexAlreadyExistsExceptions
	///
	/// @param  Index name to use (required)
	/// @oaran  Shards count to use
	/// @oaran  Replicas count to use
	public void createIndexIfNotExists(String index, int shards, int replicas) {
		try {
			createIndex(index, shards, replicas);
		} catch( IndexAlreadyExistsException e ) {
			// Silence
		}
	}
	
	/// Create index, silence IndexAlreadyExistsExceptions
	///
	/// @param  Index name to use (required)
	public void createIndexIfNotExists(String index) {
		createIndexIfNotExists(index, 1, 1);
	}
}
