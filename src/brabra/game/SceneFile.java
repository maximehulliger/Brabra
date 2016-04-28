package brabra.game;

public class SceneFile {
	
	private final static String defaultImgPath = "data/gui/ball.png";
	private String name = "default";
	private String path = null;
	private String imgPath = null;
	private String description = null;
	
	public SceneFile() {}
	
	public SceneFile(String name, String path, String imgPath, String description) {
		this.name = name;
		this.path = path;
		this.imgPath = imgPath;
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}
	
	public String getImgPath() {
		return imgPath == null ? defaultImgPath : imgPath;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
