package picoded.JCache.embedded;

import picoded.conv.*;

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
import org.elasticsearch.index.query.*;
import org.elasticsearch.indices.*;
import org.elasticsearch.action.search.*;
import org.elasticsearch.search.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

///
/// Elasticsearch : Serously what were you guys thinking when making the java API
/// Seriously, what happened to KISS : Keep it simple stupid.
///
public class ElasticsearchClient {
	
	//----------------------------------------------------------------------------
	//
	// Constructor
	//
	//----------------------------------------------------------------------------
	
	/// The actual elasticsearch server node
	public final Client client;
	public final AdminClient admin;
	
	/// The constructor wrapper to the actual client
	public ElasticsearchClient(Client inClient) {
		client = inClient;
		admin = client.admin();
	}
	
	//----------------------------------------------------------------------------
	//
	// Data put and get
	//
	//----------------------------------------------------------------------------
	
	/// Inserts the JSON data into elastisearch 
	///
	/// @param  Index name to use (required)
	/// @param  Type to use (required)
	/// @param  ID to use, pass null to do an auto generated ID insertion
	/// @param  The map data to insert
	///
	/// @returns The record ID, returns the auto generated ID if it was not previously given.
	public String put(String index, String type, String id, Map<String, Object> data) {
		IndexRequestBuilder req;
		if (id != null && id.length() > 0) {
			req = client.prepareIndex(index, type, id);
		} else {
			req = client.prepareIndex(index, type);
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
	public Map<String, Object> get(String index, String type, String id) {
		try {
			GetResponse res = client.prepareGet(index, type, id).get();
			if (res.isExists()) {
				return res.getSource();
			}
		} catch (IndexNotFoundException e) {
			// Silent index not found, this happens on no value request
		}
		return null;
	}
	
	//----------------------------------------------------------------------------
	//
	// Index setup & handling operation
	//
	//----------------------------------------------------------------------------
	
	/// Check if index exists
	///
	/// @param  Index name to use (required)
	///
	/// @returns Boolean if it exists
	public boolean hasIndex(String index) {
		try {
			return admin.indices().exists(new IndicesExistsRequest(index)).get().isExists();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
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
		Settings indexSettings = Settings.builder().put("number_of_shards", shards).put("number_of_replicas", replicas)
			.build();
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
		} catch (IndexAlreadyExistsException e) {
			// Silence
		}
	}
	
	/// Create index, silence IndexAlreadyExistsExceptions
	///
	/// @param  Index name to use (required)
	public void createIndexIfNotExists(String index) {
		createIndexIfNotExists(index, 1, 1);
	}
	
	/// Trigger an index refresh, and wait for result
	/// Important for unit testing
	public void refreshIndex() {
		admin.indices().prepareRefresh().get();
	}
	
	/// Trigger an index refresh, and wait for result
	/// Important for unit testing
	///
	/// @param  Index name to use (required)
	public void refreshIndex(String index) {
		admin.indices().prepareRefresh(index).get();
	}
	
	//----------------------------------------------------------------------------
	//
	// Data Search : Helpers
	//
	//----------------------------------------------------------------------------
	
	protected QueryBuilder queryBuilderFromMap(Map<String, Object> query) {
		//return QueryBuilders.matchAllQuery();
		return QueryBuilders.wrapperQuery(ConvertJSON.fromMap(query));
	}
	
	protected SearchRequestBuilder getSearchRequestBuilder(String index, String type, Map<String, Object> query) {
		SearchRequestBuilder req = client.prepareSearch(index).setTypes(type);
		req.setQuery(queryBuilderFromMap(query));
		return req;
	}
	
	protected SearchRequestBuilder getSearchRequestBuilder(String index, String type, Map<String, Object> query,
		int from, int size) {
		return getSearchRequestBuilder(index, type, query).setFrom(from).setSize(size);
	}
	
	protected SearchResponse getSearchResponse(String index, String type, Map<String, Object> query, int from, int size) {
		return client.search(getSearchRequestBuilder(index, type, query, from, size).request()).actionGet();
	}
	
	protected SearchResponse getSearchResponse(String index, String type, Map<String, Object> query) {
		return client.search(getSearchRequestBuilder(index, type, query).request()).actionGet();
	}
	
	protected static List<Map<String, Object>> searchResponseToMaps(SearchResponse res) {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		SearchHit[] searchHits = res.getHits().getHits(); //Double tapping hits? lol
		for (SearchHit hit : searchHits) {
			Map<String, Object> node = hit.sourceAsMap();
			if (node != null) {
				ret.add(node);
			}
		}
		return ret;
	}
	
	protected static List<String> searchResponseToIds(SearchResponse res) {
		List<String> ret = new ArrayList<String>();
		SearchHit[] searchHits = res.getHits().getHits(); //Double tapping hits? lol
		for (SearchHit hit : searchHits) {
			String id = hit.getId();
			if (id != null) {
				ret.add(id);
			}
		}
		return ret;
	}
	
	//----------------------------------------------------------------------------
	//
	// Data Search : Actual
	//
	//----------------------------------------------------------------------------
	
	/// Does a search, and return the result count
	///
	/// @param  Index name to use (required)
	/// @param  Type to use (required)
	/// @param  Elasticsearch query map
	///
	/// @returns The search count
	public long getSearchCount(String index, String type, Map<String, Object> query) {
		return getSearchResponse(index, type, query).getHits().totalHits();
	}
	
	/// Does a search, and return the result maps
	///
	/// @param  Index name to use (required)
	/// @param  Type to use (required)
	/// @param  Elasticsearch query map
	/// @param  From index
	/// @param  Search size
	///
	/// @returns The record map list
	public List<Map<String, Object>> getSearchMaps(String index, String type, Map<String, Object> query, int from,
		int size) {
		return searchResponseToMaps(getSearchResponse(index, type, query, from, size));
	}
	
	/// Does a search, and return the result id
	///
	/// @param  Index name to use (required)
	/// @param  Type to use (required)
	/// @param  Elasticsearch query map
	/// @param  From index
	/// @param  Search size
	///
	/// @returns The id list
	public List<String> getSearchMapIds(String index, String type, Map<String, Object> query, int from, int size) {
		return searchResponseToIds(getSearchResponse(index, type, query, from, size));
	}
	
	//----------------------------------------------------------------------------
	//
	// Data Aggregation
	//
	//----------------------------------------------------------------------------
	
}
