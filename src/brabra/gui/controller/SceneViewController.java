package brabra.gui.controller;

import brabra.gui.model.SceneModel;
import brabra.gui.view.SceneView;
import javafx.scene.input.MouseEvent;
import brabra.gui.TriangleButton;
import javafx.event.EventHandler;

public class SceneViewController {
	
	public SceneViewController(SceneView view, SceneModel sceneModel) {
		for(TriangleButton triangle:view.btns){
			triangle.setOnMouseClicked(new EventHandler<MouseEvent>() {
			    public void handle(MouseEvent me) {
			    	//TODO: let the field class deal with that (open/close from anywhere on the field, not just on the triangle)
			        //triangle.setState(triangle.open);
			    }
			});
		}
	}
	
}
