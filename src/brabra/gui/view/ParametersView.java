package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;

import brabra.Brabra;
import brabra.Master;
import brabra.Parameters;
import brabra.Parameters.Change;
import brabra.gui.ToolWindow;
import brabra.gui.field.BooleanField;
import brabra.gui.field.Field;
import brabra.gui.field.StringField;
import brabra.gui.field.ValueField;
import brabra.gui.field.VectorField;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;


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
		
		//--- Ping area
		final HBox pingPane = new HBox();
		// the address field
		final Field adressField = new StringField(
				sa -> para.setServerAdress(sa),
				() -> para.serverAdress())
				.respondingTo(Change.ServerAdress)
				.set("Server Adress", false, true, true);
		// the ping button
		final Button pingButton = getNewButton("Ping ?");
		// link
		pingPane.getChildren().addAll(adressField, pingButton);
				

		//--- View:
		
		// title
		setTitle("Parameters:");
		// link
		fields.forEach(f -> addContent(f));
		addContent(pingPane);
		
		//--- reacto to changes:
		fields.add(adressField);
		fields.forEach(f -> {
			para.addObserver(f);
			// reaction to change
			((ValueField<?>)f).addOnChange(
					() -> ToolWindow.feedbackPopup.displayMessage("set !", true, 0.2f));
		});
		
		//--- Control:
		
		pingButton.setOnAction(e -> {
			Master.launch(() -> {
				final boolean ok = Brabra.app.game.scene.providerDistant.ping();
				ToolWindow.feedbackPopup.displayMessage(ok ? "Pong !" : "Nobody there :(", ok, 0.5f);
			});
		});
	}
}

