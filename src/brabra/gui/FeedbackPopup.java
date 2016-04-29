package brabra.gui;

import brabra.Brabra;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class FeedbackPopup extends VBox {
	
	public FeedbackPopup () {
		super();
		setMaxHeight(Brabra.height/3f);
		setMaxWidth(ToolWindow.width*3f/4);
		setAlignment(Pos.BOTTOM_CENTER);
		getStyleClass().add("glass-pane");
	}
	
	public void addContent(Node n) {
		if (getChildren().size() == 0)
			setPopupVisible(true);
		getChildren().add(n);
	}
	
	public void removeContent(Node n) {
		getChildren().remove(n);
		if (getChildren().size() == 0)
			setPopupVisible(false);
	}
	
	private void setPopupVisible(boolean visible) {
		setManaged(visible);
		setVisible(visible);
	}
}
