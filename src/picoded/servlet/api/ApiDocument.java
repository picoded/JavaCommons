package picoded.util;

import java.util.*;
import org.apache.commons.lang3.tuple.*;

/**
 * The Document class serves as an object for ease of documentation of API endpoints
 * for the generating of Swagger documentation. In the future, it may be able to port
 * in RAML documentation as well.
 *
 **/
public class ApiDocument {
	/**
	 * Attributes for Swagger
	 */
	private String nameOfFunc = "";
	private String[] tags = null;
	private String description = "";
	Request request = null;
	Response response = null;
	
	public ApiDocument(String name, String desc, String[] tags) {
		nameOfFunc = name;
		description = desc;
		this.tags = tags;
		request = new Request();
		response = new Response();
	}
	
	public ApiDocument(String name, String desc, String[] tags,
		Map<String, Triple<String, String, Boolean>> requestParams,
		Map<String, Triple<String, String, Boolean>> responseParams, String responseDesc) {
		nameOfFunc = name;
		description = desc;
		this.tags = tags;
		request = new Request(requestParams);
		response = new Response(responseParams, responseDesc);
	}
	
	public void setRequest(Request req) {
		request = req;
	}
	
	public Request getRequest() {
		return request;
	}
	
	public void setResponse(Response res) {
		response = res;
	}
	
	public Response getResponse() {
		return response;
	}
	
	public void setName(String name) {
		nameOfFunc = name;
	}
	
	public String getNameOfFunction() {
		return nameOfFunc;
	}
	
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	
	public String[] getTags() {
		return tags;
	}
	
	public void setDesc(String desc) {
		description = desc;
	}
	
	public String getDesc() {
		return description;
	}
	
	class Request {
		Map<String, Triple<String, String, Boolean>> parameters = null;
		
		public Request() {
			parameters = new HashMap<>();
		}
		
		public Request(Map<String, Triple<String, String, Boolean>> params) {
			parameters = params;
		}
		
		public void setRequestParams(Map<String, Triple<String, String, Boolean>> params) {
			parameters = params;
		}
		
		public Map<String, Triple<String, String, Boolean>> getRequestParams() {
			return parameters;
		}
	}
	
	class Response {
		Map<String, Triple<String, String, Boolean>> parameters = null;
		String description = "";
		
		public Response() {
			parameters = new HashMap<>();
		}
		
		public Response(Map<String, Triple<String, String, Boolean>> params, String desc) {
			parameters = params;
			description = desc;
		}
		
		public void setResponseParams(Map<String, Triple<String, String, Boolean>> params) {
			parameters = params;
		}
		
		public Map<String, Triple<String, String, Boolean>> getRequestParams() {
			return parameters;
		}
		
		public void setDescription(String desc) {
			description = desc;
		}
		
		public String getDesc() {
			return description;
		}
	}
}
