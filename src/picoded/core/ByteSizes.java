package picoded.core;

/// Declearation of various CPU, Configuration values. Used in code optimization assumptions
///
/// Currently assumes: Haswell, 64bit CPU.
///
class ByteSizes {
	
	/// Assumed CPU line size, used in linesize cache aware data structures, this is in bytes
	/// see: http://stackoverflow.com/questions/716145/l1-memory-cache-on-intel-x86-processors
	public static final byte CPU_LINESIZE = 64;
	
	/// An object pointer size, Assuming 64 bit system
	public static final byte OBJ_POINTERSIZE = 8;
	
	/// The assumed blank class object overhead, used to optimize cache line size
	/// See: http://stackoverflow.com/questions/17335884/object-header-size-in-java-on-64bit-vm-with-4gb-ram
	public static final byte CLASS_OVERHEAD = 12;
}