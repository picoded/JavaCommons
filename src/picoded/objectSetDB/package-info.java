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
/// + ACID everything : For reliability
/// + Reuse SQL and NOSQL : For compliance
/// + Relative Object relation : For ease of use, and super joins
/// + Flexible Object Structure : For rapid iterations
/// + Every object has a GUID/ID : For ID purposes, can be replaced with manual ID's
/// + Every object belongs to a collection set : For Object Structure enforcement
/// + Works using just java.util.Map interface : For ease of use
///
package picoded.objectSetDB;

