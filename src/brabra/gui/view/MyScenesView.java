package brabra.gui.view;

import java.util.Observable;
import java.util.Observer;

import brabra.Brabra;
import brabra.game.scene.Scene;
import brabra.gui.field.SceneField;


public class MyScenesView extends View implements Observer {
    
	private final Scene.Model sceneModel;
	
	public MyScenesView(Scene.Model sceneModel) {
		this.sceneModel = sceneModel;
		
		// TODO: catch f5 to reload local files
		
		refreshView();
		sceneModel.addObserver(this);
	}
	
	private void refreshView() {
		//--- View:
		super.clear();
		super.setTitle("My Scenes ("+Scene.loader.scenes.size()+")");
		Scene.loader.scenes.forEach(sf -> addContent(new SceneField(sf, sceneModel.scene)));
	}
	
	public void update(Observable o, java.lang.Object arg) {
		// the view only react to scene file change.
		if (((Scene.Model.Arg)arg).change == Scene.Model.Change.SceneFileChanged)
			Brabra.app.fxApp.runLater(()-> refreshView());
	}
}
