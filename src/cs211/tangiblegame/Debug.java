package cs211.tangiblegame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Utilitary class to debug with class ;) take verbosity from TangibleGame. */
public class Debug {
	private static final String debugLine = " > ";
	private static final String msgLine = " ( ";
	
	private static String work = TangibleGame.name+" initialization";
	private static int frameWithoutPrintCount = 0;
	/** during actual work. */
	private static boolean noPrint = true;
	/** during the whole frame. */
	private static boolean noPrintFrame = true;
	
	public final List<Debugable> followed = new ArrayList<>();
	public final Set<String> onceMem = new TreeSet<>();
	
	/** Interface to be able to be followed by the debugger. */
	public static interface Debugable {
		/** Return a state update with presentation (what speaks ?) or null if no update since last frame. */
		public abstract String getStateUpdate();
	}
	
	/** To display info on the followed objects and update noprint score. */
	public void update() {
		// no print count
		if (noPrintFrame)
			frameWithoutPrintCount++;
		else
			noPrintFrame = true;
		
		// print update state of all followed
		for (Debugable o : followed)
			print(3, o.getStateUpdate(), "state update", debugLine, false);
	}

	/** Set the work name that will we displayed */
	public void setCurrentWork(String work) {
		Debug.work = work;
		noPrint = true;
	}

	// --- Standard debug: print function ---
	
	/** Log the debug msg in a nice way :) */
	public void log(String s) {
		print(Integer.MAX_VALUE, s, "log", debugLine, false);
	}
	
	/** Log the debug msg in a nice way if interesting enough. */
	public void log(int verbMin, String s) {
		print(verbMin, s, "log", debugLine, false);
	}
	
	/** Print a message only once. (remembers) */
	public void once(int verbMin, String s) {
		if (TangibleGame.verbosity >= verbMin && !onceMem.contains(s)) {
			onceMem.add(s);
			msg(verbMin, s);
		}
	}

	/** Print a friendly message if interesting enough.  */
	public void msg(int verbMin, String s) {
		print(verbMin, s, "msg", msgLine, false);
	}

	/** Print a friendly message if interesting enough.  */
	public void info(int verbMin, String s) {
		print(verbMin, s, "info", msgLine, false);
	}

	/** Print a friendly message if interesting enough.  */
	public void err(String s) {
		print(Integer.MAX_VALUE, s, "err", debugLine, false);
	}
	
	private static void print(int verbMin, String s, String msgType, String line, boolean error) {
		if (s != null && TangibleGame.verbosity >= verbMin) {
			if (noPrint) {
				noPrint = false;
				noPrintFrame = false;
				String frameCount;
				if (frameWithoutPrintCount == 0)
					frameCount = "";
				else if (frameWithoutPrintCount == 1)
					frameCount = " (1 frame since last msg)";
				else 
					frameCount = " ("+frameWithoutPrintCount+" frames since last msg)";
				frameWithoutPrintCount = 0;
				System.out.println("---- from "+work+frameCount+":");
			}
			String out = (verbMin == Integer.MAX_VALUE ? "_" : verbMin)+"! "+ msgType+": "
					+ (s.contains("\n") ? "\n"+line+s.replaceAll("\n", "\n"+line) : s);
			if (error)
				System.err.println(out);
			else
				System.out.println(out);
		}
	}

	// --- Game debug ---
	
	public static class GameDebug extends Debug {
		
	}
}
