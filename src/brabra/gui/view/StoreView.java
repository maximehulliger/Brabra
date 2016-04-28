package brabra.gui.view;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import brabra.game.SceneFile;
import brabra.gui.field.Field;
import brabra.gui.field.SceneField;


public class StoreView extends View {

	private final GridPane grid = new GridPane();
	//private Field first;
	//private ObjectField objectField = null;
	Label desc = new Label("Name:"); 
	final ImageView img = new ImageView(new Image("data/gui/ball.png"));
	final List<Field> fields = new ArrayList<>();
	
	public StoreView() {

		//--- Search Box, only graphical
		Label label1 = new Label("Look for scene");
		TextField textField = new TextField ();
		HBox searchHolder = new HBox();
		
		//TODO: to css
		searchHolder.setSpacing(10);
		searchHolder.setPadding(new Insets(20, 20, 20, 20));
		
		searchHolder.getChildren().addAll(label1, textField);
		
		//--- Scene files > fields
		final ArrayList<SceneField> fields = new ArrayList<>();
		getDistantSceneFiles().forEach(sfile -> fields.add(new SceneField(sfile)));
		
		//--- View:
		super.setTitle("Scene Store, fud again :D");
		addContent(searchHolder,grid);
	}
	
	private ArrayList<SceneFile> getDistantSceneFiles() {
		final ArrayList<SceneFile> files = new ArrayList<>();
		
		//TODO: get from server (server adress should be in Parameters / (& modifiable in the parameters view).
		
		return files;
	}
}

