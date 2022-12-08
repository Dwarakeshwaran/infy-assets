package restlet.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorLog {

	private String stackTrace;
	private String errorMessage;

	public ErrorLog(Exception e) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String stackTrace = sw.toString();

		this.stackTrace = stackTrace;
		this.errorMessage = e.getMessage();

	}

	@Override
	public String toString() {
		return "ErrorLog [stackTrace=" + stackTrace + ", errorMessage=" + errorMessage + "]";
	}

}