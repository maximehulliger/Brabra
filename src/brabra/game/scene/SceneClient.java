package brabra.game.scene;

import java.io.StringReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;

import brabra.game.scene.SceneFile;


public class SceneClient {

	public String getDistantName() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8080/BrabraServer/api/");

		String response = target
				.path("getScenes")
				.request()
				.accept(MediaType.APPLICATION_XML)
				.get(String.class);

		SceneFile scene = JAXB.unmarshal(new StringReader(response), SceneFile.class);
		return scene.getName();
	}
	
	public boolean ping(String name){
		try{
            InetAddress address = InetAddress.getByName(getDomainName(name));
            address.isReachable(10);
            return true;
        } catch (Exception e){
        	e.printStackTrace();
            return false;
        }
	}
	private String getDomainName(String url) throws URISyntaxException {
	    URI uri = new URI(url);
	    String domain = uri.getHost();
	    return domain.startsWith("www.") ? domain.substring(4) : domain;
	}
}