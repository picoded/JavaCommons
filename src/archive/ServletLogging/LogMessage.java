package picoded.ServletLogging;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class LogMessage {
	
	// store format string
	private String format;
	
	// store format string arguments
	private List<Object> args;
	
	// / Default Constructor
	public LogMessage() {
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
	
	// / Add object to the arguments list
	public void addArgs(Object obj) {
		if (this.args == null) {
			this.args = new ArrayList<Object>();
		}
		this.args.add(obj);
	}
	
	// / Add list of Object to the arguments list
	public void addArgs(List<Object> list) {
		if (this.args == null) {
			this.args = new ArrayList<Object>();
		}
		this.args.addAll(list);
	}
	
	// / Returns formatted string
	@Override
	public String toString() {
		if (StringUtils.isBlank(getFormat()) || getArgs() == null || getArgs().isEmpty()) {
			return "";
		}
		return String.format(getFormat(), getArgs().toArray(new Object[getArgs().size()]));
	}
	
}