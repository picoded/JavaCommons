package picoded.ServletLogging;

import java.util.ArrayList;
import java.util.List;

public class LogMessage {

	// store format string
	private String format;
	
	// store format string arguments
	private List<Object> args;
	
	
	// / Default Constructor
	public LogMessage(){
	}
	
	// / Returns the format string
	public String getFormat() {
		return format;
	}
	
	// / Set the format string
	public void setFormat(String format) {
		this.format = format;
	}
	
	// / Returns the arguments list
	public List<Object> getArgs() {
		return args;
	}
	
	// / Set the arguments list
	public void setArgs(List<Object> args) {
		this.args = args;
	}
	
	// / Add object to the arguments list
	public void addArgs(Object obj) {
		if (this.args == null) {
			this.args = new ArrayList<Object>();
		}
		this.args.add(obj);
	}
	
	// / Add Object[] array to the arguments list
	public void addAllArgs(List<Object> list) {
		if (this.args == null) {
			this.args = new ArrayList<Object>();
		}
		this.args.addAll(list);
	}
	
	// / Returns formatted string
	@Override
	public String toString() {
		return String.format( getFormat(), getArgs() );
	}

}