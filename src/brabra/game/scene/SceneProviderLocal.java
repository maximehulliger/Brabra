package brabra.game.scene;

import java.util.ArrayList;
import java.util.List;

import brabra.model.SceneFile;

public class SceneProviderLocal {

	/** Fetch the scenes from the server and return them */
	public List<SceneFile> fetch() {
		ArrayList<SceneFile> scenes = new ArrayList<>();
		scenes.add(
				new SceneFile().set(
						"default", 
						"default.xml", 
						"resource/gui/scene/default_local_1.jpg", 
						"Just the default scene :)\n\n- Maxime"
						));

		// TODO: get scenes from /resource/scene/
		
		return scenes;
	}
	
}
