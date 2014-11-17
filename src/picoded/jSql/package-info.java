/// jSql provides a wrapper around several common SQL implementations. Via an (almost) single set of syntax.
/// 
/// How this works is by using a core base syntax, which is based off mysql/sqlite. And writing and intermidiate
/// parser for each SQL implementation. To work around its vendor specific issue, to run its respective commands.
/// 
/// Currently Supported SQL Databases
/// + MySQL
/// + Oracle
/// + MS-SQL
/// + Sqlite
package picoded.jSql;