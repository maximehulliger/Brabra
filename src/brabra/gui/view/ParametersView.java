package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;

import brabra.Parameters;
import brabra.Parameters.Change;
import brabra.gui.field.BooleanField;
import brabra.gui.field.Field;
import brabra.gui.field.VectorField;


/** View for the parameters. Listen to the app model. */
public class ParametersView extends View {

	public ParametersView(Parameters para) {
		//--- Fields:
		final List<Field> fields = new ArrayList<>();
		// running
		fields.add(new BooleanField.Pro(
				run -> para.setRunning(run), 
				() -> para.running())
				.respondingTo(Change.Running)
				.set("Running", true, false, true));
		// display all collider
		fields.add(new BooleanField(
				dc -> para.setDisplayAllColliders(dc), 
				() -> para.displayAllColliders())
				.respondingTo(Change.DisplayAllColliders)
				.set("Display all Colliders", true, false, true));
		// gravity
		fields.add(new VectorField.ProCustom(
				g -> para.setGravity(g),
				() -> para.gravity())
				.respondingTo(Change.Gravity)
				.set("Gravity", true, false, true));
		// braking in interaction
		fields.add(new BooleanField.Pro(
				b -> para.setBraking(b), 
				() -> para.braking())
				.respondingTo(Change.Braking)
				.set("Braking focused Object", true, false, true));
		// > camera stuff (to display)
		// skybox
		fields.add(new BooleanField.Pro(
				s -> para.setDisplaySkybox(s), 
				() -> para.displaySkybox())
				.respondingTo(Change.DisplaySkybox)
				.set("Display Skybox", true, false, true));
		// axis
		fields.add(new BooleanField.Pro(
				s -> para.setDisplayAxis(s), 
				() -> para.displayAxis())
				.respondingTo(Change.DisplayAxis)
				.set("Display Axis", true, false, true));
		// center point
//		fields.add(new BooleanField.Pro(
//				s -> para.setDisplayCenterPoint(s), 
//				() -> para.displayCenterPoint())
//				.respondingTo(Change.DisplayCenterPoint)
//				.set("Display center point", true, false, true));

		//--- View & Control:
		
		// title
		setTitle("Parameters:");
		// fields
		fields.forEach(f -> {
			f.triangleButton.setOpen(false);
			addContent(f);
			para.addObserver(f);
		});
	}
}

