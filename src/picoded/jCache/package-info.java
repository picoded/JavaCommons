/// jCache provides a wrapper around several common NOSQL memory cache implementations. Via an (almost) single set of syntax.
/// 
/// How this works is by using a core base syntax, which is based off hazelcast / redis. And writing an intermidiary
/// parser for each SQL implementation. To work around its vendor specific issue, and run its respective commands.
/// 
/// Currently Supported SQL Databases
/// + HazelCast
/// + Redis
/// + Memcache
///
package picoded.jCache;