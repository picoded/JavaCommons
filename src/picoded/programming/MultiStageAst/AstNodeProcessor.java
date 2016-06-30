package picoded.programming.MultiStageAst;
import java.util.function.Consumer;

/// AstNodeProcessor represents the lamda function used to create
///
public interface AstNodeProcessor extends Consumer<AstNode> {
	
	/// The lamda function to implement
	///
	/// @params {AstNode}  node  - The node for the interface to use and generate
	///
	public void accept(AstNode node);
}
