package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;

import brabra.Parameters;
import brabra.gui.field.BooleanField;
import brabra.gui.field.Field;


/** View for the parameters. Listen to the app model. */
public class ParametersView extends View {
	
	int currentRow = 0;
	
	public ParametersView(Parameters para) {
		//--- Fields:
		final List<Field> fields = new ArrayList<>();
		// running
		fields.add(new BooleanField("running",
					run -> para.setRunning(run), 
					() -> para.running()));
		// display all collider
		fields.add(new BooleanField("display all collider", 
				dc -> para.setDisplayAllColliders(dc), 
				() -> para.displayAllColliders()));
		
		//--- View:
		// title
		title.setText("parameters:");
		super.add(title, 0, currentRow++);
		// fields
		fields.forEach(f -> {
			super.add(f, 0, currentRow++);
			para.addObserver(f);
		});
	}
}

