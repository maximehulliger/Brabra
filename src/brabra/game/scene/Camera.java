package brabra.game.scene;

import brabra.ProMaster;

import brabra.Brabra;
import brabra.Debug;
import brabra.game.Color;
import brabra.game.physic.geo.Vector;
import brabra.game.physic.geo.ProTransform;
import brabra.game.scene.SceneLoader.Attributes;
import processing.core.PShape;

/** 
 * Class dealing with the camera, background and light (+removal of far objects). 
 * Default mode is None (see FollowMode for more).
 * If the camera is placed before objects update, 
 * the camera position will be 1 frame late from the objects in scene. 
 **/ 
public class Camera extends Object {

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

	// intern, mode related
	/** The absolute point that looks the camera. */
	private final Vector orientation = defaultOrientation.copy();

	/** Creates a new camera. */
	public Camera() {
		setName("Camera");
	}

	// --- Setters ---

	// TODO: --- Raycast from screen ---

	/*public void drawMouseray(float dist) { 
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

	// --- Main usage (draw) ---

	/** Put the camera in the processing scene and carry his job (see class doc). */
	public void place() {
		Debug.setCurrentWork("camera");
		final Vector focus = hasParent() ? parent().position : zero;

		// Remove the objects too far away.
		game.scene.forEachObjects(o -> {
			if (ProMaster.distSq(focus, o.position) > distSqBeforeRemove)
				game.scene.remove(o);
		});

		// Draw all the stuff
		app.background(200);
		Vector pos = hasParent() ? position.plus(parent().position) : position;
		app.camera(pos.x, pos.y, pos.z, 
				focus.x, focus.y, focus.z, 
				orientation.x, orientation.y, orientation.z);
		if (app.para.displaySkybox()) {
			app.pushMatrix();
			ProTransform.translate(position);
			app.shape(skybox());
			app.popMatrix();
		}

		if (app.para.displayAxis())
			displayAxis();

		//drawMouseray(50);
	}

	/** Display (maybe) a point at the center of the screen. */
	public void gui() {
		//TODO: not working
		if (app.para.displayCenterPoint()) {
			pointCentralColor.fill();
			app.point(Brabra.width/2, Brabra.height/2);
		}
	}

	// --- life cycle (validate + update) ---

	public void validate(Attributes atts) {
		super.validate(atts);
		final String distString = atts.getValue("dist");
		if (distString == null)
			Debug.err("for camera: dist (or pos) should be set. ignoring.");
		else {
			final Vector dist = vec(distString);
			if (dist.equals(zero)) {
				Debug.err("for camera: dist should not be zero. ignoring.");
			} else
				position.set(dist);
		}
			
		final String displaySkybox = atts.getValue("displaySkybox");
		if (displaySkybox != null)
			app.para.setDisplaySkybox(Boolean.parseBoolean(displaySkybox));
		final String displayCentralPoint = atts.getValue("displayCenterPoint");
		if (displayCentralPoint != null)
			app.para.setDisplayCenterPoint(Boolean.parseBoolean(displayCentralPoint));
		final String displayAxis = atts.getValue("displayAxis");
		if (displayAxis != null)
			app.para.setDisplayAxis(Boolean.parseBoolean(displayAxis));
	}

	public void onDelete() {
		super.onDelete();
	}

	// --- private ---

	private static PShape skybox() {
		if (skybox == null) {
			skybox = app.loadShape("skybox.obj");
			skybox.scale(far/90);
		}
		return skybox;
	}

	private void displayAxis() {
		line(zero, x(far), xColor);
		line(zero, y(far), yColor);
		line(zero, z(far), zColor);
	}

	@Override
	public void display() {
		
	}
}
