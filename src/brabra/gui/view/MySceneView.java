package brabra.gui.view;

import java.util.ArrayList;
import brabra.game.SceneFile;
import brabra.gui.field.SceneField;


public class MySceneView extends View {
    
	public MySceneView() {
		
		// get Scenes
		ArrayList<SceneFile> sceneFiles =  new ArrayList<>();
		sceneFiles.add(new SceneFile("default", "default.xml", null, "Just the default scene :)\n\n- Maxime"));
		
		//--- View:
		super.setTitle("My Scenes, fud :)");
		sceneFiles.forEach(sf -> addContent(new SceneField(sf)));
	}
}
