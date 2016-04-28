package brabra.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("api/getScenes")
public class Server {
	
  // This method is called if XMLis request
  @GET
  @Produces({MediaType.APPLICATION_XML})
  public SceneFile getScene() {
	  SceneFile scene = new SceneFile();
	  
	  scene.set("testScene","nothing");
	  
	  return scene;
  }
} 