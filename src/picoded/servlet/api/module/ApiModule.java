package picoded.servlet.api.module;

import picoded.servlet.api.*;
import picoded.core.common.SystemSetupInterface;

/**
 * The ApiModule interface template, an extension of SystemSetupInterface
 **/
public interface ApiModule extends SystemSetupInterface {

	/**
	 * Setup the API module, with the given parameters. And register the relevant end points
	 *
	 * @param  api         ApiBuilder to add the required functions
	 * @param  prefixPath  prefix to assume as (should be able to accept "" blanks)
	 * @param  config      configuration object, assumed to be a map. use GenericConvert.toStringMap to preprocess the data
	 */
	public void apiSetup(ApiBuilder inApi, String inPrefixPath, Object inConfigMap);

}
