package brabra.gui.field;

import brabra.gui.view.View;
import brabra.model.SceneFile;
import brabra.Brabra;
import brabra.game.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;


public class SceneField extends Field.Static {

	// private final static String toDefaultSceneImg = "resource/gui/scene/default.png";
	
	
	public SceneField(SceneFile sceneFile, Scene scene) {


		//--- View:
		
		super.setName(sceneFile.getName());
		
		// desc + image (maybe)
		final HBox upHolder = new HBox(), downHolder = new HBox();
		// description
		final Label descriptionLabel = new Label(sceneFile.getDescription());
		upHolder.getChildren().add(descriptionLabel);
		// img
		final String imagePath = sceneFile.getImgPath();
		// TODO add default image
		// if (imagePath == null)
		// 	   imagePath = toDefaultSceneImg;
		if (imagePath != null) {
			final ImageView image = View.getNewImage(imagePath);
			image.getStyleClass().add("sceneField-img");
			upHolder.getChildren().add(image);
		}
		
		// buttons
		final Button seeButton = View.getNewButton("(See in editor)");
		final Button execButton = View.getNewButton("Load");
		downHolder.getChildren().addAll(seeButton, execButton);

		// link to field
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
}
