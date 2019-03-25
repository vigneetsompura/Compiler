/**
 * COP 5556: Programming Language Principles
 * Project 6
 * Due Date: November 20, 2018
 * 
 * Name: Vigneet M Sompura
 * UFID: 8121 - 1616
 * Email: vigneetsompura@ufl.edu
 */
package compiler;

/**
 * 
 * A simple globalLog that can be used to record a trace of 
 * an instrumented program.
 * 
 * The output can be used for grading and debugging.
 *
 */

public class RuntimeLog {
	
	private StringBuffer sb;
	public static RuntimeLog globalLog;
	
	public static void initLog() {
		globalLog = new RuntimeLog();
		globalLog.sb = new StringBuffer();
	}
	
	public static void globalLogAddEntry(String entry){
		if (globalLog != null) globalLog.addEntry(entry);
	}
	
	private void addEntry(String entry) {
		sb.append(entry);
	}

	public static String getGlobalString() {
		return (globalLog != null) ? globalLog.toString() : "";
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	public static void resetLogToNull() {
		globalLog = null;
	}
	

}
