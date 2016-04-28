package brabra.game.scene;

public class SceneFile {
	
	private final static String toDefaultImg = "resource/gui/ball.png";
	private String name = "default";
	private String filePath = null;
	private String imgPath = null;
	private String description = null;
	private String content = null;
	
	public SceneFile() {}
	
	public SceneFile(String name, String path, String imgPath, String description) {
		this.name = name;
		this.filePath = path;
		this.imgPath = imgPath;
		this.description = description;
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
		return imgPath == null ? toDefaultImg : imgPath;
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
