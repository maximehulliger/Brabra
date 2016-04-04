package brabra.gui.view;

import java.util.Observable;
import java.util.Observer;

import javafx.scene.layout.Pane;
import brabra.gui.model.SceneModel;

public class ObjectStateView implements Observer {
	
	
	public ObjectStateView(Pane root, SceneModel sceneModel) {
		sceneModel.addObserver(this);

	}

	public void update(Observable o, Object arg) {
		
	}
}
