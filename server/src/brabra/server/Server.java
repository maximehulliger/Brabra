package brabra.server;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import brabra.model.SceneFile;


@Path("api/")
public class Server extends Application {
	
	public static final String pong = "pong !";
	
	@GET
	@Path("ping")
	@Produces({MediaType.TEXT_PLAIN})
    public String ping(){
		System.out.println("ping to server !");
        return pong;
    } 

	@GET
	@Path("scenes")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response scenes(){
		final List<SceneFile> scenes = new ArrayList<SceneFile>();
		
		SceneFile scene1 = new SceneFile();
		scene1.setName("testScene");
		scene1.setDescription("nothing");
		scenes.add(scene1);
		GenericEntity<List<SceneFile>> generic = new GenericEntity<List<SceneFile>>(scenes) {};
        return Response.status(201).entity(generic).build();
    }
} 