package brabra.gui.model;

import java.util.*;

import brabra.game.scene.Scene;
import brabra.game.scene.Object;

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
	
	public List<Object> objects(){
		return scene.objects();
	}

	// --- Setters ---


	// --- update ---
	
	public void update(Observable o, java.lang.Object arg) {
		// when updated
		setChanged();
		notifyObservers();
	}
}
