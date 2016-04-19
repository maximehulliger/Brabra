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

public final class SceneView extends View implements Observer {
	
	private static final String defaultTitle = "Empty Scene.";
	private final Label title;
	private final List<ObjectField> objectFields = new ArrayList<>();
	private final Scene sceneModel;

	private int currentRow = 0;
	
	public SceneView(Scene sceneModel) {
		this.sceneModel = sceneModel;
		
		//--- View:
		//super.setHgap(8);
		//super.setVgap(8);
		this.title = new Label(defaultTitle);
		super.add(title, 0, currentRow++);
		
		assert(sceneModel.objects.size() == 0);

		//--- Control:
		sceneModel.addObserver(this);
	}

	public void update(Observable o, java.lang.Object arg) {
		// get correct argument
		final Scene.Arg sceneArg = (Scene.Arg)arg;
		final Object obj = sceneArg.object;
		final Scene.Change change = sceneArg.change;
		// the view only react to object addition or deletion.
		if (change == Scene.Change.ObjectAdded || change == Scene.Change.ObjectRemoved) {
			// update the title
			final int nbObj = sceneModel.objects.size();
			title.setText(nbObj > 0 ? "Scene with "+nbObj+" objects:" : defaultTitle);
			// add or remove fields
			if (change == Scene.Change.ObjectAdded) {
				ObjectField newField = new ObjectField(obj);
				objectFields.add(newField);
				sceneModel.addObserver(newField);
				super.add(newField, 0, currentRow++);
			} else if (change == Scene.Change.ObjectRemoved) {
				// we remove the object field of the object that is no longer in the scene.
				final List<ObjectField> deadFields = objectFields.stream().filter(of -> of.object == obj).collect(Collectors.toList());
				assert(deadFields.size() == 1);
				final ObjectField deadField = deadFields.get(0);
				getChildren().remove(deadField);
				objectFields.remove(deadField);
				currentRow--;
			}
		}
	}
}
