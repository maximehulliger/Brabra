package brabra.gui.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class View extends VBox {

	private final Label title = new Label();
	
	public View(){
		title.getStyleClass().add("title-text");
		this.getStyleClass().add("holding-tab");
		
		getChildren().add(title);
	}
	
	protected void setTitle(String title) {
		this.title.setText(title);
	}

	protected void addContent(Node n) {
		getChildren().add(n);
	}

	protected void removeContent(Node n) {
		getChildren().remove(n);
	}
	
	protected void clear() {
		getChildren().clear();
		getChildren().add(title);
	}
}
