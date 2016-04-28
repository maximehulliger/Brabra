package brabra.gui.view;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

	protected void addContent(Node... n) {
		getChildren().addAll(n);
	}

	protected void removeContent(Node... n) {
		getChildren().removeAll(n);
	}
	
	protected void clear() {
		getChildren().clear();
		getChildren().add(title);
	}

	public static Button getNewButton(String buttonText) {
        return new Button(buttonText);
	}

	public static Button getNewButton(String buttonText, String imgPath) {
		if (imgPath != null) {
			try {
				final ImageView img = new ImageView(new Image(imgPath));
		        final Button button = new Button(buttonText, img);
		        button.setContentDisplay(ContentDisplay.TOP);
		        
		        // TODO: -> css
		        img.setFitWidth(50);
		        img.setFitHeight(50);
		        
	        	return button;
			} catch (Exception e) {
				System.err.println("can't load image at: "+imgPath);
			}
		}
		// else or exception
		return getNewButton(buttonText);
	}
	
	public static Button getNewButton(String buttonText, String imgPath, String tip) {
		final Button button = getNewButton(buttonText, imgPath);
		button.setTooltip(new Tooltip(tip));
		return button;
	}
}
