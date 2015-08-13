package picoded.webTemplateEngines;

import java.util.function.Function;

public interface FormInputInterface  extends Function<FormNode, StringBuilder> {
	public StringBuilder apply(FormNode node);
}
