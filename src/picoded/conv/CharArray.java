package picoded.conv;

///
/// CharArray's static manipulation and search functions
///
public class CharArray {
	
	//-------------------------------------------------------------
	//
	// Basic functions (have its String varient)
	//
	//-------------------------------------------------------------
	
	///
	/// Takes a string value, and checks the char[] from start to end for the "needle" if it starts with it
	///
	/// @param  Value to find
	/// @param  character array to scan
	/// @param  the offset index to start checking from
	/// @param  the offset index to terminate scan (should be heystack.length)
	///
	/// @return  The boolean true, if matches
	/// 
	public static boolean startsWith(String needle, char[] heystack, int startOffset, int endOffset) {
		return startsWith_returnOffsetAfterNeedle(needle, heystack, startOffset, endOffset) >= 0;
	}
	
	///
	/// Takes a string value, and searches the char[] from start to end for the "needle"
	///
	/// @param  Value to find
	/// @param  character array to scan
	/// @param  the index to start checking from
	/// @param  the index to terminate scan (should be heystack.length)
	///
	/// @return  The found position in the heystack AFTER the needle
	/// 
	public int indexOf(String needle, char[] heystack, int startOffset, int endOffset) {
		// Iterate till found
		for(; startOffset < endOffset; ++startOffset) {
			if( startsWith_returnOffsetAfterNeedle(needle, heystack, startOffset, endOffset) >= 0 ) {
				return startOffset;
			}
		}
		// Not found =(
		return -1;
	}
	
	///
	/// Takes and slices out a char[] 
	///
	/// @param  Source array to slice from
	/// @param  the index to start from
	/// @param  the index to terminate (probably source.length)
	///
	/// @return  The sliced subarray
	/// 
	public static char[] slice(char[] source, int startOffset, int endOffset) {
		return ArrayConv.subarray(source, startOffset, endOffset); 
	}
	
	//-------------------------------------------------------------
	//
	// Complex varient, extends basic functions use cases
	//
	//-------------------------------------------------------------
	
	///
	/// Takes a string value, and checks the char[] from start to end for the "needle" if it starts with it
	///
	/// @param  Value to find
	/// @param  character array to scan
	/// @param  the offset index to start checking from
	/// @param  the offset index to terminate scan (should be heystack.length)
	///
	/// @return  The found position in the heystack AFTER the needle
	/// 
	public static int startsWith_returnOffsetAfterNeedle(String needle, char[] heystack, int startOffset, int endOffset) {
		// Needle size and length check
		int needleSize = needle.length();
		
		// Impossible to match, not enough chars
		if( (endOffset-startOffset) < needleSize ) {
			return -1;
		}
		
		// Scan the needle
		for(int needleIndx = 0; needleIndx < needleSize; ++needleIndx) {
			// Check for char match
			if(needle.charAt(needleIndx) != heystack[startOffset+needleIndx]) {
				// No match, terminates
				return -1; 
			}
		}
		
		// Passed all checks
		return startOffset+needleSize;
	}
	
	///
	/// Takes a string value, and checks the char[] from start to end for the "needle" if it starts with it
	///
	/// @param  Needle array value to find
	/// @param  character array to scan
	/// @param  the offset index to start checking from
	/// @param  the offset index to terminate scan (should be heystack.length)
	///
	/// @return  The index of the needle found, else -1
	/// 
	public static int startsWith_returnNeedleIndex(String[] needleArray, char[] heystack, int startOffset, int endOffset) {
		for(int i=0; i<needleArray.length; ++i) {
			if( startsWith_returnOffsetAfterNeedle(needleArray[i], heystack, startOffset, endOffset) >= 0 ) {
				return i;
			}
		}
		
		return -1;
	}
	
	///
	/// Takes a string value, and searches the char[] from start to end for the "needle"
	///
	/// @param  Value to find
	/// @param  character array to scan
	/// @param  the index to start checking from
	/// @param  the index to terminate scan (should be heystack.length)
	///
	/// @return  The found position in the heystack AFTER the needle
	/// 
	public static int indexOf_skipEscapedCharacters(String[] escapeStrings, String needle, char[] heystack, int startOffset, int endOffset) {
		// Iterate till found
		for(; startOffset < endOffset; ++startOffset) {
			
			// Check for escape string char skipping
			for( int i=0; i<escapeStrings.length; ++i ) {
				int nxtOffset = startsWith_returnOffsetAfterNeedle(escapeStrings[i], heystack, startOffset, endOffset);
				if(nxtOffset >= 0) {
					startOffset = nxtOffset; //+1 to skip the next character is done in for loop
					continue;
				}
			}
			
			// Valid match, returns
			if( startsWith_returnOffsetAfterNeedle(needle, heystack, startOffset, endOffset) >= 0 ) {
				return startOffset;
			}
		}
		// Not found =(
		return -1;
	}
	
	
	
}
