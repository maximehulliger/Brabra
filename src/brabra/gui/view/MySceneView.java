package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import brabra.Brabra;
import brabra.game.physic.geo.Box;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Sphere;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.Movable;
import brabra.game.scene.Object;
import brabra.game.scene.fun.Starship;
import brabra.gui.field.Field;
import brabra.gui.field.ObjectField;


public class MySceneView extends View {

	private final GridPane grid = new GridPane();
	
	private ObjectField objectField = null;
	
	public MySceneView() {
	

		//--- View:
		grid.setPadding(new Insets(20,20,20,20));

		//SelectionContent.getStyleClass().add("fields-SelectionContent");
		// > choice view
		// get the object choice buttons
		ArrayList<Button> buttons = new ArrayList<>();
		buttons.add(getButton("run"));
		buttons.add(getButton("See in editor"));
		
		//objectField = (ObjectField)new ObjectField(object, false).set("lol", true, false, false);
		//content.add(objectField, 0, 1);
		
		// arrange them
		

		grid.add(buttons.get(0), 0, 0);
		grid.add(buttons.get(1), 1, 0);
		
		//final List<Field> fields = new ArrayList<>();
		// running
		//fields.add(objectField);
		//objectField = (ObjectField)new ObjectField(object, false).set("lol", true, false, false);
		
		setSelectionMode();
	}
	
	private void setSelectionMode() {
		super.setTitle("Scene or w/e");
        content.getChildren().clear();
    	content.add(grid, 0, 0);
    }
	
	private Button getButton(String buttonText) {
		final Button button;
		button = new Button(buttonText);
		button.setPrefWidth(150);
        button.setPrefHeight(25);
        //button.setTooltip(new Tooltip(buttonText));
        return button;
	}
	
}
