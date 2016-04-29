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

import brabra.ProMaster;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import brabra.model.SceneFile;

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
			app.debug.err("file : '"+filePath+"' not found !");
		} catch (Exception e) {
			app.debug.err("\nerreur dans scene.xml:");
			e.printStackTrace();
		}
	}
	
	public void loadLocalFiles() {
		scenes.clear();
		
		// TODO: get scenes from /resource/scene/
		
		scenes.add(new SceneFile().set("default", "default.xml", null, "Just the default scene :)\n\n- Maxime"));
		
		// set default
		currentScene = scenes.getFirst();
		
		if (scenes.size() == 0)
			app.debug.err("no .xml scene files found in '/resource/scenes/' :'(");
		
		game.scene.notifyChange(Scene.Change.SceneFileChanged, null);
	}

	private class SceneFileHandler extends DefaultHandler {
		private final Stack<Object> parentStack = new Stack<>();
		private final Stack<Attributes> attrStack = new Stack<>();
		
	    public void startElement(String namespaceURI, String localName,String qName, org.xml.sax.Attributes atts) 
	    		throws SAXException {
	    	if (localName.equals("Scene"))
	    		return;
	    	else if (localName.equals("Settings") || localName.equals("Physic"))
	    		app.para.validate(atts);
	    	else {
	    		// get loc & dir
	    		final String locString = atts.getValue("pos");
	    		final String dirString = atts.getValue("dir");
	    		final Vector loc = locString != null ? vec(locString) : zero;
	    		final Quaternion rot = dirString != null ? Quaternion.fromDirection(vec(dirString), Vector.up) : identity;
	    		// create object
	    		final Object newObj = game.scene.getPrefab(localName, loc, rot);
				attrStack.push(new Attributes(atts, parentStack.empty() ? null : parentStack.peek()));
				parentStack.push(newObj);
	    	}
	    }
	    
		public void endElement(String uri, String localName, String qName) 
				throws SAXException {
			if (!localName.equals("Scene") && !localName.equals("Physic") && !localName.equals("Settings")) {
	    		final Object obj = parentStack.pop();
	    		final Attributes atts = attrStack.pop();
	    		if (obj != null) {
	    			obj.validate(atts);
	    			game.scene.add(obj);
	    		}
	    	}
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
