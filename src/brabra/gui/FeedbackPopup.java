package brabra.gui;

import brabra.Brabra;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class FeedbackPopup extends VBox {
	
	public FeedbackPopup () {
		super();
		setMaxHeight(Brabra.height/3f);
		setMaxWidth(ToolWindow.width*3f/4);
		setAlignment(Pos.BOTTOM_CENTER);
		getStyleClass().add("popup-box");
	}
	
	public void addContent(Label n) {
		if (getChildren().size() == 0)
			setPopupVisible(true);
		getChildren().add(n);
	}
	
	public void removeContent(Label n) {
		n.setVisible(false);
		getChildren().remove(n);
		if (getChildren().size() == 0)
			setPopupVisible(false);
	}
	
	private void setPopupVisible(boolean visible) {
		setVisible(visible);
		setManaged(visible);
	}
}
