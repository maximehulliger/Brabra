package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import brabra.Brabra;
import brabra.Debug;
import brabra.game.scene.Object;
import brabra.game.scene.Scene;
import brabra.gui.field.ObjectField;

public final class SceneView extends View implements Observer {
	
	private static final String defaultTitle = "Empty Scene.";
	private final List<ObjectField> objectFields = new ArrayList<>();
	private final Scene.Model sceneModel;

	
	public SceneView(Scene.Model scene) {
		this.sceneModel = scene;
		
		//--- View:
		setTitle(defaultTitle);
		
		//--- Control:
		sceneModel.addObserver(this);
	}
	
	public void setScene(Scene scene) {
		
	}

	public void update(Observable o, java.lang.Object arg) {
		// get correct argument
		final Scene.Model.Arg sceneArg = (Scene.Model.Arg)arg;
		final Object obj = sceneArg.object;
		final Scene.Model.Change change = sceneArg.change;
		// the view only react to object addition or deletion.
		if (change == Scene.Model.Change.ObjectAdded || change == Scene.Model.Change.ObjectRemoved) {
			Brabra.app.fxApp.runLater(() -> {
				// update the title
				// add or remove fields
				if (change == Scene.Model.Change.ObjectAdded) {
					addObjectField(obj);
				} else if (change == Scene.Model.Change.ObjectRemoved) {
					// we remove the object field of the object that is no longer in the scene.
					final List<ObjectField> deadFields = objectFields.stream().filter(of -> of.object == obj).collect(Collectors.toList());
					deadFields.forEach(df -> removeContent(df));
					objectFields.removeAll(deadFields);
				}
				final int nbObj = objectFields.size();
				setTitle(nbObj > 0 ? "Scene with "+nbObj+" objects:" : defaultTitle);
			});
		}
	}
	
	private void addObjectField(Object obj) {
		ObjectField newField = new ObjectField(obj, true);
		if (Debug.followed.contains(obj))
			newField.setOpen(false);
		objectFields.add(newField);
		sceneModel.addObserver(newField);
		addContent(newField);
	}
}
