package picoded.JCache.embedded;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;

import org.elasticsearch.action.get.*;
import org.elasticsearch.action.update.*;
import org.elasticsearch.action.index.*;
import org.elasticsearch.action.*;
import org.elasticsearch.index.*;


import java.util.*;

///
/// Elasticsearch : Serously what were you guys thinking when making the java API
/// Seriously, what happened to KISS : Keep it simple stupid.
///
public class ElasticsearchClient {
	
	/// The actual elasticsearch server node
	public final Client client;
	
	/// The constructor wrapper to the actual client
	public ElasticsearchClient(Client inClient) {
		client = inClient;
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
}
