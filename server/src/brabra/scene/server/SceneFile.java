package brabra.scene.server;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SceneFile {
	private String name;
	private String content;
	public void set(String name, String content){
		this.name = name;
		this.content = content;
	}
	public String getname() {
		return name;
	}
	public void setname(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
} 
