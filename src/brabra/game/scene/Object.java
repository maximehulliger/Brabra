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
public abstract class Object extends Transform {
	
	// > Flags, other
	private String name;

	/** Create a Body with this location & rotation. rotation can be null. */
	public Object() {
		super();
		setName(getClass().getSimpleName());
		
		// to notify model:
		position.addOnChange(() -> model.notifyChange(Change.Transform));
		rotation.addOnChange(() -> model.notifyChange(Change.Transform));
	}

	/** Set this to the other object. should be overridden to make the copy complete & called. */
	public void copy(Object other) {
		super.copy(other);
		setName(other.name);
	}
	
	// --- Methods to override if wanted (of course basically everything is ;) ) ---

	/** Override it to display the object. */
	public abstract void display();
	
	/** 
	 * Override it to validate the object when added from an xml file (after parent & children set). 
	 * Return true when this was valdated (only once). 
	 **/
	public void validate(Attributes atts) {
		// get loc & dir
		final String locString = atts.getValue("pos");
		final String dirString = atts.getValue("dir");
		if (locString != null)
			position.set(vec(locString));
		if (dirString != null)
			rotation.set(Quaternion.fromDirection(vec(dirString), Vector.up));
		
		// other attributes
		final String name = atts.getValue("name");
		if (name != null)
			setName(name);
		final String cameraMode = atts.getValue("camera");
		if (cameraMode != null) {
			// set this as camera's parent with the given relation
			game.camera.setFocused(this);
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
	public void onDelete() {}

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
		return "> "+this+" <";
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
