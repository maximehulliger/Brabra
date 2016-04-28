package brabra;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/** Utilitary class to debug with class ;) take verbosity from TangibleGame. */
public class Debug extends ProMaster {
	private static final String debugLine = " > ";
	private static final String msgLine = " ( ";
	
	private String work = Brabra.name+" initialization";
	private String lastWork = null, lastLastWork = null;
	/** during actual work. */
	private static boolean noPrint = true;
	/** List of all the object with the debug attribute. */
	public final List<Object> followed = new ArrayList<>();
	public final Set<String> onceMem = new TreeSet<>();
	/** to do tests in discrete mode. */
	public boolean testMode = false;

	/** Set the work name that will we displayed */
	public void setCurrentWork(String work) {
		if (!this.work.equals(work)) {
			this.work = work;
			noPrint = true;
		}
	}

	// --- Standard debug: print function ---
	
	/** Log the debug msg in a dev way (red). */
	public void log(String s) {
		print(Integer.MIN_VALUE, s, "log", debugLine, true);
	}
	
	/** Log the debug msg in a nice way if interesting enough. */
	public void log(int verbMin, String s) {
		print(verbMin, s, "log", debugLine, false);
	}
	
	/** Print a message only once. (remembers) */
	public void once(int verbMin, String s) {
		if (app.verbosity >= verbMin && !onceMem.contains(s)) {
			onceMem.add(s);
			msg(verbMin, s);
		}
	}

	/** Print a friendly message if interesting enough.  */
	public void msg(int verbMin, String s) {
		print(verbMin, s, "msg", msgLine, false);
	}

	/** Print a friendly message if interesting enough.  */
	public void msg(int verbMin, String s, String context) {
		final String workMem = work;
		setCurrentWork(context);
		print(verbMin, s, "msg", msgLine, false);
		setCurrentWork(workMem);
	}

	/** Print a friendly message if interesting enough.  */
	public void info(int verbMin, String s) {
		print(verbMin, s, "info", msgLine, false);
	}

	/** Print a friendly message if interesting enough.  */
	public void err(String s) {
		print(Integer.MIN_VALUE, s, "err", debugLine, true);
	}
	
	private void print(int verbMin, String s, String msgType, String line, boolean error) {
		if (!testMode && !s.equals("") && app.verbosity >= verbMin) {
			// format msg
			final String msg = (verbMin == Integer.MIN_VALUE ? "-" : verbMin)
					+ "! " + msgType + ": " + s.replaceAll("\n", "\n"+line);
			
			// for the console (work)
			if (noPrint) {
				if (verbMin < Brabra.verbMax && !work.equals(lastWork) && !work.equals(lastLastWork)) {
					System.out.println("---- from "+work+":");
					lastLastWork = lastWork;
					lastWork = work;
					noPrint = false;
				}
			}
			
			// for the console (msg)
			if (error)
				System.err.println(msg);
			else
				System.out.println(msg);
			
			// for the tool window (msg)
			// TODO: ToolWindow.displayMessage(msg, !error);
		}
	}
}
