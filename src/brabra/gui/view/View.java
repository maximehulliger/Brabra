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
	private final VBox content = new VBox();
	
	public View(){
		title.getStyleClass().add("view-title");
		content.getStyleClass().add("view-content-vbox");
		//ScrollPane scrollHolder = new ScrollPane();
		//scroll.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		//scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		// TODO put the scroll holder scrollHolder.content
		getChildren().addAll(title, content);
	}
	
	protected void setTitle(String title) {
		this.title.setText(title);
	}

	protected void addContent(Node... n) {
		content.getChildren().addAll(n);
	}

	protected void removeContent(Node... n) {
		content.getChildren().removeAll(n);
	}
	
	protected void clear() {
		content.getChildren().clear();
	}
	
	// --- Buttons ---

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
