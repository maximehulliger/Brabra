package cs211.tangiblegame.realgame;

import processing.core.PShape;
import processing.core.PVector;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.physic.Collider;
import cs211.tangiblegame.Color;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.physic.Object;

//gère la camera, le background et la lumière. 
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
	private PVector focus = zero;
	private PVector orientation = defaultOrientation;
	
	private static PVector distNot = vec(100,100,100);
	private static PVector distStatic = vec(300,300,300);
	private static PVector distRel = PVector.mult(vec(0, 6, 9), 15f);
	
	public Camera() {
		super(distNot);
	}
	
	public void set(String followMode, String dist, Body toFollow) {
		if (followMode == null && dist == null && toFollow == null)
			return;
		
		if (toFollow != null) {
			setParent(toFollow, Parency.FollowPosition);
			if (followMode != null) {
				setMode(FollowMode.fromString(followMode));
			}
		} else if (followMode != null && dist != null) {
			PVector newDist = vec(dist);
			FollowMode mode = FollowMode.fromString(followMode);
			if (!newDist.equals(getDist(mode))) {
				setDist(mode, newDist);
				if (this.followMode == mode)
					setMode(null);
				else
					game.debug.info(2, "camera dist in "+followMode+" mode set at "+dist);
			}
			
		}
	}

	public void setSkybox(boolean displaySkybox) {
		this.displaySkybox = displaySkybox;
	}
	
	/** Change the camera mode and location. if mode is null, just updateAbs. display state. */
	public void setMode(FollowMode mode) {
		if (mode != null) {
			this.followMode = mode;
			this.locationRel.set(getDist(mode));
		}
		updateAbs();
		displayState();
	}
	
	public void nextMode() {
		if (parent != null)
			setMode(followMode.next());
		else
			game.debug.msg(3, "Camera need an object to focus on.");
	}
	
	public void place() {
		// 1.update
		update();
		updateAbs();
		
		for (Collider c : game.physic.colliders)
			if (ProMaster.distSq(focus, c.location) > distSqBeforeRemove)
				game.physic.toRemove.add( c );
		
		// 2.draw
		app.camera(location.x, location.y, location.z, 
				focus.x, focus.y, focus.z, 
				orientation.x, orientation.y, orientation.z);
		
		pushLocal();
		if (displaySkybox) {
			app.shape(skybox);
		} else {
			app.background(200);
			app.ambientLight(255, 255, 255);
			app.directionalLight(50, 100, 125, 0, -1, 0);
		}
		popLocal();
		
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
		if (followMode == FollowMode.Not)
			game.debug.info(2, "camera fixed at "+location);
		else
			game.debug.info(2, "camera following "+parent+" at "+parent.location
					+" from +"+locationRel+" in "+followMode+" mode.");
	}
	
	public void drawMouseray(float dist) {
		float focal = 10;
	    PVector mrel = new PVector(-(app.mouseX-app.width/2)/focal, -(app.mouseY-app.height/2)/focal, -focal);
	    
	    app.fill(0,0,0);
	    app.sphere(5);
	    //this finds the position of the mouse in model space
	    PVector mousePos = absolute(mrel, location, identity);

	    PVector camToMouse=PVector.sub(mousePos, location);

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
	    System.out.println("cam pos: "+location);
	    System.out.println("mouse pos: "+mousePos);
	    System.out.println("cam to mouse: "+camToMouse);
	    System.out.println("cam to focus: "+PVector.sub(location, focus));
	}

	public void updateAbs() {
		if (parent == null) {
			assert(followMode == FollowMode.Not);
		} else if (parent.transformChanged) {
			orientation = (followMode == FollowMode.Relative) ?
					parent.orientation() : defaultOrientation;
			switch(followMode) {
			case Static:
				focus = parent.location;
			case Relative:
				focus = parent.absUp(60);
			default:
				focus = zero;
			}
		}
	}

	private void setDist(FollowMode mode, PVector dist) {
		switch(mode) {
		case Not:
			distNot = dist;
			break;
		case Static:
			distStatic = dist;
			break;
		case Relative:
			distRel = dist;
			break;
		}
	}
	
	private PVector getDist(FollowMode mode) {
		switch(followMode) {
		case Not:
			return distNot;
		case Static:
			return distStatic;
		case Relative:
			return distRel;
		default: 
			return zero;
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
