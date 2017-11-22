package picoded.core.common;

// Third party imports
import org.apache.commons.lang3.RandomUtils;

/**
 * Provides a common interface for classes to have a standardised means of
 * backend setup, destruction, and maintainance calls
 *
 * + setup
 * + destroy
 * + maintenance
 * + incrementalMaintenance
 * + clear
 *
 */
public interface SystemSetupInterface extends AutoCloseable {
	
	//--------------------------------------------------------------------------
	//
	// Backend system setup / teardown / maintenance
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Does onetime set up of the backend storage. If needed.
	 * The SQL equivalent would be "CREATE TABLE {TABLENAME} IF NOT EXISTS"
	 **/
	default void systemSetup() {
		// Does nothing, needs implementation
	}
	
	/**
	 * Destroy, Teardown and delete the backend storage. If needed
	 * The SQL equivalent would be "DROP TABLE {TABLENAME}"
	 **/
	default void systemDestroy() {
		// Does nothing, needs implementation
	}
	
	/**
	 * Perform maintenance, this is meant for large maintenance jobs.
	 * Such as weekly or monthly compaction. It may or may not be a long
	 * running task, where its use case is backend specific.
	 *
	 * A common example is stop the world cleanup for a database backend
	 **/
	default void maintenance() {
		// Does nothing, needs implementation
	}
	
	/**
	 * Perform increment maintenance, meant for minor changes between requests. If called.
	 *
	 * By default this randomly triggers a maintenance call with 2% probability.
	 * The main reason for doing so, is that for many implmentations there may not be
	 * a concept of incremental maintenance, and in many cases its implementor may forget
	 * to actually call a maintenance call. For years.
	 *
	 * Unless the maintenance call is too expensive, (eg more then 2 seconds), having
	 * it randomly trigger and slow down one transaction randomly. Helps ensure everyone,
	 * systems is more performant in overall.
	 *
	 * It is a very controversal decision, however as awsome as your programming or
	 * devops team is. Your client and their actual infrastructure may be "not as awesome"
	 *
	 * In other cases there might be small incremental maintenance that can be done,
	 * speicifc to a module, in which this call works as intended.
	 **/
	default void incrementalMaintenance() {
		// 2 percent chance of trigering maintenance
		// This is to lower to overall performance cost incrementalMaintenance per request
		int num = RandomUtils.nextInt(0, 100);
		if (num <= 1) {
			maintenance();
		}
	}
	
	//--------------------------------------------------------------------------
	//
	// Clearing of data
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Removes all data, without tearing down setup
	 *
	 * This is equivalent of "TRUNCATE TABLE {TABLENAME}"
	 **/
	default void clear() {
		// Does nothing, needs implementation
	}
	
	//--------------------------------------------------------------------------
	//
	// Does the closure of any underlying connection if needed
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Perform any required connection / file handlers / etc closure
	 * This is to clean up any "resource" usage if needed.
	 * 
	 * Particularly important for JSQL backend implementation (for example)
	 * 
	 * Note that unlike the "AutoCloseable" specification. This is REQUIRED
	 * to be indepotent, as it will be called multiple times
	 * 
	 * Also as per our "standard" its exception type is limited to RuntimeException
	 * This reduces the "warning" for possible interrupt exception types in java.
	 */
	default void close() {
		// Does nothing
	}
	
	//--------------------------------------------------------------------------
	//
	// Configuration map
	//
	//--------------------------------------------------------------------------
	
	// NOTE: Until a use case for this is found, config getter is commented out
	//
	// /**
	// * Configuration getting, that was used in configSetup
	// *
	// * @return GenericConvertMap<String,Object> representing the current config
	// */
	// default GenericConvertMap<String,Object> config() {
	// 	throw new UnsupportedOperationException("Missing the respective config implementation");
	// }
}