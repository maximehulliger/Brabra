package brabra.gui.model;


import java.util.Observable;
import java.util.Observer;

import brabra.Brabra;


/** 
 * Model representing the whole app (with all the general settings) 
 * for the tool window (for thread safety). 
 * Listen to the main app and update observers on change.
 * TODO: everything..
 **/
public class AppModel extends Observable implements Observer {

	public final Brabra app;
	
	public AppModel(Brabra app) {
		this.app = app;
	}
	
	// --- Getters ---

	public int verbosity() {
		return app.verbosity;
	}

	
	// --- Setters ---

	public void setVerbosity(int verbosity) {
		app.verbosity = verbosity;
		setChanged();
		notifyObservers();
	}
	
	
	// --- update ---
	
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers();
	}
}
