package brabra.gui.view;

import brabra.Brabra;
import brabra.game.physic.geo.Box;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Sphere;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.Movable;
import brabra.game.scene.Object;
import brabra.game.scene.fun.Starship;
import brabra.gui.field.ObjectField;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CreateView extends View {

	private final VBox choiceView = new VBox();
	private final GridPane creationControlButtons = new GridPane();
	
	private ObjectField objectField = null;
	
	public CreateView() {
		
		//--- View:
		
		// > choice view
		choiceView.setPadding(new Insets(5));
		//choiceView.setAlignment(Pos.CENTER);
		//choiceView.setHgap(10);
        //choiceView.setVgap(10);
		// title
		title.setText("Select an object to create:");
		//title.setFont(Font.font("Cambria", 48));
		choiceView.getChildren().add(title);
        // object choice buttons
		final GridPane objectChoiceButtons = new GridPane();
		int currentRow = 0;
		objectChoiceButtons.add(getButton(null, Object.class), 0, currentRow);
        objectChoiceButtons.add(getButton(null, Movable.class), 1, currentRow++);
        objectChoiceButtons.add(getButton("data/gui/ball.png", Sphere.class), 0, currentRow);
        objectChoiceButtons.add(getButton("data/gui/box.png", Box.class), 1, currentRow++);
        objectChoiceButtons.add(getButton("data/gui/plane.png", Plane.class), 0, currentRow);
        objectChoiceButtons.add(getButton("data/gui/starship.png", Starship.class), 1, currentRow++);
        choiceView.getChildren().add(objectChoiceButtons);
		
        // > creation view
        // control buttons
        creationControlButtons.setPadding(new Insets(5));
        creationControlButtons.setHgap(10);
        creationControlButtons.setVgap(10);
        //creationControlButtons.setAlignment(Pos.CENTER);
        final Button returnBtn = getButton("data/gui/return.png", "Return");
        creationControlButtons.add(returnBtn, 0, 1);
        final Button createBtn = getButton("data/gui/hammer.png", "Create");
        creationControlButtons.add(createBtn, 1, 1);
        final Button dragBtn = getButton("data/gui/drag.png", "Place");
        creationControlButtons.add(dragBtn, 2, 1);
        
        // > at start choice view
        getChildren().add(choiceView);
         
        //--- Control:
        
        // > choice view
		//		in getButton(String, Class<T>)
        
        // > creation view
        returnBtn.setOnAction(e -> {
        	getChildren().clear();
        	getChildren().add(choiceView);
        });
        createBtn.setOnAction(e -> {
        	// add the edited object in the scene
        	final Object obj = objectField.object;
        	Brabra.app.game.scene.add(obj);
        	// remove the old field
    		getChildren().remove(objectField);
        	// create the object & his field
    		final Object newObj = getNewObject(obj.getClass());
    		newObj.copy(obj);
    		setFieldsToCreate(obj);
        });
	}
	
	private <T extends Object> Button getButton(String imgPath, Class<T> type) {
		Button button = getButton(imgPath, type.getSimpleName());
		button.setOnAction(e -> onButtonClickFor(type));
        return button;
	}

	private Button getButton(String imgPath, String buttonText) {
		final Button button;
		if (imgPath == null)
			button = new Button(buttonText);
		else {
			final ImageView img = new ImageView(new Image(imgPath));
			img.setFitHeight(50);
			img.setFitWidth(50);
	        button = new Button(buttonText, img);
		}
        button.setContentDisplay(ContentDisplay.TOP);
        button.setPrefWidth(150);
        button.setPrefHeight(150);
        //button.setTooltip(new Tooltip(buttonText));
        return button;
	}

	/** Remove the choice view and create+add the creation view. */
	private <T extends Object> void onButtonClickFor(Class <T> type) {
		//--- View:
		// remove the old choice view
		getChildren().clear();
		// control buttons & all the fields (under objectField)
		add(creationControlButtons, 0, 0);
		setFieldsToCreate(getNewObject(type));
	}
	
	/** Set the field for this object in the creation view. */
	private void setFieldsToCreate(Object obj) {
		// create new
		objectField = new ObjectField(obj);
		add(objectField, 0, 1);
		objectField.setOpen(true);
	}
	
	private <T extends Object> Object getNewObject(Class<T> type) {
		return Brabra.app.game.scene.getPrefab(type.getSimpleName(), Vector.zero, Quaternion.identity);
	}
}
