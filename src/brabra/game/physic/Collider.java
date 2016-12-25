package brabra.game.physic;

import brabra.game.Color;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;

/** A class able to init and react to a collision. */
public abstract class Collider extends Body {
	
	public final static Color colliderColor = new Color(255, 0, 0, 150, 255, 0, 0);
	
	private boolean displayCollider = false;

	public void copy(Object o) {
		super.copy(o);
		Collider oc = this.as(Collider.class);
		if (oc != null) {
			displayCollider = oc.displayCollider;
		}
	}
	
	// --- Getters ---

	public boolean displayCollider() {
		return displayCollider || app.para.displayAllColliders();
	}
	
	// --- Setters ---
	
	public void setDisplayCollider(boolean displayCollider) {
		this.displayCollider = displayCollider;
		model.notifyChange(Change.DisplayCollider);
	}
	
	// --- Collider ---

	/** To display the shape of the collider (without color, in relative space). */
	public abstract void displayShape();
	
	/** 
	 * Display the collider... maybe. 
	 * In local space (should be call after pushLocal()). 
	 * Return true if the collider was displayed.
	 **/
	protected boolean displayColliderMaybe() {
		final boolean display = displayCollider();
		if (display) {
			colliderColor.fill();
			displayShape();
		}
		return display;
	}

	public void validate(Attributes atts) {
		super.validate(atts);
		
		final String displayCollider = atts.getValue("displayCollider");
		if (displayCollider != null)
			setDisplayCollider( Boolean.parseBoolean(displayCollider) );
	}
}
