package brabra.gui.view;

import java.util.ArrayList;

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
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class CreateView extends View {

	private final GridPane selectionContent = new GridPane(), creationControlButtons = new GridPane();
	
	private ObjectField objectField = null;
	
	public CreateView() {
		
		//--- View:
		// styles
		selectionContent.getStyleClass().add("create-selectionContent-grid");
		creationControlButtons.getStyleClass().add("create-creationControlButtons-grid");
		// > choice view
		// get the object choice buttons
		ArrayList<Button> buttons = new ArrayList<>();
		buttons.add(getButton(null, Object.class));
		buttons.add(getButton(null, Movable.class));
		buttons.add(getButton("data/gui/ball.png", Sphere.class));
		buttons.add(getButton("data/gui/box.png", Box.class));
		buttons.add(getButton("data/gui/plane.png", Plane.class));
		buttons.add(getButton("data/gui/starship.png", Starship.class));
		// arrange them
		int column = 2;
		for (int current=0, r=0, c=0; current<buttons.size(); r++) {
			for (c=0;c<column;c++,current++)
				selectionContent.add(buttons.get(current), c, r);
		}
		
        // > creation view
        //creationControlButtons.setAlignment(Pos.CENTER);
        final Button returnBtn = getButton("data/gui/return.png", "Return");
        creationControlButtons.add(returnBtn, 0, 1);
        final Button createBtn = getButton("data/gui/hammer.png", "Create");
        creationControlButtons.add(createBtn, 1, 1);
        final Button dragBtn = getButton("data/gui/drag.png", "Place");
        creationControlButtons.add(dragBtn, 2, 1);
        
        // default view
        setSelectionMode();
		
        //--- Control:
        
        // > choice view
		//		in getButton(String, Class<T>)
        
        // > creation view
        returnBtn.setOnAction(e -> setSelectionMode());
        createBtn.setOnAction(e -> {
        	// Add the current object to create into the scene & refresh the view.
        	final Object obj = objectField.object;
        	Brabra.app.game.scene.add(obj);
        	// remove the old field
    		getChildren().remove(objectField);
        	// create the object & his field
    		final Object newObj = getNewObject(obj.getClass());
    		newObj.copy(obj);
    		setCreationMode(newObj);
        });
	}

	/** Clear the view view and put the creation view for this new object. */
	private <T extends Object> void setCreationMode(T object) {
		// remove the old choice view
		super.clear();
		super.setTitle("Go on.. :D");
        // control buttons
		addContent(creationControlButtons);
		// and the object field & all the fields (under it)
		objectField = (ObjectField)new ObjectField(object, false).set("lol", true, false, false);
		addContent(objectField);
    }

	/** Clear the view view and put the selection view for this new object. */
	private void setSelectionMode() {
		super.clear();
    	super.setTitle("Select an object to create:");
        addContent(selectionContent);
    }

	private <T extends Object> Button getButton(String imgPath, Class<T> type) {
		Button button = getButton(imgPath, type.getSimpleName());
		button.setOnAction(e -> setCreationMode(getNewObject(type)));
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
	
	private <T extends Object> Object getNewObject(Class <T> type) {
		return Brabra.app.game.scene.getPrefab(type.getSimpleName(), Vector.zero, Quaternion.identity);
	}
}
