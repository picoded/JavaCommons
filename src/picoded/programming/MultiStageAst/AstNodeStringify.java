package picoded.programming.MultiStageAst;

import java.util.function.Function;

/// AstNodeStringify represents the lamda function used to stringify the AstNdoe
///
public interface AstNodeStringify extends Function<AstNode, StringBuilder> {
	
	/// The lamda function to implement
	///
	/// @params {AstNode}  node  - The AST node for the interface to use and generate
	///
	/// @returns {StringBuilder}  full to string formatter
	public StringBuilder apply(AstNode node);
}
