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
	private int frameWithoutPrintCount = 0;
	/** during actual work. */
	private static boolean noPrint = true;
	/** during the whole frame. */
	private static boolean noPrintFrame = true;
	/** List of all the object with the debug attribute. */
	public final List<Object> followed = new ArrayList<>();
	public final Set<String> onceMem = new TreeSet<>();
	/** to do tests in discrete mode. */
	public boolean testMode = false;
	
	
	/** To display info on the followed objects and update noprint score. */
	public void update() {
		setCurrentWork("debug followed");
		
		// no print count
		if (noPrintFrame)
			frameWithoutPrintCount++;
		else
			noPrintFrame = true;
	}

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
			if (noPrint) {
				if (verbMin < Brabra.verbMax && !work.equals(lastWork) && !work.equals(lastLastWork)) {
					final String frameCount;
					if (frameWithoutPrintCount == 0)
						frameCount = "";
					else if (frameWithoutPrintCount == 1)
						frameCount = " (1 frame since last msg)";
					else 
						frameCount = " ("+frameWithoutPrintCount+" frames since last msg)";
					System.out.println("---- from "+work+frameCount+":");
					lastLastWork = lastWork;
					lastWork = work;
				}
			}
			noPrint = false;
			noPrintFrame = false;
			frameWithoutPrintCount = 0;
			final String out = (verbMin == Integer.MIN_VALUE ? "-" : verbMin)
					+ "! " + msgType + ": " + s.replaceAll("\n", "\n"+line);
			if (error)
				System.err.println(out);
			else
				System.out.println(out);
		}
	}
}
