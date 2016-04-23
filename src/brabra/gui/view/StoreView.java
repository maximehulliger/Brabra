package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import brabra.gui.field.Field;

public class StoreView extends View {

	private final GridPane grid = new GridPane();
	//private Field first;
	//private ObjectField objectField = null;
	Label desc = new Label("Name:"); 
	final ImageView img = new ImageView(new Image("data/gui/ball.png"));
	final List<Field> fields = new ArrayList<>();
	private final VBox subfieldHolder = new VBox();
	
	public StoreView() {
		
		//final List<Field> fields = new ArrayList<>();

		//first.set("first", false, true, true);
		
		//fields.add(Field());
		
		//--- Search Box, only graphical
		Label label1 = new Label("Look for scene");
		TextField textField = new TextField ();
		HBox hb = new HBox();
		hb.getChildren().addAll(label1, textField);
		hb.setSpacing(10);
		hb.setPadding(new Insets(20, 20, 20, 20));
		
		//--- Fixed view
	     grid.getColumnConstraints().add(new ColumnConstraints(150));
	     grid.getColumnConstraints().add(new ColumnConstraints(150));
		//--- View:
		img.setFitHeight(50);
		img.setFitWidth(50);
		grid.setHgap(10); //horizontal gap in pixels => that's what you are asking for
		grid.setVgap(10);
		grid.setAlignment(Pos.CENTER);
		
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
		grid.add(desc, 0, 0);
		grid.add(img, 1, 0);
		grid.add(buttons.get(0), 0, 1);
		grid.add(buttons.get(1), 1, 1);
		
		subfieldHolder.getChildren().addAll(hb,grid);

		//final List<Field> fields = new ArrayList<>();
		// running
		//fields.add(objectField);
		//objectField = (ObjectField)new ObjectField(object, false).set("lol", true, false, false);
		
		setSelectionMode();
	}
	
	private void setSelectionMode() {
		super.setTitle("Scene or w/e");
        content.getChildren().clear();
    	content.add(subfieldHolder, 0, 0);
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

