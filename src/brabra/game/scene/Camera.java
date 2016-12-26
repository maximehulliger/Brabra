package brabra.game.scene;

import brabra.Brabra;
import brabra.Debug;
import brabra.ProMaster;
import brabra.game.Color;
import brabra.game.physic.geo.ProTransform;
import brabra.game.physic.geo.Vector;
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
	private static final Color xColor = new Color("red", true), 
			yColor = new Color("green", true), 
			zColor = new Color("blue", true),
	pointCentralColor = new Color("red", true);

	private Object focused = null;
	private Mode mode = Mode.Relative;
	
	private final Vector orientation = defaultOrientation.copy();
	private final Vector distRelative = add(up(90), behind(135)), 
			distStatic = Vector.cube(300),
			distNone = Vector.cube(300);
	
	public enum Mode {
		Relative, Static, None;

		public Mode next() {
			return values()[(this.ordinal()+1) % values().length];
		}

		public static Mode fromString(String f) {
			if (f.equals("static"))
				return Mode.Static;
			else if (f.equals("relative"))
				return Mode.Relative;
			else if (f.equals("none"))
				return Mode.None;
			else {
				Debug.err("Camera mode unknown: \""+f+"\", taking relative");
				return Mode.Relative;
			}
		}
	}
			
	/** Creates a new camera. */
	public Camera() {
		setName("Camera");
	}

	// --- Setters ---
	
	public void setFocused(Object o) {
		focused = o;
		if (o == null)
			setMode(Mode.None);
	}
	
	public void changeMode() {
		setMode(mode.next());
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
		updatePosition();
	}
	
	private void updatePosition() {
		switch(mode) {
		case Static:
			position.set(distStatic);
			orientation.set(defaultOrientation);
			break;
		case Relative:
			position.set(distRelative);
			break;
		default: // None
			position.set(distNone);
			orientation.set(defaultOrientation);
			break;
		}
	}

	/** Set the camera relative dist for this mode. */
	public void setDist(Mode mode, Vector dist) {
		switch(mode) {
		case Static:
			distStatic.set(dist);
			break;
		case Relative:
			distRelative.set(dist);
			break;
		default: // None
			distNone.set(dist);
		}
		updatePosition();
	}
	
	// --- Main usage (draw) ---

	/** Put the camera in the processing scene and carry his job (see class doc). */
	public void place() {
		//set focus & pos (& orientation if needed)
		final Vector focus, pos;
		if (focused == null || mode == Mode.None) {
			focus = zero;
			pos = position;
		} else {
			focus = focused.position;
			switch (mode) {
			case Relative:
				pos = focused.absolute(position);
				orientation.set(focused.localFromRel(y(-1)));
				break;
			default: // Static
				pos = position.plus(focused.position);
				break;
			}
		}

		// Remove the objects too far away.
		game.scene.forEachObjects(o -> {
			if (ProMaster.distSq(focus, o.position) > distSqBeforeRemove)
				game.scene.remove(o);
		});

		// Draw all the stuff
		app.background(200);
		
		app.camera(pos.x, pos.y, pos.z, 
				focus.x, focus.y, focus.z, 
				orientation.x, orientation.y, orientation.z);
		
		if (app.para.displaySkybox()) {
			app.pushMatrix();
			ProTransform.translate(pos);
			app.shape(skybox());
			app.popMatrix();
		}

		if (app.para.displayAxis())
			displayAxis();
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
		
		setFocused(atts.parent());
		
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
		
		final String modeString = atts.getValue("mode");
		if (modeString == null)
			setMode(Mode.fromString(modeString));
		
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
