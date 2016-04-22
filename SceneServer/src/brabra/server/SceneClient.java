package brabra.server;

import java.io.StringReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;


public class SceneClient {

	public static void test() {
		Client client = ClientBuilder.newClient();
		//final URI uri = UriBuilder.fromUri("http://localhost:8080/SceneServer").build();
		WebTarget target = client.target("http://localhost:8080/SceneServer");

		String response = target.path("rest").
				path("ProtoServer").
				request().
				accept(MediaType.APPLICATION_XML).
				get(String.class);

		SceneFile scene = JAXB.unmarshal(new StringReader(response), SceneFile.class);
		System.out.println(scene.getname());
	}
}