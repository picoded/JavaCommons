///
/// # dstack : THE Data Stack
///
/// Picoded answer to NoSQL : To have a single NoSQL interface, 
/// that works against as many backends we thrown at it.
///
/// Including SQL, Ram, FileSystem, S3?
///
/// The general idea is that one should be using one standard interface, which serves 99% of our
/// data storage / persistency use cases. Allowing the level of caching, and persistency to be controlled,
/// at a dev-ops / configuration layer. And not at the application layer.
///
/// Hence its concept of mixing and matching via "data stacks". Gaining the advantage / disadvantage,
/// of each backend layer you deploy with. With a very strong emphasis of making it work, even at
/// a performance loss, if there are no other layers.
///
/// No query for your MetaTable backend? no worries, we will scan ALL the objects D=
///
/// However beyond that, it forms the concept of structually having a few core NoSQL data system,
/// which serves as a foundation for much much larger data persistency need.
///
/// With more complex needs (such as user accounts) being served via modules.
///
/// ----------------------------------------------------------------------------------------------------
///
/// ## So what are the core data structures?
///
/// |------------------|----------------------------------------------------------------------|
/// | KeyValue         | Key to string value mapping. Used for temporary values storage       |
/// | MetaTable        | Key to object map storage, used primarily for "table" like data      |
/// | AtomicLongTable  | Used for handling long values where atomic consistency is required   |
/// |------------------|----------------------------------------------------------------------|
///
/// ## Are there any other data structures in consideration?
///
/// |------------------|----------------------------------------------------------------------|
/// | ByteStream       | Meant to be a drop in replacement for file input/output streams.     |
/// |                  | For large streams that can reach 100's of MB, or even GB's.          |
/// |                  | Or simply just files. Functions similarly to KeyValue                |
/// |------------------|----------------------------------------------------------------------|
///
/// ----------------------------------------------------------------------------------------------------
/// 
/// # The many layers of "Data Stack Level"
///
/// Throughout the documentation you will find multiple implmentation of the dstack interfaces starting
/// with a mention on its target "Data Stack level". This is meant to show what level a particular,
/// implmentation is focused on for. And a hint on where it should be arranged in the data stack.
///
/// In concept data flows from L6 to L0, from the global source of truth, to local session.
///
/// In practice most production systems only do an L3 to L0.
///
/// ## Level 0 : Local variable
///
/// The lowest level, this refers to session variables, like a local variable. Note that conceptually 
/// this is meant for a single session via a single API call. 
/// 
/// A websocket with multiple API calls is kinda a level 0.5 which complciates things.
/// So please do not implement such a thing, use http/2 instead.
///
/// Due to their very short lived nature, usually no persistency is provided, 
/// and cache staling is not an issue.
///
/// ## Level 1 : Shared memory space
///
/// Variables that persist across multiple sessions, from this point onwards cache staling
/// especially on multiple servers start being an issue if not handled properly.
///
/// In production, this is normally for write-once-read-multiple-times kind of data.
/// or with proper expirary settings, configuration settings.
///
/// At this point, data is still not persistent, especially across server reboots.
///
/// ## Level 2 : Local resillience
///
/// This is where some form of local resillience / persistency is introduced. Through the use of local data
/// storage. Such as the local file system. This data however has as much gurantee as its implementation,
/// with the local file system.
///
/// A not so good example is a generated PDF file in a webserver, for one time return.
/// While it is persistent in that webserver being a File. It is not persistent across multiple webserver.
///
/// ## Level 3 : Shared external machine / cluster
///
/// This represents a single external machine, or a low latency cluster of machines,
/// that is shared across multiple webserver.
///
/// Which is the traditional many webserver to a single SQL server, or a cluster of redis. 
///
/// ## Level 4 : Multi-Cluster syncronization
///
/// Now things start to get coplicated, at this point, this is normally for really large scale
/// deployments. Where multiple L3 cluster, in possibly different geographic regions across the world.
///
/// Note that this represents a form of clustering that is normally master-master, so as to avoid
/// the syncronization lock issues that is required to travel the globe 4 times. Think eventual consistency.
///
/// Think eventual consistency
///
/// ## Level 5  : Global level autorative cluster
///
/// This represents a varient of L4, where there is a global autorative cluster. Allowing more complex and painful
/// read-after-write consistency on a global level.
///
/// Another way to view this, is an authoriative L4, in which all other L4 clusters read from.
///
package picoded.dstack;

