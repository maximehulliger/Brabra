package brabra.gui.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class View extends VBox {

	private final Label title = new Label();
	private int currentRow = 0;
	
	protected final GridPane content = new GridPane();
	
	public View(){
		title.getStyleClass().add("title-text");
		getChildren().addAll(title, content);
		
		content.getStyleClass().add("holding-tab");
		
		//super.setHgap(8);
		//super.setVgap(8);
	}
	
	protected void setTitle(String title) {
		this.title.setText(title);
	}

	protected void addContent(Node n) {
		content.add(n, 0, currentRow++);
	}

	protected void removeContent(Node n) {
		final boolean removed = content.getChildren().remove(n);
		if (!removed)
			throw new IllegalArgumentException("Node to remove not in the view !");
		else
			currentRow--;
	}
}
