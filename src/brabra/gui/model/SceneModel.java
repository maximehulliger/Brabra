
package brabra.gui.model;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedDeque;

import brabra.game.scene.Scene;
import brabra.gui.ToolWindow;
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
		scene.addObserver(this);
	}
	
	// --- Getters ---
	
	public int objectCount() {
		return scene.objects.size();
	}
	
	/** Return the list (thread-safe) of all the objects in the scene. */
	public ConcurrentLinkedDeque<Object> objects(){
		return scene.objects;
	}

	// --- update ---
	
	public void update(Observable o, java.lang.Object arg) {
		// when updated
		ToolWindow.run(() -> {
			this.setChanged();
			this.notifyObservers(arg);
		});
	}
}
