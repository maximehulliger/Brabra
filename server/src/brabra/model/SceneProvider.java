package brabra.model;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import brabra.server.Server;

/** A class that provide scenes from a distant server with a certain adress. */
public class SceneProvider {
	
	public final static String defaultServerAdress = "http://localhost:8080/";
	
	private final String serverAdress;

	public SceneProvider() {
		this.serverAdress = defaultServerAdress;
	}

	public SceneProvider(String serverAdress) {
		this.serverAdress = serverAdress;
	}

	/** Test the connection with the server. Return true if ok. */
	public boolean ping() {
		return Server.pong.equals(get("ping", MediaType.TEXT_PLAIN, String.class));
	}
	
	/** Fetch the scenes from the server and return them */
	public List<SceneFile> fetch() {
		return Arrays.asList(get("scenes", MediaType.APPLICATION_XML, SceneFile[].class));
	}
	
	/** To get a resource from the server api. */
	private <T> T get(String path, String mediaType, Class<T> dataClass) {
		final Client client = ClientBuilder.newClient();
		final WebTarget target = client.target(serverAdress+"BrabraServer/api/");
		return target.path(path).request().accept(mediaType).get(dataClass);
	}
}