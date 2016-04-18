package brabra.game.scene;

import brabra.ProMaster;
import brabra.Brabra;
import brabra.game.Color;
import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.geo.Vector;
import processing.core.PShape;

/** 
 * Class dealing with the camera, background and light (+removal of far objects). 
 * Default mode is Not (see FollowMode for more).
 * If the camera is placed before objects update, 
 * the camera position will be 1 frame late from the objects in scene. 
 **/ 
public class Camera extends Object {
	/** 
	 * All the modes for the camera. <p>
	 * Not: fixed at distNot, looking at zero. <p>
	 * Static: follow the parent in static mode. <p>
	 * Full: follow the parent in full mode.
	 **/
	public enum FollowMode {
		Not, Static, Full;
		public FollowMode next() {
	        return values()[(this.ordinal()+1) % values().length];
	    }
		public static FollowMode fromString(String f) {
			if (f.equals("static"))
				return FollowMode.Static;
			else if (f.equals("full") || f.equals("relative"))
				return FollowMode.Full;
			else if (f.equals("fixed") || f.equals("not") || f.equals("none"))
				return FollowMode.Not;
			else {
				game.debug.err("camera mode unknown: \""+f+"\", taking Not");
				return FollowMode.Not;
			}
		}
	}
	
	// static / definitive stuff
	public static final float close = 0.05f;
	public static final float far = 10_000;
	private static final float distSqBeforeRemove = far*far; //from camera
	private static final Vector defaultOrientation = y(-1);
	private static PShape skybox;
	private static final Color 
			xColor = new Color("red", true), 
			yColor = new Color("green", true), 
			zColor = new Color("blue", true),
			pointCentralColor = new Color("red", true);
	
	// simple flags
	private boolean displaySkybox = true;
	private boolean displayCentralPoint = true;
	private boolean displayAxis = true;
	
	// intern, mode related
	private FollowMode followMode = FollowMode.Not;
	/** The absolute point that looks the camera. */
	private final Vector focus = zero.copy();
	private final Vector orientation = defaultOrientation.copy();
	private final Vector distNot = Vector.cube(300);
	private final Vector distStatic = Vector.cube(300);
	private final Vector distFull = add(up(90), behind(135));
	private boolean stateChanged = false, stateChangedCurrent = false;
	private boolean absValid = false;
	
	/** Creates a new camera. */
	public Camera() {
		super(Vector.cube(100));
		setName("Camera");
	}
	
	// --- Getters ---
	
	protected boolean absValid() {
		return absValid && super.absValid();
	}
	
	public void displayState() {
		game.debug.info(2, presentation()+" "+state()+state(false));
	}
	
	public String getStateUpdate() {
		boolean sSC = super.stateChanged();
		return (sSC ? super.getStateUpdate() : "")
				+(sSC && stateChanged ? "\n" : "")
				+(stateChanged ? state() : "");
	}
	
	public boolean stateChanged() {
		return stateChanged || super.stateChanged();
	}

	private String state() {
		updateAbs();
		return (followMode == FollowMode.Not ? "fixed at "+locationAbs
				: "at "+parent().location()+" from +"+locationRel+" in "+followMode+" mode.");
				//+ " looking at "+focus+" orientation: "+orientation;
	}
	
	// --- Setters ---

	public void setDisplaySkybox(boolean displaySkybox) {
		this.displaySkybox = displaySkybox;
	}
	
	public void setDisplayCentralPoint(boolean displayCentralPoint) {
		this.displayCentralPoint = displayCentralPoint;
	}
	
	public void setDisplayAxis(boolean displayAxis) {
		this.displayAxis = displayAxis;
	}
	
	/** Set the object followed by the camera. toFollow & followMode should be non-null. */
	public void set(Object toFollow, String followMode, String dist) {
		assert(toFollow != null && followMode != null);
		// get follow mode
		final FollowMode mode = FollowMode.fromString(followMode);
		// update dist if set
		if (dist != null)
			setDist(mode, vec(dist));
		// check input
		if (toFollow == this)
			game.debug.err("The camera can not follow itself...");
		else {
			// apply
			setParent(toFollow);
			setMode(mode);
		}
	}
	
	public boolean setParent(Object p) {
		if (super.setParent(p)) {
			stateChangedCurrent = true;
			absValid = false;
			return true;
		} else
			return false;
	}
	
	/** To let parentRel be consistent with followMode. */
	public void setParentRel(ParentRelationship rel) {
		if (rel == ParentRelationship.None)
			setMode(FollowMode.Not);
		switch (followMode) {
		case Not:
			super.setParentRel(ParentRelationship.None);
			break;
		case Full:
			super.setParentRel(ParentRelationship.Full);
			break;
		default: // Static
			super.setParentRel(ParentRelationship.Static);
			break;
		}
	}

	/** Set the camera relative dist for this mode. */
	public void setDist(FollowMode mode, Vector dist) {
		if ( !dist.equals(getDist(mode)) ) {
			switch(mode) {
			case Not:
				distNot.set(dist);
				break;
			case Static:
				distStatic.set(dist);
				break;
			case Full:
				distFull.set(dist);
				break;
			}
			if (mode == followMode) {
				absValid = false;
				stateChangedCurrent = true;
			}
		}
	}
	
	/** Change the camera mode and location. display state. */
	public void setMode(FollowMode mode) {
		if (mode == followMode && absValid)
			assert(locationRel.equals(getDist(mode))); //should already be set
		else {
			followMode = mode;
			setParentRel(null);
			locationRel.set(getDist(mode));
			stateChangedCurrent = true;
			absValid = false;
			displayState();
		}
	}
	
