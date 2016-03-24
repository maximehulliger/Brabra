package brabra.gui.model;

import java.util.Observable;
import java.util.Observer;

import brabra.game.scene.Scene;

/** 
 * Model representing the scene (of the main window) 
 * for the tool window (for thread safety). 
 * Listen to the main app and update observers on change.
 **/
public class SceneModel extends Observable implements Observer {

	private final Scene scene;
	
	public SceneModel(Scene scene) {
		this.scene = scene;
	}
	
	
	// --- Getters ---
	
	public int objectCount() {
		return scene.objects().size();
	}

	// --- Setters ---


	// --- update ---
	
	public void update(Observable o, Object arg) {
		// when updated
		notifyObservers();
	}
}
