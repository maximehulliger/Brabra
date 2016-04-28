package brabra.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class GlassPane extends GridPane{
	private int currentRow = 0;

	public GlassPane(){
		super();
		setMaxHeight(150);
		setMaxWidth(ToolWindow.width/4*3);
		getStyleClass().add("glass-pane");
		setAlignment(Pos.TOP_LEFT);
	}
	
	public void addContent(Node n) {
		if (currentRow == 0) {
			setManaged(true);
			setVisible(true);
		}
		add(n, 0, currentRow++);
	}
	
	public void removeContent(Node n) {
		n.setVisible(false);
		n.setManaged(false);
		currentRow--;
		if (currentRow == 0) {
			setManaged(false);
			setVisible(false);
		}
	}
}
