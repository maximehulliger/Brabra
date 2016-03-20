package cs211.tangiblegame.realgame;

import processing.core.PShape;
import processing.core.PVector;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.Color;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.physic.Object;

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
	
	/** Distance from camera before remove. */
	private static final float distSqBeforeRemove = far*far;
	private static final PVector defaultOrientation = y(-1);
	private static final Color 
			xColor = new Color("red", true), 
			yColor = new Color("green", true), 
			zColor = new Color("blue", true),
			pointCentralColor = new Color("red", true);
	private static PShape skybox;
	
	private boolean displaySkybox = true;
	private boolean displayPointCentral = true;
	private boolean displayAxis = true;
	
	private FollowMode followMode = FollowMode.Not;
	/** The absolute point that looks the camera. */
	private final PVector focus = zero.copy();
	private final PVector orientation = defaultOrientation.copy();
	private final PVector distNot = cube(300);
	private final PVector distStatic = cube(300);
	private final PVector distFull = add(up(90), behind(135));
	private boolean stateChanged = false, stateChangedCurrent = false;
	private boolean absValid = false;
	
	/** Creates a new camera and add it to the physic objects. */
	public Camera() {
		super(cube(100));
		setName("Camera");
		game.physic.addNow(this);
		if (skybox == null) {
			skybox = app.loadShape("skybox.obj");
			skybox.scale(far/90);
		}
	}
	
	public boolean update() {
		if (super.update()) {
			stateChanged = stateChangedCurrent;
			stateChangedCurrent = false;
			return true;
		} else
			return false;
	}

	protected boolean absValid() {
		return absValid && super.absValid();
	}
	
	public void set(Body toFollow, String followMode, String dist) {
		assert(toFollow != null && followMode != null);
		// 1. get follow mode
		FollowMode mode = FollowMode.fromString(followMode);
		// 2. update dist if set
		if (dist != null)
			setDist(mode, vec(dist));
		// 3. apply
		setParent(toFollow);
		setMode(mode);
	}

	public void setDist(FollowMode mode, PVector dist) {
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
	
	public void setSkybox(boolean displaySkybox) {
		this.displaySkybox = displaySkybox;
		game.debug.info(3, (displaySkybox ? "with" : "without")+" skybox.");
	}
	
	/** To let parentRel be consistent with followMode. */
	public void setParentRel(ParentRelationship rel) {
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
	
	public void nextMode() {
		if (parent() != null)
			setMode(followMode.next());
		else
			game.debug.msg(3, "Camera need an object to focus on.");
	}
	
	public void place() {
		game.debug.setCurrentWork("camera");
		updateAbs();
		
		// we remove the objects too far away.
		game.physic.objects().forEach(o -> {
			if (ProMaster.distSq(focus, o.location()) > distSqBeforeRemove)
				game.physic.remove(o);
		});
		
		// draw all the stuff
		app.background(200);
		app.camera(locationAbs.x, locationAbs.y, locationAbs.z, 
				focus.x, focus.y, focus.z, 
				orientation.x, orientation.y, orientation.z);
		if (displaySkybox) {
			app.pushMatrix();
				translate(locationAbs);
				app.shape(skybox);
			app.popMatrix();
		} else {
			//app.directionalLight(50, 100, 125, 0, -1, 0);
			app.ambientLight(255, 255, 255);
		}
		
		if (displayAxis)
			displayAxis();
		
		//drawMouseray(50);
	}
	
	public void gui() {
		if (displayPointCentral) {
			pointCentralColor.fill();
			app.point(TangibleGame.width/2, TangibleGame.height/2);
		}
	}
	
	public void displayState() {
		game.debug.info(2, presentation()+" "+state()+state(true,true,true,true));
	}
	
	protected String state() {
		updateAbs();
		return (followMode == FollowMode.Not ? "fixed at "+locationAbs
				: "at "+parent().location()+" from +"+locationRel+" in "+followMode+" mode.")
				+ " looking at "+focus+" orientation: "+orientation;
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
	
	public boolean setParent(Object p) {
		if (super.setParent(p)) {
			stateChangedCurrent = true;
			absValid = false;
			return true;
		} else
			return false;
	}
	
	/*public void drawMouseray(float dist) { //TODO
		float focal = 10;
	    PVector mrel = new PVector(-(app.mouseX-app.width/2)/focal, -(app.mouseY-app.height/2)/focal, -focal);
	    
	    app.fill(0,0,0);
	    app.sphere(5);
	    //this finds the position of the mouse in model space
	    PVector mousePos = absolute(mrel, locationAbs, identity);

	    PVector camToMouse=PVector.sub(mousePos, locationAbs);

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
	    System.out.println("cam to focus: "+PVector.sub(locationAbs, focus));
	}*/

	protected boolean updateAbs() {
		boolean sUpdated = super.updateAbs();
		if (!absValid || sUpdated) {
			// get new values.
			PVector newFocus;
			PVector newOrientation;
			PVector newLocationRel = getDist(followMode);
			switch(followMode) {
			case Static:
				newFocus = parent().location();
				newOrientation = defaultOrientation;
				break;
			case Full:
				newFocus = parent().absolute(up(60));
				newOrientation = orientation();
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

	private PVector getDist(FollowMode mode) {
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
