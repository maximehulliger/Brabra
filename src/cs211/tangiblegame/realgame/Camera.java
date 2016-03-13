package cs211.tangiblegame.realgame;

import processing.core.PShape;
import processing.core.PVector;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.Color;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.physic.Object;

/** 
 * Class dealing with the camera, background and light. (+removal of far objects)
 * Default mode is Not.
 **/ 
public class Camera extends Object {
	public static final float distSqBeforeRemove = 12_000*12_000; 	//distance du vaisseau avant remove
	private static final PVector defaultOrientation = vec(0,-1,0); 	//orientation
	
	public enum FollowMode {
		Not, Static, Relative;
		public FollowMode next() {
	        return values()[(this.ordinal()+1) % values().length];
	    }
		
		public static FollowMode fromString(String f) {
			if (f.equals("static"))
				return FollowMode.Static;
			else if (f.equals("relative"))
				return FollowMode.Relative;
			else if (f.equals("fixed") || f.equals("not"))
				return FollowMode.Not;
			else {
				System.err.println("mode pour camera inconu : \""+f+"\"\n");
				return FollowMode.Not;
			}
		}
	}
	
	public static PShape skybox;
	
	private boolean displaySkybox = false;
	private boolean displayPointCentral = true;
	private Color colorPointCentral = Color.get("red");
	private boolean displayAxis = true;
	
	private FollowMode followMode = FollowMode.Not;
	/** The absolute point that looks the camera. */
	private final PVector focus = new NVector(zero);
	private final PVector orientation = new NVector(defaultOrientation);
	
	private final PVector distNot = cube(100);
	private final PVector distStatic = cube(300);
	private final PVector distRel = mult(vec(0, 6, 9), 15f);
	private boolean stateChanged = false, stateChangedCurrent = false;
	private boolean absValid = true; //as super( distNot );
	
	/** Creates a new camera and add it to the physic objects. */
	public Camera() {
		super( cube(100) );
		setName("Camera");
		game.physic.add( this );
	}
	
	public void update() {
		stateChanged = stateChangedCurrent;
		stateChangedCurrent = false;
		super.update();
	}

	public boolean absValid() {
		return absValid && super.absValid();
	}
	
	public void set(String followMode, String dist, Body toFollow) {
		if (followMode == null && dist == null && toFollow == null)
			return;
		if (toFollow != null) {
			setParent(toFollow);
			stateChangedCurrent = true;
			absValid = false;
		} 
		if (followMode != null && dist != null) {
			PVector newDist = vec(dist);
			FollowMode mode = FollowMode.fromString(followMode);
			if (!newDist.equals(getDist(mode))) {
				setDist(mode, newDist);
				if (this.followMode == mode)
					setMode(mode); //to update
			}
		}
	}

	public void setSkybox(boolean displaySkybox) {
		this.displaySkybox = displaySkybox;
	}
	
	/** Change the camera mode and location. display state. */
	public void setMode(FollowMode mode) {
		if (mode == followMode)
			assert(!locationRel.equals(getDist(mode))); //only called on changes
		followMode = mode;
		switch (followMode) {
		case Not:
			setParentRel(ParentRelationship.None);
			break;
		default:
			setParentRel(ParentRelationship.Static);
			break;
		}
		locationRel.set(getDist(mode));
		stateChangedCurrent = true;
		absValid = false;
		displayState();
	}
	
	public void nextMode() {
		if (hasParent())
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
		
		// draw basic stuff
		app.background(200);
		app.camera(locationAbs.x, locationAbs.y, locationAbs.z, 
				focus.x, focus.y, focus.z, 
				orientation.x, orientation.y, orientation.z);
		
		if (displaySkybox) {
			pushLocal();
			app.shape(skybox);
			popLocal();
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
			colorPointCentral.fill();
			app.point(app.width/2, app.height/2);
		}
	}
	
	public void displayState() {
		updateAbs();
		game.debug.info(2, presentation()+" "+state());
	}
	
	private String state() {
		return (followMode == FollowMode.Not ? "fixed at "+locationAbs
				: "at "+parent().location()+" from +"+locationRel+" in "+followMode+" mode.")
				+ " orientation: "+orientation;
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
	
	public void setParent(Object p) {
		super.setParent(p);
		setParentRel(ParentRelationship.Static);
	}
	
	public void drawMouseray(float dist) { //TODO
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
	}

	public boolean updateAbs() {
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
			case Relative:
				newFocus = parent().absUp(60);
				newOrientation = parent().orientation();
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

	private void setDist(FollowMode mode, PVector dist) {
		switch(mode) {
		case Not:
			distNot.set(dist);
			break;
		case Static:
			distStatic.set(dist);
			break;
		case Relative:
			distRel.set(dist);
			break;
		}
	}
	
	private PVector getDist(FollowMode mode) {
		switch(followMode) {
		case Static:
			return distStatic;
		case Relative:
			return distRel;
		default:
			return distNot;
		}
	}
	
	private void displayAxis() {
		app.stroke(255, 0, 0);
		app.line(0, 0, 0, far, 0, 0);
		app.stroke(0, 255, 0);
		app.line(0, 0, 0, 0, far, 0);
		app.stroke(0, 0, 255);
		app.line(0, 0, 0, 0, 0, far);
	}
}
