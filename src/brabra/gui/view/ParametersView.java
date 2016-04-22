package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;

import brabra.Parameters;
import brabra.gui.field.BooleanField;
import brabra.gui.field.Field;


/** View for the parameters. Listen to the app model. */
public class ParametersView extends View {
	
	public ParametersView(Parameters para) {
		//--- Fields:
		final List<Field> fields = new ArrayList<>();
		// running
		fields.add(new BooleanField(
				run -> para.setRunning(run), 
				() -> para.running())
				.set("Running", true, false, true));
		// display all collider
				fields.add(new BooleanField(
						dc -> para.setDisplayAllColliders(dc), 
						() -> para.displayAllColliders())
						.set("Display all Colliders", true, false, true));
		// braking in interaction
		fields.add(new BooleanField(
				b -> para.setBraking(b), 
				() -> para.braking())
				.set("Braking focused object", true, false, true));
				
		//--- View:
		// title
		setTitle("Parameters:");
		// fields
		fields.forEach(f -> {
			addContent(f);
			para.addObserver(f);
		});
	}
}

