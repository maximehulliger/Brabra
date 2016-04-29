package brabra.game.scene;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import brabra.model.SceneFile;
import brabra.model.SceneProvider;

public class SceneClientTest {

	private final SceneProvider sceneProvider = new SceneProvider();
	
	@Test
	public void pingTest() throws IOException {
		assertTrue(sceneProvider.ping());
	}
	
	@Test
	public void getSceneTest() {
		List<SceneFile> sceneFiles = sceneProvider.fetch();

		assertTrue(sceneFiles != null);
		assertTrue(sceneFiles.size() != 0);
	}
}
