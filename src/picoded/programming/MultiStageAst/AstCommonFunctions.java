package picoded.programming.MultiStageAst;

import picoded.struct.*;
import picoded.conv.*;

import java.util.*;

///
/// Takes in the string, grow out (build) an abstract syntax tree.
///
/// This works by processing the given string through the various stages of AstNodeProcessor
///
public class AstCommonFunctions {
	
	/// Takes the full raw text it represents, and echos it
	public static AstNodeStringify echo_stringify = (node) -> {
		StringBuilder res = new StringBuilder();
		if(node.prefix != null) {
			res.append(node.prefix);
		}
		res.append(node.nodeString());
		if(node.suffix != null) {
			res.append(node.suffix);
		}
		return res;
	};
	
	/// Stringify all the child nodes, and returns
	public static AstNodeStringify children_stringify = (node) -> {
		StringBuilder res = new StringBuilder();
		for(AstNode child : node.children) {
			res.append( child.toString() );
		}
		return res;
	};
}
