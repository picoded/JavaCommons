package picoded.programming.MultiStageAst;

import picoded.struct.*;
import picoded.conv.*;

import java.util.*;

///
/// AST Exception
///
public class AstSyntaxException extends RuntimeException {
	public AstSyntaxException() { super(); }
	public AstSyntaxException(String message) { super(message); }
	public AstSyntaxException(String message, Throwable cause) { super(message,cause); }
	public AstSyntaxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) { super(message,cause,enableSuppression,writableStackTrace); }
	public AstSyntaxException(Throwable cause) { super(cause); }
}
