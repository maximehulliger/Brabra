package brabra.gui.field;

import java.util.Observable;
import brabra.gui.view.View;
import brabra.model.SceneFile;
import brabra.Brabra;
import brabra.game.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;


public class SceneField extends Field {

	private final static String toDefaultSceneImg = "resource/gui/scene/default.png";
	
	
	public SceneField(SceneFile sceneFile, Scene scene) {

		// > get field elements
		final HBox upHolder = new HBox(), downHolder = new HBox();
		// description
		final Label descriptionLabel = new Label(sceneFile.getDescription());
		// img
		final ImageView image = new ImageView(new Image(
				sceneFile.getImgPath() != null ?
						sceneFile.getImgPath() : toDefaultSceneImg));
		// buttons
		final Button seeButton = View.getNewButton("(See in editor)");
		final Button execButton = View.getNewButton("Load");
		
		//--- View:
		
		super.setName(sceneFile.getName());
		//style
		image.getStyleClass().add("sceneField-img");
		
		// link to field
		upHolder.getChildren().addAll(descriptionLabel, image);
		downHolder.getChildren().addAll(seeButton, execButton);
		subfields().addAll(upHolder, downHolder);
		
		//--- Control:
		
		execButton.setOnAction(e -> {
			Brabra.app.runLater(() -> {
				scene.loader.setFile(sceneFile);
				scene.loader.load();
			});
		});
		
		//TODO: open external (or own) editor
		
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

}
