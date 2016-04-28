package brabra.gui.field;

import java.util.Observable;

import brabra.gui.view.View;
import brabra.Brabra;
import brabra.game.scene.Scene;
import brabra.game.scene.SceneFile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;


public class SceneField extends Field {

	public SceneField(SceneFile sceneFile, Scene scene) {

		//--- View:
		
		super.setName(sceneFile.getName());
		final GridPane contentHolder = new GridPane();
		// description & img.
		contentHolder.add(new Label(sceneFile.getDescription()), 0, 0);
		contentHolder.add(new ImageView(new Image(sceneFile.getImgPath())), 1, 0);
		// buttons
		final Button execButton = View.getNewButton("Load");
		contentHolder.add(execButton, 1, 1);
		final Button seeButton = View.getNewButton("See in editor");
		contentHolder.add(seeButton, 0, 1);		

		// link to field
		subfields().add(contentHolder);
		
		// TODO:to css
		//contentHolder.getColumnConstraints().add(new ColumnConstraints(150));
		//contentHolder.getColumnConstraints().add(new ColumnConstraints(150));
		contentHolder.setHgap(10); //horizontal gap in pixels => that's what you are asking for
		contentHolder.setVgap(10);
		contentHolder.setAlignment(Pos.CENTER);
		contentHolder.setPadding(new Insets(20,20,20,20));
		
		//--- Control:
		
		//TODO: load scene in processing

		
		execButton.setOnAction(e -> {
			Brabra.app.runLater(() -> {
				scene.loader.setFile(sceneFile);
				scene.loader.load();
			});
		});
		
		//TODO: open external editor
		
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

}
