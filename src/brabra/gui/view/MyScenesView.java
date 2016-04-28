package brabra.gui.view;

import java.util.Observable;
import java.util.Observer;

import brabra.game.scene.Scene;
import brabra.gui.field.SceneField;


public class MyScenesView extends View implements Observer {
    
	private final Scene scene;
	
	public MyScenesView(Scene scene) {
		this.scene = scene;
		
		// TODO: catch f5 to reload local files
		
		refreshView();
		scene.addObserver(this);
	}
	
	private void refreshView() {
		//--- View:
		super.clear();
		super.setTitle("My Scenes ("+scene.loader.scenes.size()+")");
		scene.loader.scenes.forEach(sf -> addContent(new SceneField(sf, scene)));
	}
	
	public void update(Observable o, java.lang.Object arg) {
		// the view only react to scene file change.
		if (((Scene.Arg)arg).change == Scene.Change.SceneFileChanged)
			refreshView();
	}
}
