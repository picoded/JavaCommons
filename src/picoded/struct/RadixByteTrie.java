package picoded.struct;

import java.util.Map;

///
/// @WARNING This is considered experimental, avoid usage unless truely needed
///
/// Implements a byte array RadixTrie, also known as patricia trie, or prefix tree
///
/// See: http://en.wikipedia.org/wiki/Radix_tree
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// ### LineCache optimisation notes.
/// The following aims to keep its this class object (and its decendents), and its internal vars,
/// within CPU line cache size (Assumed 64 bytes)
///
/// + Java Object Overhead  = 12
/// + Inherited from ByteKeyArray = 29 - 12 (Java Object Overhead does not add up)
/// + parentNode   = 8
/// + storedValue  = 8
/// + prefixArray  = 8
/// + prefixLength = 1
///
/// *Total = 54 bytes*
///
@SuppressWarnings("deprecation")
@Deprecated
public class RadixByteTrie<V> /* implements Map<K,V> */{
	
	/// Parent node above this tree node.
	/// if this is null, node is considered root
	private RadixByteTrie<V> parentNode = null;
	
	/// Stored value of the node, if any
	private V storeValue = null;
	
	///Prefix
	private byte[] prefixArray = null;
	
}