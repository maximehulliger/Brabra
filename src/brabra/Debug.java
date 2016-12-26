package brabra;

import java.util.ArrayList;
import java.util.List;


/** Utilitary class to debug with class ;) take verbosity from TangibleGame. */
public class Debug extends ProMaster {
	private static final String debugLine = " > ";
	private static final String msgLine = " ( ";
	
	/** List of all the object with the debug attribute. */
	public static final List<Object> followed = new ArrayList<>();
	
	/** to do tests in discrete mode. */
	public static boolean silentMode = false;
	
	// just a static class
	private Debug() {}

	// --- Standard debug: print function ---
	
	/** Log the debug msg in a dev way (red). */
	public static void log(String s) {
		print(Integer.MIN_VALUE, s, "log", debugLine, true);
	}
	
	/** Log the debug msg in a nice way if interesting enough. */
	public static void log(int verbMin, String s) {
		print(verbMin, s, "log", debugLine, false);
	}
	
	/** Print a friendly message if interesting enough.  */
	public static void msg(int verbMin, String s) {
		print(verbMin, s, "msg", msgLine, false);
	}

	/** Print a friendly message if interesting enough.  */
	public static void info(int verbMin, String s) {
		print(verbMin, s, "info", msgLine, false);
	}

	/** Print a friendly message if interesting enough.  */
	public static void err(String s) {
		print(Integer.MIN_VALUE, s, "err", debugLine, true);
	}
	
	private static void print(int verbMin, String s, String msgType, String line, boolean error) {
		if (!silentMode && !s.equals("") && app.verbosity >= verbMin) {
			// format msg
			final String msg = (verbMin == Integer.MIN_VALUE ? "-" : verbMin)
					+ "! " + msgType + ": " + s.replaceAll("\n", "\n"+line);
			
			// for the console (msg)
			if (error)
				System.err.println(msg);
			else
				System.out.println(msg);
		}
	}
}
