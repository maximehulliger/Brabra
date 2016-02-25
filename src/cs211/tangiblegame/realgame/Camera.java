package cs211.tangiblegame.realgame;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.TangibleGame;

//gère la camera, le background et la lumière. point central en option :)
public class Camera extends ProMaster {
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
			else if (f.equals("fixed"))
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
		if (followMode != null && dist != null)
			updateModeDist(FollowMode.fromString(followMode), vec(dist), toFollow == null);
				
		
	}
	
	private void updateModeDist(FollowMode mode, PVector dist, boolean blabla) {
		FollowMode oldMode = this.followMode;
		PVector oldDist = getDist();
		this.followMode = mode;
		setDist(dist);
		if (!oldDist.equals(getDist()) && blabla)
			System.out.println("camera dist in "+mode+" mode set at "+dist);
		this.followMode = oldMode;
	}

	public void setSkybox(boolean displaySkybox) {
		this.displaySkybox = displaySkybox;
	}
	
	public void nextMode() {
		followMode = followMode.next();
		displayState();
	}
	
	public void place() {
		switch(followMode) {
		case Not:
			app.camera(distNot.x, distNot.y, distNot.z, 0, 0, 0, 0, -1, 0);
			break;
		case Static:
			PVector posVue = toFollow.location.get();
			PVector posCam = PVector.add( distStatic, posVue );
			app.camera(posCam.x, posCam.y, posCam.z, posVue.x, posVue.y, posVue.z, 0, -1, 0);
			break;
		case Relative:
			PVector camPos = toFollow.absolute(distRel);
			PVector or = Body.absolute(Body.up, zero, toFollow.rotation);
			PVector focus = PVector.add(toFollow.location, toFollow.absUp(60));
			app.camera(camPos.x, camPos.y, camPos.z, focus.x, focus.y, focus.z, -or.x, -or.y, -or.z);
			break;
		}
		
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
	}
	
	public void gui() {
		app.camera();
		app.hint(PApplet.DISABLE_DEPTH_TEST);
		if (app.intRealGame.starship != null)
			app.intRealGame.starship.armement.displayGui();
		if (TangibleGame.imgAnalysis)
			app.imgAnalyser.displayCtrImg();
		app.hint(PApplet.ENABLE_DEPTH_TEST);
	}
	
	public void displayState() {
		if (followMode == FollowMode.Not)
			System.out.println("camera fixed at "+getDist());
		else
			System.out.println("camera following "+toFollow.toString()+" at "+toFollow.location+" from "+getDist()+" in "+followMode+" mode.");
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
		app.line(0, 0, 0, 0, far, 0);
		app.stroke(0, 0, 255);
		app.line(0, 0, 0, far, 0, 0);
		app.line(0, 0, 0, 0, 0, far);
	}
}
