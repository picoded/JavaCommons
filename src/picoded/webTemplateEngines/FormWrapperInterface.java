package picoded.webTemplateEngines;

import java.util.function.Function;

public interface FormWrapperInterface extends Function<FormNode, String[]> {
	public String[] apply(FormNode node);
}
