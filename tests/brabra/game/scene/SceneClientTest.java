package brabra.game.scene;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import brabra.model.SceneFile;

public class SceneClientTest {

	private final SceneProviderDistant sceneProviderDistant = new SceneProviderDistant();
	
	@Test
	public void pingTest() throws IOException {
		assertTrue(sceneProviderDistant.ping());
	}
	
	@Test
	public void getSceneTest() {
		List<SceneFile> sceneFiles = sceneProviderDistant.fetch();

		assertTrue(sceneFiles != null);
		assertTrue(sceneFiles.size() != 0);
	}
}
