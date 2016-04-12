package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import javafx.scene.control.Label;
import brabra.game.scene.Object;
import brabra.game.scene.Scene;
import brabra.gui.field.ObjectField;
import brabra.gui.model.SceneModel;

public final class SceneView extends View implements Observer {
	
	private static final String defaultTitle = "Empty Scene.";
	private final Label title;
	private final List<ObjectField> objectFields = new ArrayList<>();
	private final SceneModel sceneModel;

	private int currentRow = 0;
	
	public SceneView(SceneModel sceneModel) {
		this.sceneModel = sceneModel;
		
		//--- View:
		//super.setHgap(8);
		//super.setVgap(8);
		this.title = new Label(defaultTitle);
		super.add(title, 0, currentRow++);
		
		assert(sceneModel.objectCount() == 0);
		/*for (Object o : sceneModel.objects()) {
			ObjectField newField = new ObjectField(o);
			objectFields.add(newField);
			sceneModel.addObserver(newField);
			super.add(newField, 0, currentRow++);
		}*/

		//--- Control:
		sceneModel.addObserver(this);
	}

	public void update(Observable o, java.lang.Object arg) {
		// the view only react to object addition or deletion.
		if (arg == Scene.argObjectAdded || arg == Scene.argObjectRemoved) {
			final int nbObj = sceneModel.objectCount();
			title.setText(nbObj > 0 ? "Scene with "+nbObj+" objects:" : defaultTitle);
			if (arg == Scene.argObjectAdded) {
				//get new objects
				List<Object> newObjects = new ArrayList<>(sceneModel.objects());
				newObjects.removeAll(objectFields.stream().map(f -> f.object).collect(Collectors.toList()));
				for (Object obj : newObjects) {
					ObjectField newField = new ObjectField(obj);
					objectFields.add(newField);
					sceneModel.addObserver(newField);
					super.add(newField, 0, currentRow++);
				}
				//assert(newObjects.size() > 0); TODO
			} else if (arg == Scene.argObjectRemoved) {
				// we remove the object fields of the object that are no longer in the scene.
				List<ObjectField> deadFields = new ArrayList<>();
				for (ObjectField of : objectFields)
					if (!sceneModel.objects().contains(of.object))
						deadFields.add(of);
				assert(deadFields.size() > 0);
				getChildren().removeAll(deadFields);
				objectFields.removeAll(deadFields);
				currentRow -= deadFields.size();
			}
		}
	}
}
