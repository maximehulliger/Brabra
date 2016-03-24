package brabra.game;


import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import brabra.ProMaster;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Quaternion;
import brabra.game.scene.Object;
import processing.core.PVector;

/** Class responsible to load the scene file. */
public final class XMLLoader extends ProMaster {
	
	/** The name of the file to load. */
	public String filename;
	
	private final XMLReader xmlreader;
	
	public XMLLoader() {
		filename = app.inputPath()+"scene.xml";
		XMLReader xmlreader = null;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
		    SAXParser parser = factory.newSAXParser();
		    xmlreader = parser.getXMLReader();
		    xmlreader.setContentHandler(new PrefabHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.xmlreader = xmlreader;
	}
	
	/** 
	 * Load the object from the file at filename. 
	 * To see the supported attributes for the objects, look at the validate(Attr) methods 
	 * or in the readme file.
	 **/
	public void load() {
		try {
			xmlreader.parse(filename);
		} catch (Exception e) {
			game.debug.err("\nerreur dans scene.xml:");
			e.printStackTrace();
		}
	}
	
	private class PrefabHandler extends DefaultHandler {
		private final Stack<Object> parentStack = new Stack<>();
		private final Stack<Attributes> attrStack = new Stack<>();
		
	    public void startElement(String namespaceURI, String localName,String qName, org.xml.sax.Attributes atts) 
	    		throws SAXException {
	    	if (localName.equals("scene"))
	    		return;
	    	else if (localName.equals("physic") || localName.equals("settings"))
	    		validate(atts);
	    	else {
	    		// get loc & dir
	    		final String locString = atts.getValue("pos");
	    		final String dirString = atts.getValue("dir");
	    		final PVector loc = locString != null ? vec(locString) : zero;
	    		final Quaternion rot = dirString != null ? Quaternion.fromDirection(vec(dirString)) : identity;
	    		// create object
	    		final Object newObj = game.scene.addPrefab(localName, loc, rot);
				attrStack.push(new Attributes(atts, parentStack.empty() ? null : parentStack.peek()));
				parentStack.push(newObj);
	    	}
	    }
	    
		public void endElement(String uri, String localName, String qName) 
				throws SAXException {
			if (!localName.equals("scene") && !localName.equals("physic") && !localName.equals("settings")) {
	    		final Object obj = parentStack.pop();
	    		final Attributes atts = attrStack.pop();
	    		if (obj != null)
	    			obj.validate(atts);
	    	}
		}
	}
	
	/** Validates the global settings of the program. */
	public void validate(org.xml.sax.Attributes atts) {
		final String verbosity = atts.getValue("verbosity");
		if (verbosity != null) {
			if (verbosity.equals("max") || verbosity.equals("all"))
				app.verbosity = Integer.MAX_VALUE;
			else if (verbosity.equals("min") || verbosity.equals("silence") || verbosity.equals("none"))
				app.verbosity = Integer.MIN_VALUE;
			else
				app.verbosity = Integer.parseInt(verbosity);
		}
		final String displayAllColliders =  atts.getValue("displayAllColliders");
		if (displayAllColliders != null)
			Collider.displayAllColliders = Boolean.parseBoolean(displayAllColliders);
		final String gravity = atts.getValue("gravity");
		if (gravity != null)
		  	game.physic.gravity = Float.parseFloat(gravity);
		final String running = atts.getValue("running");
	  	if (running != null)
	  		game.setRunning(Boolean.parseBoolean(running));
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
