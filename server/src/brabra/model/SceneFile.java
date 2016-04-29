package brabra.model;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class SceneFile {
	
	private String name = "default";
	private String filePath = "";
	private String imgPath = "";
	private String description = "";
	private String content = "";
	
	
	public SceneFile set(String name, String path, String imgPath, String description) {
		this.name = name;
		this.filePath = path;
		this.imgPath = imgPath;
		this.description = description;
		return this;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	/** Set the relative path from the scene folder to the scene xml file. */
	public void setFilePath(String path) {
		this.filePath = path;
	}

	/** Get the relative path from the scene folder to the scene xml file. */
	public String getFilePath() {
		assert (filePath != null);
		return filePath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}
	
	public String getImgPath() {
		return imgPath;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
}