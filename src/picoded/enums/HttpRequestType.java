package picoded.enums;

/// HttpRequestType enum, used in webUtils, servletUtils, servlet, and RESTBuidler
public enum HttpRequestType {
	GET,
	POST,
	PUT,
	DELETE,
	HEAD,
	OPTION;
	
	/// TypeMap to be extended, and stored in their respective package usage
	public static class HttpRequestTypeSet {
		public static HttpRequestType GET  = HttpRequestType.GET;
		public static HttpRequestType POST = HttpRequestType.POST;
		public static HttpRequestType PUT  = HttpRequestType.PUT;
		public static HttpRequestType DELETE = HttpRequestType.DELETE;
		public static HttpRequestType OPTION = HttpRequestType.OPTION;
	}
	
	/// Byte to enum serialization
	public static HttpRequestType byteToEnum(byte val){
		switch(val){
			case 0: return HttpRequestType.GET;
			case 1: return HttpRequestType.POST;
			case 2: return HttpRequestType.PUT;
			case 3: return HttpRequestType.DELETE;
			case 4: return HttpRequestType.OPTION;
		}
		return null;
	}
	
	/// Enum to byte serialization
	public static byte enumToByte(HttpRequestType val){
		switch(val){
			case GET: return 0;
			case POST: return 1;
			case PUT: return 2;
			case DELETE: return 3;
			case OPTION: return 4;
		}
		return -1;
	}
}
