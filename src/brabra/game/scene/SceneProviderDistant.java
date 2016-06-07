package brabra.game.scene;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import brabra.game.scene.SceneFile;

/** A class that provide scenes from a distant server with a certain adress. */
public class SceneProviderDistant {
	
	public final static String defaultServerAdress = "http://localhost:8080/";
	
	private final Supplier<String> getServerAdress;

	public SceneProviderDistant(Supplier<String> getServerAdress) {
		this.getServerAdress = getServerAdress;
	}

	public SceneProviderDistant(String serverAdress) {
		this(() -> serverAdress);
	}

	public SceneProviderDistant() {
		this(defaultServerAdress);
	}

	/** Fetch the scenes from the server and add them to the list. return true if ok. */
	public boolean fetchSafe(List<SceneFile> listToFill) {
		try {
			listToFill.addAll(fetch());
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/** Test the connection with the server. Return true if ok. */
	public boolean pingSafe() {
		try {
			return ping();
		} catch (Exception e) {
			return false;
		}
	}
	
	/** 
	 * Test the connection with the server. Return true if ok. 
	 * @throws java.net.ConnectException if the server adress is unvalid. 
	 **/
	protected boolean ping() {
		return "pong !".equals(get("ping", MediaType.TEXT_PLAIN, String.class));
	}
	
	/** 
	 * Fetch the scenes from the server and return them.
	 * @throws java.net.ConnectException if the server adress is unvalid. 
	 **/
	protected List<SceneFile> fetch() {
		return Arrays.asList(get("scenes", MediaType.APPLICATION_XML, SceneFile[].class));
	}
	
	/** 
	 * To get a resource from the server api. 
	 * @throws java.net.ConnectException if the server adress is unvalid. 
	 **/
	private <T> T get(String path, String mediaType, Class<T> dataClass) {
		final Client client = ClientBuilder.newClient();
		final WebTarget target = client.target(getServerAdress.get()+"BrabraServer/api/");
		return target.path(path).request().accept(mediaType).get(dataClass);
	}
}