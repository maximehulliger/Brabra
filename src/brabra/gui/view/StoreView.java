package brabra.gui.view;

import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import brabra.game.scene.Scene;
import brabra.game.scene.SceneFile;
import brabra.gui.field.Field;
import brabra.gui.field.SceneField;
import brabra.gui.field.StringField;


public class StoreView extends View {

	private final HBox filterHolder = new HBox();
	private final Scene scene;
	
	private String filter = "";
	
	
	public StoreView(Scene scene) {
		this.scene = scene;
		
		// > create Filter area
		// Filter Label
		final Label filterLabel = new Label("Look for: ");
		// Filter String Field
		final Field filterField = new StringField(
				(s) -> setFilter(s),
				() -> filter)
				.set(null, true, false, false);
		// refreshButton
		final Button refreshButton = new Button("refresh");
		// TODO: img for refresh
		filterHolder.getChildren().addAll(filterLabel, filterField, refreshButton);
		
		//--- View:
		refreshView();
		
		//--- Control:
		refreshButton.setOnAction(e -> refreshView());
	}

	private void setFilter(String filter) {
		this.filter = filter == null ? "" : filter;
		refreshView();
	}
	
	/** Refresh the View */
	private void refreshView() {
		clear();
		// get fields
		final ArrayList<Field> fields = new ArrayList<>();
		getDistantSceneFiles(filter).forEach(sfile -> fields.add(new SceneField(sfile, scene)));
		super.setTitle("Scene Store, fud again :D");
		
		// link
		addContent(filterHolder);
		fields.forEach(f -> addContent(f));
	}
	
	private static ArrayList<SceneFile> getDistantSceneFiles(String filter) {
		final ArrayList<SceneFile> files = new ArrayList<>();
		
		//TODO: get from server (server adress should be in Parameters / (& modifiable in the parameters view).
		
		return files;
	}
}

