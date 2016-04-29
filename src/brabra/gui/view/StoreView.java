package brabra.gui.view;

import java.util.ArrayList;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import brabra.game.scene.Scene;
import brabra.gui.field.Field;
import brabra.gui.field.SceneField;
import brabra.gui.field.StringField;
import brabra.model.SceneFile;


public class StoreView extends View {

	private final static String defaultTitle = "Scene Store";
	private final HBox filterHolder = new HBox();
	private final Scene scene;
	
	private String filter = "";
	
	
	public StoreView(Scene scene) {
		this.scene = scene;
		
		// > create Filter area
		// Filter Label
		final Label filterLabel = new Label("Look for");
		// Filter String Field
		final Field filterField = new StringField(
				(s) -> setFilter(s),
				() -> filter)
				.set(null, true, false, false);
		// refreshButton
		final Button refreshButton = new Button("refresh");
		// TODO: img for refresh
		filterHolder.getChildren().addAll(filterLabel, filterField, refreshButton);
		filterHolder.getStyleClass().add("store-filter-holder");
		
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
		
		// get scene files
		final ArrayList<SceneFile> sceneFiles = getDistantSceneFiles(filter);
		
		// get fields
		final ArrayList<Field> fields = new ArrayList<>();
		sceneFiles.forEach(sfile -> fields.add(new SceneField(sfile, scene)));
			
		//--- View:
		
		clear();
		super.setTitle(defaultTitle + " ("+sceneFiles.size()+")");
		
		// link
		filterHolder.getStyleClass().add("filter-holder");
		addContent(filterHolder);
		fields.forEach(f -> addContent(f));
	}
	
	private ArrayList<SceneFile> getDistantSceneFiles(String filter) {
		final ArrayList<SceneFile> files = new ArrayList<>();
		
		// get them from server
		scene.providerDistant.fetchSafe(files);
		
		//TODO: filter them
		
		return files;
	}
}

