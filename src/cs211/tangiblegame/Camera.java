package cs211.tangiblegame;

import processing.core.PShape;
import processing.core.PVector;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.ProMaster;

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
			else if (f.equals("not"))
				return FollowMode.Not;
			else {
				System.out.println("FollowMode pour camera inconu :"+f);
				return FollowMode.Not;
			}
		}
	}
	
	public static PShape skybox;
	
	public boolean displaySkybox = false;
	public boolean pointCentral = true;
	public boolean drawAxis = true;
	
	public FollowMode followMode = FollowMode.Not;
	public Body toFollow = new Body(zero, Quaternion.identity);

	public PVector distNot = vec(50,50,50);
	public PVector distStatic = vec(300,300,300);
	public PVector distRel = PVector.mult(vec(0, 6, 9), 15f);
	
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
	
	public void setSkybox(Boolean displaySkybox) {
		this.displaySkybox = displaySkybox;
		if (displaySkybox)
			System.out.println("avec skybox");
		else
			System.out.println("sans skybox");
	}
	
	public void set(String mode, String dist, Body toFollow) {
		if (mode != null) 
		  	followMode = FollowMode.fromString(mode);
		if (toFollow == null)
			followMode = FollowMode.Not;
	  	if (dist != null)
		  	setDist(vec(dist));
	  	
	  	if (app.intRealGame.camera.followMode == FollowMode.Not)
			System.out.println("camera fixed at "+getDist());
		else
			System.out.println("camera now following "+toFollow.toString()+" at "+getDist()+" in "+mode+" mode.");
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
			translate( toFollow.location  );
			app.shape(skybox); 
			System.out.println("skybox :D");
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
	
	private void drawAxis() {
		float far = 10000;
		app.stroke(255, 0, 0);
		app.line(0, 0, 0, 0, far, 0);
		app.stroke(0, 0, 255);
		app.line(0, 0, 0, far, 0, 0);
		app.line(0, 0, 0, 0, 0, far);
	}
	
	public void nextMode() {
		followMode = followMode.next();
		System.out.println("now in "+followMode+" mode at "+getDist());
	}
}
