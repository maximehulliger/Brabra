package brabra.game.scene;


import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import brabra.Debug;
import brabra.ProMaster;
import brabra.game.scene.SceneFile;

/** 
 * Class responsible for getting the scene files 
 * and to load a scene file into the scene. 
 **/
public final class SceneLoader extends ProMaster {
	
	/** path from root to scene folder. */
	private final static String toSceneFolder = "scenes";
	
	/** Array representing the currently loaded local files. */
	public final ConcurrentLinkedDeque<SceneFile> scenes = new ConcurrentLinkedDeque<>();
	
	/** The name of the file to load. */
	private SceneFile currentScene = null;
	
	private final XMLReader xmlreader;
	
	public SceneLoader() {
		XMLReader xmlreader = null;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
		    SAXParser parser = factory.newSAXParser();
		    xmlreader = parser.getXMLReader();
		    xmlreader.setContentHandler(new SceneFileHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.xmlreader = xmlreader;
	}
	
	public void setFile(SceneFile scene) {
		currentScene = scene;
	}
	
	/** Load the object from the current scene file. Clear the scene before. */
	public void load() {
		final String filePath = app.pathToFolder(toSceneFolder) + currentScene.getFilePath();
		try {
			game.scene.clear();
			xmlreader.parse(filePath);
		} catch (FileNotFoundException e) {
			Debug.err("file : '"+filePath+"' not found !");
		} catch (Exception e) {
			Debug.err("\nerreur dans scene.xml:");
			e.printStackTrace();
		}
	}
	
	public void loadLocalFiles() {
		scenes.clear();
		
		scenes.addAll(Scene.providerLocal.fetch());

		
		// set default
		if (currentScene == null)
			currentScene = scenes.getFirst();
		
		if (scenes.size() == 0)
			Debug.err("no .xml scene files found in '/resource/scenes/' :'(");
		
		game.scene.model.notifyChange(Scene.Model.Change.SceneFileChanged, null);
	}

	private class SceneFileHandler extends DefaultHandler {
		
		private final Stack<Object> parentStack = new Stack<>();
		
	    public void startElement(String namespaceURI, String localName,String qName, org.xml.sax.Attributes atts) 
	    		throws SAXException {
	    	if (localName.equals("Scene") || localName.equals("Settings") || localName.equals("Physic")) {
	    		app.para.validate(atts);
	    		parentStack.push(null);
	    	} else {
	    		// create object & add it to the scene
	    		final Object newObj = game.scene.getPrefab(localName);
	    		newObj.validate(new Attributes(atts, parentStack.peek()));
    			game.scene.add(newObj);
				parentStack.push(newObj);
	    	}
	    }
	    
		public void endElement(String uri, String localName, String qName) 
				throws SAXException {
			parentStack.pop();
		}
	}
	
	/** Class that carry the attributes of a particular object in the xml file. has it's rightful parent too. */
	public static class Attributes extends HashMap<String, String> {
		private static final long serialVersionUID = -6595200285921918122L;
		private final Object parent;

		public Attributes(org.xml.sax.Attributes attr, Object parent) {
			for (int i=0; i < attr.getLength(); i++)
				super.put(attr.getQName(i), attr.getValue(i));
			this.parent = parent;
		}
		
		public Attributes() {
			this.parent = null;
		}
		
		public String getValue(String field) {
			return get(field);
		}
		
		public Object parent() {
			return parent;
		}
	}
}
