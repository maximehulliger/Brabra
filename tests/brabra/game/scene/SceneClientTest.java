package brabra.game.scene;

import static org.junit.Assert.*;

import org.junit.Test;

public class SceneClientTest {

	private final SceneClient sceneClient = new SceneClient();
	
	@Test
	public void pingTest() {
		assertTrue(sceneClient.ping("http://localhost:8080/"));
	}
	
	@Test
	public void getSceneTest() {
		assertTrue(sceneClient.getDistantName() != null);
	}
}
