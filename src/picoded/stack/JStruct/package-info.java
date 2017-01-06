package picoded.JStruct;

/// JStruct servers as the base refence implementation,
/// for all the various "picoded.J*" data structures
///
/// However as it is completely in memory datastructure,
/// it has absolutely 0 persistency.
///
/// This is to be extended into the following package,
/// 
/// + JFile  - File based storage, used mainly for binary storage
/// + JCache - Distributed cache, used with others for performance
/// + JSql   - SQL based storage and persistency, used for indexing, etc
/// + JStack - Complex combination of the above
