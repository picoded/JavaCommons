package picoded.servlet.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiFunction;

import picoded.set.HttpRequestType;

/**
* Convinence class, which makes it easier to type drop in replacement for
* "BiFunction<ApiRequest, ApiResponse, ApirResponse>". Note that it is intentionally
* not a requirement to use this class for the ApiBuilder functionality. As we wanted
* to maintain the standards compatibility.
**/
public interface ApiFunction extends BiFunction<ApiRequest, ApiResponse, ApiResponse> {
}