	/** Switch camera mode. */
	public void nextMode() {
		if (parent() != null)
			setMode(followMode.next());
		else
			game.debug.msg(3, "Camera need an object to focus on.");
	}
	
	// --- Raycast from screen ---
	
	/*public void drawMouseray(float dist) { //TODO
		float focal = 10;
	    Vector mrel = new Vector(-(app.mouseX-app.width/2)/focal, -(app.mouseY-app.height/2)/focal, -focal);
	    
	    app.fill(0,0,0);
	    app.sphere(5);
	    //this finds the position of the mouse in model space
	    Vector mousePos = absolute(mrel, locationAbs, identity);

	    Vector camToMouse=Vector.sub(mousePos, locationAbs);

	    app.stroke(150, 150, 150, 255); //box line colour
	    line(camToMouse, mousePos);
	    app.noStroke();
	    app.pushMatrix();
		    //translate(mouseX-width/2, mouseY-height/2,0);
		    translate(camToMouse);
		    app.fill(255,0,0);
		    app.sphere(5);
	    app.popMatrix();
	    app.pushMatrix();
		    translate( mousePos );
		    app.fill(0,255,0);
		    app.sphere(5);
	    app.popMatrix();
	    System.out.println("cam pos: "+locationAbs);
	    System.out.println("mouse pos: "+mousePos);
	    System.out.println("cam to mouse: "+camToMouse);
	    System.out.println("cam to focus: "+Vector.sub(locationAbs, focus));
	}*/
	
	// --- main usage (draw) ---

	/** Put the camera in the processing scene and carry his job (see class doc). */
	public void place() {
		game.debug.setCurrentWork("camera");
		updateAbs();
		
		// we remove the objects too far away.
		game.scene.forEachObjects(o -> {
			if (ProMaster.distSq(focus, o.location()) > distSqBeforeRemove)
				game.scene.remove(o);
		});
		
		// draw all the stuff
		app.background(200);
		app.camera(locationAbs.x, locationAbs.y, locationAbs.z, 
				focus.x, focus.y, focus.z, 
				orientation.x, orientation.y, orientation.z);
		if (displaySkybox) {
			app.pushMatrix();
				translate(locationAbs);
				app.shape(skybox());
			app.popMatrix();
		} else {
			//app.directionalLight(50, 100, 125, 0, -1, 0);
			app.ambientLight(255, 255, 255);
		}
		
		if (displayAxis)
			displayAxis();
		
		//drawMouseray(50);
	}
	
	/** Display (maybe) a point at the center of the screen. */
	public void gui() {
		if (displayCentralPoint) {
			pointCentralColor.fill();
			app.point(Brabra.width/2, Brabra.height/2);
		}
	}
	
	// --- life cycle (validate + update) ---

	public boolean validate(Attributes atts) {
		if (super.validate(atts)) {
			final String mode = atts.getValue("mode");
			if (mode != null) {
				final String distString = atts.getValue("dist");
				final Vector dist = distString != null ? vec(distString) : locationRel;
				if (dist == null)
					game.debug.err("for camera: dist (or pos) should be set with mode. ignoring.");
				else if (dist.equals(zero))
					game.debug.err("for camera: dist (or pos) should not be zero. ignoring.");
				else
					setDist(FollowMode.fromString(mode), dist);
			}
			final String displaySkybox = atts.getValue("displaySkybox");
			if (displaySkybox != null)
			  	setDisplaySkybox(Boolean.parseBoolean(displaySkybox));
			final String displayCentralPoint = atts.getValue("displayCentralPoint");
			if (displayCentralPoint != null)
				setDisplayCentralPoint(Boolean.parseBoolean(displayCentralPoint));
			final String displayAxis = atts.getValue("displayAxis");
			if (displayAxis != null)
				setDisplayAxis(Boolean.parseBoolean(displayAxis));
			return true;
		} else
			return false;
	}
	
	public void onDelete() {
		super.onDelete();
		this.unvalidate();
	}
	
	public boolean update() {
		if (super.update()) {
			stateChanged = stateChangedCurrent;
			stateChangedCurrent = false;
			return true;
		} else
			return false;
	}

	protected boolean updateAbs() {
		boolean sUpdated = super.updateAbs();
		if (!absValid || sUpdated) {
			// get new values.
			Vector newFocus;
			Vector newOrientation;
			Vector newLocationRel = getDist(followMode);
			
			if (!hasParent() || parent()==null) {
				assert (followMode == FollowMode.Not);
			}
			
			switch(followMode) {
			case Static:
				newFocus = parent().location();
				newOrientation = defaultOrientation;
				break;
			case Full:
				newFocus = parent().absolute(up(60));
				newOrientation = absoluteDir(down);
				break;
			default: //Not
				newFocus = zero;
				newOrientation = defaultOrientation;
				break;
			}
			// apply them if needed.
			if (!locationRel.equals(newLocationRel))
				locationRel.set(newLocationRel);
			if (!orientation.equals(newOrientation))
				orientation.set(newOrientation);
			if (!focus.equals(newFocus))
				focus.set(newFocus);
			absValid = true;
			return true;
		} else
			return false;
	}
	
	// --- private ---

	private static PShape skybox() {
		if (skybox == null) {
			skybox = app.loadShape("skybox.obj");
			skybox.scale(far/90);
		}
		return skybox;
	}
	
	private Vector getDist(FollowMode mode) {
		switch(followMode) {
		case Static:
			return distStatic;
		case Full:
			return distFull;
		default:
			return distNot;
		}
	}
	
	private void displayAxis() {
		line(zero, x(far), xColor);
		line(zero, y(far), yColor);
		line(zero, z(far), zColor);
	}
}
