package cs211.tangiblegame.realgame;

import processing.core.PShape;
import processing.core.PVector;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.physic.Collider;
import cs211.tangiblegame.ProMaster;

//gère la camera, le background et la lumière. 
public class Camera extends ProMaster {
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
	private boolean pointCentral = true;
	private boolean drawAxis = true;
	
	private FollowMode followMode = FollowMode.Not;
	private Body toFollow = null;
	private PVector position = distNot;
	private PVector focus = zero;
	private PVector orientation = defaultOrientation;
	
	private static PVector distNot = vec(100,100,100);
	private static PVector distStatic = vec(300,300,300);
	private static PVector distRel = PVector.mult(vec(0, 6, 9), 15f);
	
	public void set(String followMode, String dist, Body toFollow) {
		if (followMode == null && dist == null && toFollow == null)
			return;
		
		if (toFollow != null) {
			this.toFollow = toFollow;
			if (followMode != null) {
				this.followMode = FollowMode.fromString(followMode);
				displayState();
			}
		}
		
		if (followMode != null && dist != null) {
			FollowMode oldMode = this.followMode;
			PVector oldDist = getDist();
			this.followMode = FollowMode.fromString(followMode);
			setDist(vec(dist));
			if (!oldDist.equals(getDist()) && toFollow == null)
				System.out.println("camera dist in "+followMode+" mode set at "+dist);
			this.followMode = oldMode;
		}
		
		updateAbs();
	}

	public void setSkybox(boolean displaySkybox) {
		this.displaySkybox = displaySkybox;
	}
	
	public void nextMode() {
		if (toFollow != null) {
			followMode = followMode.next();
			updateAbs();
			displayState();
		} else
			System.out.println("camera need an object to focus on.");
	}
	
	public void place() {
		// 1.update
		if (toFollow != null && toFollow.transformChanged) {
			updateAbs();
		}
		
		for (Collider c : game.physic.colliders)
			if (ProMaster.distSq(focusPoint(), c.location) > distSqBeforeRemove)
				game.physic.toRemove.add( c );
		
		// 2.draw
		app.camera(position.x, position.y, position.z, 
				focus.x, focus.y, focus.z, 
				orientation.x, orientation.y, orientation.z);
		
		if (displaySkybox) {
			app.pushMatrix();
			if (toFollow != null)
				translate( toFollow.location  );
			app.shape(skybox);
			app.popMatrix();
		} else {
			app.background(200);
			app.ambientLight(255, 255, 255);
			app.directionalLight(50, 100, 125, 0, -1, 0);
		}
		
		if (pointCentral) {
			app.fill(255, 255, 255, 255);
			app.point(app.width/2, app.height/2);
		}
		
		if (drawAxis)
			drawAxis();
		
		//drawMouseray(50);
	}
	
	public void displayState() {
		if (followMode == FollowMode.Not)
			System.out.println("camera fixed at "+getDist());
		else
			System.out.println("camera following "+toFollow.toString()+" at "+toFollow.location+" from "+getDist()+" in "+followMode+" mode.");
	}
	
	public void drawMouseray(float dist) {
		float focal = 10;
	    PVector mrel = new PVector(-(app.mouseX-app.width/2)/focal, -(app.mouseY-app.height/2)/focal, -focal);
	    
	    app.fill(0,0,0);
	    app.sphere(5);
	    //this finds the position of the mouse in model space
	    PVector mousePos = absolute(mrel, position, identity);

	    PVector camToMouse=PVector.sub(mousePos, position);

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
	    System.out.println("cam pos: "+position);
	    System.out.println("mouse pos: "+mousePos);
	    System.out.println("cam to mouse: "+camToMouse);
	    System.out.println("cam to focus: "+PVector.sub(position, focus));
	}

	private void updateAbs() {
		orientation = orientation();
		position = position();
		focus = focusPoint();
	}

	private PVector position() {
		switch(followMode) {
		case Static:
			return PVector.add( distStatic, focusPoint() );
		case Relative:
			return toFollow.absolute(distRel);
		default:
			return distNot;
		}
	}
	
	private PVector orientation() {
		if (followMode == FollowMode.Relative)
			return absolute(Body.down, zero, toFollow.rotation);
		else
			return defaultOrientation;
	}
	
	// le point que regarde la camera
	private PVector focusPoint() {
		if (toFollow == null)
			return zero;
		else {
			switch(followMode) {
			case Static:
				return toFollow.location;
			case Relative:
				return PVector.add(toFollow.location, toFollow.absUp(60));
			default:
				return zero;
			}
		}
	}
	
	private void setDist(PVector dist) {
		switch(followMode) {
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
	
	private PVector getDist() {
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
	
	private void drawAxis() {
		app.stroke(255, 0, 0);
		app.line(0, 0, 0, far, 0, 0);
		app.stroke(0, 255, 0);
		app.line(0, 0, 0, 0, far, 0);
		app.stroke(0, 0, 255);
		app.line(0, 0, 0, 0, 0, far);
	}
}
