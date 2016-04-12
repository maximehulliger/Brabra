package brabra.gui.view;

import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

import brabra.game.physic.Collider;
import brabra.gui.field.BooleanField;
import brabra.gui.field.Field;
import brabra.gui.model.AppModel;


/** View for the parameters. Listen to the app model. */
public class ParametersView extends View {
	
	int currentRow = 0;
	
	public ParametersView(AppModel appModel) {
		//--- Fields:
		final List<Field> fields = new ArrayList<>();
		// running
		fields.add(new BooleanField("running",
					run -> appModel.app.game.setRunning(run), 
					() -> appModel.app.game.running()));
		// display all collider
		fields.add(new BooleanField("display all collider", 
				dc -> {Collider.displayAllColliders=dc;}, 
				() -> Collider.displayAllColliders));
		
		//--- View:
		// title
		super.add(new Label("parameters:"), 0, currentRow++);
		// fields
		fields.forEach(f -> super.add(f, 0, currentRow++));
		
		//--- Control:
		fields.forEach(f -> appModel.addObserver(f));
	}
}

