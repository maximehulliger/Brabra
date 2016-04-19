package brabra.gui.view;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class View extends GridPane {
	protected Label title = new Label();
	
	public View(){
		title.getStyleClass().add("title-text");
	}
}
