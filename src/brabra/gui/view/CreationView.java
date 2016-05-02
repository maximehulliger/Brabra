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
import javafx.scene.layout.GridPane;


public class CreationView extends View {

	private final GridPane selectionContent = new GridPane(), creationControlButtons = new GridPane();
	
	private ObjectField objectField = null;
	
	public CreationView() {
		
		//--- View:
		
		// styles
		selectionContent.getStyleClass().add("selection-content-grid");
		creationControlButtons.getStyleClass().add("creation-button-holder");
		// > choice view
		// get the object choice buttons
		ArrayList<Button> buttons = new ArrayList<>();
		buttons.add(getButton(null, Object.class));
		buttons.add(getButton(null, Movable.class));
		buttons.add(getButton("resource/gui/ball.png", Sphere.class));
		buttons.add(getButton("resource/gui/box.png", Box.class));
		buttons.add(getButton("resource/gui/plane.png", Plane.class));
		buttons.add(getButton("resource/gui/starship.png", Starship.class));
		// arrange them
		int column = 2;
		for (int current=0, r=0, c=0; current<buttons.size(); r++) {
			for (c=0;c<column;c++,current++) {
				final Button button = buttons.get(current);
				selectionContent.add(button, c, r);
				button.getStyleClass().add("creation-button");
			}
		}
		
        // > creation view
        //creationControlButtons.setAlignment(Pos.CENTER);
        final Button returnBtn = getNewButton("Return", "resource/gui/return.png");
        returnBtn.getStyleClass().add("creation-button-in");
        creationControlButtons.add(returnBtn, 0, 1);
        final Button createBtn = getNewButton("Create", "resource/gui/hammer.png");
        createBtn.getStyleClass().add("creation-button-in");
        creationControlButtons.add(createBtn, 1, 1);
        final Button dragBtn = getNewButton("Place", "resource/gui/drag.png");
        dragBtn.getStyleClass().add("creation-button-in");
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
		objectField = (ObjectField)new ObjectField(object, false).set("lol", true, false);
		addContent(objectField);
    }

	/** Clear the view view and put the selection view for this new object. */
	private void setSelectionMode() {
		super.clear();
    	super.setTitle("Select an object to create:");
        addContent(selectionContent);
    }

	private <T extends Object> Button getButton(String imgPath, Class<T> type) {
		Button button = getNewButton(type.getSimpleName(), imgPath);
		button.setOnAction(e -> setCreationMode(getNewObject(type)));
        return button;
	}

	private <T extends Object> Object getNewObject(Class <T> type) {
		return Brabra.app.game.scene.getPrefab(type.getSimpleName(), Vector.zero, Quaternion.identity);
	}
}
