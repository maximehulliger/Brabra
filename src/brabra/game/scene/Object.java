package brabra.game.scene;

import brabra.Master;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Transform;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.SceneLoader.Attributes;

/** 
 * An object with transforms (location, rotation) 
 * and family (parent, children)
 * that is updated every frame (the children depends on the parent). 
 **/
public class Object extends Transform {
	
	// > Flags, other
	private String name;

	/** Create a Body with this location & rotation. rotation can be null. */
	public Object(Vector location, Quaternion rotation) {
		super(location, rotation);
		setName(getClass().getSimpleName());
		
		// to notify model:
		locationRel.addOnChange(() -> model.notifyChange(Change.Transform));
		rotationRel.addOnChange(() -> model.notifyChange(Change.Transform));
	}

	/** Create a Body with this location & no initial rotation. */
	public Object(Vector location) {
		this(location, identity);
	}

	/** Create a Body with this location zero & no initial rotation. */
	public Object() {
		this(Vector.zero);
	}
	
	/** Set this to the other object. should be overridden to make the copy complete & called. */
	public void copy(Object other) {
		super.copy(other);
		setName(other.name);
	}
	
	// --- Methods to override if wanted (of course basically everything is ;) ) ---

	/** Override it to display the object. */
	public void display() {}
	
	/** 
	 * Override it to validate the object when added from an xml file (after parent & children set). 
	 * Return true when this was valdated (only once). 
	 **/
	public void validate(Attributes atts) {
		super.validate(atts);
		
		// get loc & dir
		final String locString = atts.getValue("pos");
		final String dirString = atts.getValue("dir");
		if (locString != null)
			locationRel.set(vec(locString));
		if (dirString != null)
			rotationRel.set(Quaternion.fromDirection(vec(dirString), Vector.up));
		
		// set parent first !
		final String parentRel = atts.getValue("parentRel");
		setParent(atts.parent(), parentRel != null ? ParentRelationship.fromString(parentRel) : null);
		
		// other attributes
		final String name = atts.getValue("name");
		if (name != null)
			setName(name);
		final String cameraMode = atts.getValue("camera");
		if (cameraMode != null) {
			Camera cam = game.camera();
			// set this as camera's parent with the given relation
			ParentRelationship rel = ParentRelationship.fromString(cameraMode);
			cam.setParent(this, rel);
			// set the distance for this mode
			String dist = atts.getValue("cameraDist");
			if (dist != null)
				cam.setDist(rel, vec(dist));
		}
		// focus: here because we want to do that with the children set.
		final String focus = atts.getValue("focus");
		if (focus != null && Boolean.parseBoolean(focus)) {
			game.physicInteraction.setFocused(this);
			final String force = atts.getValue("force");
			if (force != null)
				game.physicInteraction.setForce(Float.parseFloat(force));
		}
	}
	
	/** To react when the object is removed from the scene. should be called. */
	public void onDelete() {
		super.onDelete();
	}

	// --- Simple getters ---

	public boolean equals(java.lang.Object other) {
		return other == this;
	}
	
	/** To cast this object easily. return null if invalid. */
	public <T extends Object> T as(Class <T> as) {
		return Master.as(this, as);
	}
	
	// --- String getters ---

	public String toString() {
		return name;
	}
	
	/** return the presentation of the object with the name in evidence and the parent if exists. */
	public String presentation() {
		return "> "+this+" <" + (hasParent() ? " "+parentRel()+" after \""+parent()+"\"" : "");
	}
	
	// --- Setters ---
	
	public void setName(String name) {
		this.name = name;
		model.notifyChange(Change.Name);
	}
	
	/** Set the name and return the object (for "new Object().withName("pope")"). */
	public Object withName(String name) {
		setName(name);
		return this;
	}
}
