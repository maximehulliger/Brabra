package brabra;

import java.util.Observable;

import brabra.game.physic.geo.Vector;
import brabra.gui.ToolWindow;

/** Class containing all the parameters if the project (model). */
public class Parameters extends Observable {
	public enum Change {
		Gravity, DisplayAllColliders, Running
	}

	// --- Game Running ---
	
	private boolean running = true;
	
	public boolean running() {
		return running;
	}
	
	public void setRunning(boolean running) {
		if (this.running != running) {
			this.running = running;
			notifyChange(Change.Running);
			Brabra.app.game.debug.info(1, "game " + (running ? "running !" : "paused :)"));
		}
	}
	
	// --- Gravity ---
	
	private Vector gravity = ProMaster.down(0.8f);

	public Vector gravity() {
		return gravity;
	}
	
	public void setGravity(Vector gravity) {
		if (!this.gravity.equals(gravity)) {
			this.gravity = gravity;
			notifyChange(Change.Gravity);
		}
	}

	// --- Display all Colliders ---

	private boolean displayAllColliders = false;
	
	public boolean displayAllColliders() {
		return displayAllColliders;
	}
	
	public void setDisplayAllColliders(boolean displayAllColliders) {
		if (this.displayAllColliders != displayAllColliders) {
			this.displayAllColliders = displayAllColliders;
			notifyChange(Change.DisplayAllColliders);
		}
	}
	
	// --- Validate ---

	/** Validates the global settings of the program. */
	public void validate(org.xml.sax.Attributes atts) {
		// > verbosity: no one really care about..
		final String verbosity = atts.getValue("verbosity");
		if (verbosity != null) {
			if (verbosity.equals("max") || verbosity.equals("all"))
				Brabra.app.verbosity = Integer.MAX_VALUE;
			else if (verbosity.equals("min") || verbosity.equals("silence") || verbosity.equals("none"))
				Brabra.app.verbosity = Integer.MIN_VALUE;
			else
				Brabra.app.verbosity = Integer.parseInt(verbosity);
		}
		// > Display all Colliders
		final String displayAllColliders =  atts.getValue("displayAllColliders");
		if (displayAllColliders != null)
			setDisplayAllColliders(Boolean.parseBoolean(displayAllColliders));
		// > Gravity
		final String gravity = atts.getValue("gravity");
		if (gravity != null) {
			final Float justFloat = Master.getFloat(gravity, false);
		  	setGravity(justFloat == null ? Vector.fromString(gravity) : ProMaster.down(justFloat));
		}
		// > Game Running
		final String running = atts.getValue("running");
	  	if (running != null)
	  		setRunning(Boolean.parseBoolean(running));
	}
	
	private void notifyChange(Change change) {
		ToolWindow.run(() -> {
			synchronized (this) {
				setChanged();
				notifyObservers(change);
			}
		});
	}
}