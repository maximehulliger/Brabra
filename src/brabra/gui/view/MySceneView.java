package brabra.gui.view;

import java.util.Observable;
import java.util.Observer;

import brabra.game.scene.Scene;
import brabra.gui.field.SceneField;


public class MySceneView extends View implements Observer {
    
	private final Scene scene;
	
	public MySceneView(Scene scene) {
		this.scene = scene;
		
		// TODO: catch f5 to reload local files
		
		refreshView();
		scene.addObserver(this);
	}
	
	private void refreshView() {
		//--- View:
		super.clear();
		super.setTitle("My Scenes, fu "+scene.loader.scenes.size()+" time(s) :)");
		scene.loader.scenes.forEach(sf -> addContent(new SceneField(sf, scene)));
	}
	
	public void update(Observable o, java.lang.Object arg) {
		// get correct argument
		final Scene.Arg sceneArg = (Scene.Arg)arg;
		final Scene.Change change = sceneArg.change;
		// the view only react to object addition or deletion.
		if (change == Scene.Change.SceneFileReloaded)
			refreshView();
	}
}
