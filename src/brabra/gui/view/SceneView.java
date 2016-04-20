package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import brabra.game.scene.Object;
import brabra.game.scene.Scene;
import brabra.gui.field.ObjectField;

public final class SceneView extends View implements Observer {
	
	private static final String defaultTitle = "Empty Scene.";
	private final List<ObjectField> objectFields = new ArrayList<>();
	private final Scene sceneModel;

	public SceneView(Scene sceneModel) {
		this.sceneModel = sceneModel;

		assert(sceneModel.objects.size() == 0);

		//--- View:
		setTitle(defaultTitle);
		
		//--- Control:
		sceneModel.addObserver(this);
	}

	public void update(Observable o, java.lang.Object arg) {
		Scene.Arg sceneArg = (Scene.Arg)arg;
		Object obj = sceneArg.object;
		Scene.Change change = sceneArg.change;
		// the view only react to object addition or deletion.
		if (change == Scene.Change.ObjectAdded || change == Scene.Change.ObjectRemoved) {
			// update the title
			final int nbObj = sceneModel.objects.size();
			setTitle(nbObj > 0 ? "Scene with "+nbObj+" objects:" : defaultTitle);
			// add or remove fields
			if (change == Scene.Change.ObjectAdded) {
				ObjectField newField = new ObjectField(obj, true);
				objectFields.add(newField);
				sceneModel.addObserver(newField);
				addContent(newField);
			} else if (change == Scene.Change.ObjectRemoved) {
				// we remove the object field of the object that is no longer in the scene.
				List<ObjectField> deadFields = objectFields.stream().filter(of -> of.object == obj).collect(Collectors.toList());
				assert(deadFields.size() >= 1);
				removeContent(deadFields.get(0));
				objectFields.removeAll(deadFields);
			}
		}
	}
}
