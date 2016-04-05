package brabra.gui.controller;

import brabra.gui.model.AppModel;
import brabra.gui.view.ParametersView;
import javafx.scene.input.MouseEvent;
import brabra.gui.TriangleButton;
import javafx.event.EventHandler;

public class ParametersViewController {
	
	public ParametersViewController(ParametersView view, AppModel appModel) {
		for(TriangleButton triangle:view.btns){
			triangle.setOnMouseClicked(new EventHandler<MouseEvent>() {
			    public void handle(MouseEvent me) {
			        triangle.changeTriangle();
			    }
			});
		}
	}
	
}
