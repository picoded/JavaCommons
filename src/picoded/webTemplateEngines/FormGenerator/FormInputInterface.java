package picoded.webTemplateEngines.FormGenerator;

import java.util.function.Function;

/// FormInputInterface represents the lamda function used to create a form input field
///
/// Note that while it is pratically identical to FormWrapperInterface, this is intentional
/// as they have very distinct roles. FormInputInterface is meant to generate the respective
/// input and display code, without recurssion. While FormWrapperInterface generates wrapper
/// HTML code, and recursively generates its child nodes
///
public interface FormInputInterface  extends Function<FormNode, StringBuilder> {
	
	/// The lamda function to implement
	///
	/// @params {FormNode}  node  - The form node for the interface to use and generate
	///
	/// @returns {StringBuilder}  full HTML representign the node
	public StringBuilder apply(FormNode node);
}
