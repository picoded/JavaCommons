package picoded.web;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

import picoded.conv.ConvertJSON;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertMap;

public interface ResponseHttp {

	///////////////////////////////////////////////////
	// Async Http Request wait handling
	// (implment if needed)
	///////////////////////////////////////////////////

	/**
	* Wait for completed header request, called automatically
	* when getting InputStream / cookies / headers
	**/
	public default void waitForCompletedHeaders() {
	};

	/**
	* Wait for completed request, called automatically
	* when using toString / toMap
	**/
	public default void waitForCompletedRequest() {
	};

	///////////////////////////////////////////////////
	// Response handling
	///////////////////////////////////////////////////

	/**
	* Gets the response content
	**/
	public default InputStream inputStream() {
		return null;
	};

	/**
	* Gets the response content as a string
	**/
	public String toString();

	/**
	* Converts the result string into a map, via JSON's
	**/
	public default GenericConvertMap<String, Object> toMap() {
		waitForCompletedRequest();

		String r = toString();
		if (r == null || r.length() <= 1) {
			return null;
		}

		Map<String, Object> rMap = ConvertJSON.toMap(r);
		if (rMap == null) {
			return null;
		} else {
			return ProxyGenericConvertMap.ensure(rMap);
		}
	};

	/**
	* Gets the response code
	**/
	public default int statusCode() {
		return -1;
	};

	/**
	* Gets the header map.
	**/
	public default Map<String, String[]> headersMap() {
		return null;
	};

	/**
	* Gets the cookies map.
	**/
	public default Map<String, String[]> cookiesMap() {
		return null;
	};

	///////////////////////////////////////////////////
	// Websocket handling
	// (implment if needed)
	///////////////////////////////////////////////////

	/**
	* indicates if the connection is a websocket
	**/
	public default boolean isWebsocket() {
		return false;
	}

	/**
	* indicates if the websocket is currently connected
	**/
	public default boolean isWebsocketConnected() {
		return false;
	}

	/**
	* Closes the websocket, if it is not closed yet
	**/
	public default void websocketClose() {
		throw new UnsupportedOperationException(
			"Use RequestHttp.websocket to support websocket operations");
	}

	/**
	* Sets the message handler lisenter. This will cause an exception,
	* if the previous handler exists, see replaceMessageHandler if a
	* replacement is intended.
	*
	* @param    handler, the message handler listener
	*
	* @return   the previous handler if set, if replace is enabled
	**/
	public default void setMessageHandler(Consumer<String> handler) {
		if (replaceMessageHandler(handler) != null) {
			throw new RuntimeException(
				"Previous handler exists, if this is intended use replaceMessageHandler");
		}
	}

	/**
	* Set/Replaces the existing message handler lisenter.
	* This allows a replacement without throwing an exception
	*
	* @param    handler, the message handler listener
	*
	* @return   the previous handler if set, if replace is enabled
	**/
	public default Consumer<String> replaceMessageHandler(Consumer<String> handler) {
		throw new UnsupportedOperationException(
			"Use RequestHttp.websocket to support websocket operations");
	};

	/**
	* Gets the currently set message handler
	**/
	public default Consumer<String> getMessageHandler() {
		throw new UnsupportedOperationException(
			"Use RequestHttp.websocket to support websocket operations");
	}

	/**
	* sends a message via websocket
	**/
	public default void sendMessage(String message) {
		throw new UnsupportedOperationException(
			"Use RequestHttp.websocket to support websocket operations");
	}

	/**
	* Sends a message, and intercepts the next immediate response.
	* Note that the intercepted message does not get sent to the handler.
	*
	* Do note that if multiple "processes" are using a single websocket
	* be warry of race conditions that may occur.
	**/
	public default String sendAndWait(String message) {
		throw new UnsupportedOperationException(
			"Use RequestHttp.websocket to support websocket operations");
	}

}
