/// objectSetDB setsup the foundation for a structured object sets database system
///
/// Actual data storage is based on picoded.jCache (for caching) and picoded.jSql (for data persistance).
///
/// This is used to get the benefits of NoSQL movement, while still complying with the "requirment" for SQL
/// found in several larger coporate environments. While seting up an "easy to use and extend" DB
///
/// Some core principles behind the system design that applies to all data (except LOBS)
///
/// + Cache everything : For performance
/// + Index everything : For performance
/// + ACID everything : For reliability (incomplete, requires atleast a JCache to ensure locking)
/// + Reuse SQL and NOSQL : For compliance
/// + Relative Object relation : For ease of use, and super joins
/// + Flexible Object Structure : For rapid iterations
/// + Every object has a GUID/ID : For ID purposes, can be replaced with manual ID's
/// + Every object belongs to a collection set : For Object Structure enforcement
/// + Works using just java.util.Map interface : For ease of use
/// + Linked object sets : For all your complex hirachy needs
/// + Key values are alphanumeric, with underscore/dash/spaces/dots/slashes only : Ensure consistancy across all storage layers
///
/// ## jCache notes
///
/// Additionally, one of the key decision factors was to store each object (from an objectset) entirely
/// (without the LOBS data), and fetch it respectively in its entire form from jCache, even if only a
/// single attribute will eventually be fetched. This may seem counterintuitive, however the main key
/// decision for this came from bench marking done on picodedTests.libs.HazelcastMetaMapStructure_test
/// showing a huge factor in performance improvement when doing a complete object in most of the use
/// cases where multiple attributes per object is fetched.
///
/// Long story short, the overhead to fetch any key-value pair from jCache, far outweights the transmission
/// size and serialization / deserialization of the object data for 99% of the use cases not involving LOBS.
///
/// ## jCache notes
///
/// @TODO Performance optimization: Allow caching of null values?, by introducing an internal NULLOBJ, to pass as "NULL"
/// @TODO Performance optimization: Ignore cache values update, if value did not change
/// @TODO JCache, and JSql locking for ACID compliance
/// @TODO Key Value, alpha numeric, with underscore/dash/spaces/....
/// @TODO LOBS support layer for JSql
/// @TODO LOBS support layer for JSql, and file system hybrid
/// @TODO LOBS support layer for JSql, and AWS S3
///
package picoded.objectSetDB;

