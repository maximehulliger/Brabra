package brabra.game.scene;

import java.util.Observable;
import java.util.function.Function;

import brabra.Master;
import brabra.ProMaster;
import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Transform;
import brabra.game.physic.geo.Vector;
import brabra.game.physic.geo.Transform.ParentRelationship;
import brabra.game.scene.SceneLoader.Attributes;
import brabra.gui.ToolWindow;

/** 
 * An object with transforms (location, rotation) 
 * and family (parent, children)
 * that is updated every frame (the children depends on the parent). 
 **/
public class Object extends ProMaster {
	
	public final Transform<Object> transform = new Transform<>(this);
	
	protected Scene scene = null;
	
	// > Flags, other
	private String name;

	/** Create a Body with this location & rotation. rotation can be null. */
	public Object(Vector location, Quaternion rotation) {
		setName(getClass().getSimpleName());
		
		// to notify model:
		transform.locationRel.addOnChange(() -> model.notifyChange(Change.Location));
		transform.rotationRel.addOnChange(() -> model.notifyChange(Change.Rotation));
	}
	
	/** Create a Body with this location & no initial rotation. */
	public Object(Vector location) {
		this(location, identity);
	}
	
	/** Set this to the other object. should be overridden to make the copy complete & called. */
	public void copy(Object other) {
		setName(other.name);
		transform.copy(other.transform);
	}
	
	// --- Methods to override if wanted (of course basically everything is ;) ) ---

	/** Override it to display the object. */
	public void display() {}
	
	/** 
	 * Override it to validate the object when added from an xml file (after parent & children set). 
	 * Return true when this was valdated (only once). 
	 **/
	public void validate(Attributes atts) {
		// parent first !
		final String parentRel = atts.getValue("parentRel");
		setParent(atts.parent(), parentRel != null ? ParentRelationship.fromString(parentRel) : null);

		// other attributes
		final String name = atts.getValue("name");
		if (name != null)
			setName(name);
		final String cameraMode = atts.getValue("camera");
		if (cameraMode != null)
			game.camera().set(this, cameraMode, atts.getValue("cameraDist"));
		// focus: here because we want to do that with the children set.
		final String focus = atts.getValue("focus");
		if (focus != null && Boolean.parseBoolean(focus)) {
			final String force = atts.getValue("force");
			game.physicInteraction.setFocused(this, force != null ? Float.parseFloat(force) : -1);
		}
	}
	
	/** To react when the object is removed from the scene. should be called. */
	public void onDelete() {
		transform.onDelete();
	}

	// --- Simple getters ---

	public boolean equals(java.lang.Object other) {
		return other == this;
	}
	
	/** To cast this object easily. return null if invalid. */
	public <T extends Object> T as(Class <T> as) {
		return Master.as(this, as);
	}
	
	// --- Physic getters ---

	/** Return the absolute location of the object. update things if needed. the Vector should not be modified. */
	public Vector location() {
		return transform.location();
	}

	/** Return the absolute rotation. update things if needed. the Quaternion should not be modified. */
	public Quaternion rotation() {
		return transform.rotation();
	}
	
	/** Return the absolute velocity at the center of mass. */
	public Vector velocity() {
		return zero;
	}

	/** Return the absolute velocity (from an absolute pos). */
	public Vector velocityAt(Vector posAbs) {
		return zero;
	}

	/** Return the absolute velocity (from a relative pos). */
	public Vector velocityAtRel(Vector posRel) {
		return zero;
	}
	
	// --- String getters ---

	public String toString() {
		return name;
	}
	
	/** return the presentation of the object with the name in evidence and the parent if exists. */
	public String presentation() {
		return "> "+this+" <" + (hasParent() ? " "+transform.parentRel()+" after \""+transform.parent()+"\"" : "");
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
	
	// --- Family ---

	// --- Family getters ---

	/** Return true if the object should consider his parent. */
	public boolean hasParent() {
		return transform.hasParent();
	}

	/** Return the parent of the object (even if parentRel is None -> can be null). */
	public Object parent() {
		return transform.parent().object;
	}

	/** Return true if this has a parentRel link with other */
	public boolean isRelated(Object other) {
		return transform.isRelated(other.transform);
	}
	
	/** Return the fist child (dfs) that satisfy the predicate, */
	public Object childThat(Function<Object, Boolean> predicate) {
		return transform.childThat(predicate);
	}

	/** Return the fist parent that satisfy the predicate, */
	public Object parentThat(Function<Object, Boolean> predicate) {
		return transform.parentThat(predicate);
	}
	
	/** 
	 * Set the new parent object of this object. This will now follow the parent and 
	 * apply this' and parent's loc and rot (depending on parentRel) to get in local space. 
	 * If newParent is null, parentRel should be None or null, otherwise set parentRel to Full if it was None. 
	 * Set the transform/push relationship from this with his parent (no parent -> root).
	 * Return true if the parent changed.
	 **/
	public boolean setParent(Object newParent, ParentRelationship newParentRel) {
		return transform.setParent(newParent == null ? null : newParent.transform, newParentRel);
	}

	// --- Update stuff (+transformChanged) ---

	/** 
	 * 	Update the object and his children.
	 * 	Called every frame with the updated flag. Update parent first.
	 * 	Return true if the object was updated or false if it already was for this frame.
	 **/
	protected void update() {
		if (scene == null)
			throw new IllegalArgumentException("The object \""+toString()+"\" should be added to the scene before updating it.");
		
		// update it
		transform.update();

		// update the children
		transform.children.forEach(t -> t.object.update());
	}
	
	/** Flag used to avoid too much*/
	//protected boolean updatingAbs = false;
	
	/** 
	 * Update the local absolute variables if needed. check the parent (of course ^^). 
	 * Compute them from relative location & rotation. 
	 * Call updateAbs() on children with updated set to true.
	 * Should be called at first by child class.
	 * Return true if something was updated.
	 **/
	protected void updateAbs() {
		
	}
	
	// --- Observation ---
	
	public enum Change {
		Location, Rotation, Velocity, RotVelocity, DisplayCollider, Mass, Name, Size
	}
	
	public final ObjectModel model = new ObjectModel();
	
	/** To let someone watch this object */
	public final static class ObjectModel extends Observable {
		public void notifyChange(Change change) {
			ToolWindow.runLater(() -> {
				synchronized (this) {
					setChanged();
					notifyObservers(change);
				}
			});
		}
	}

	// --- push & pop local ---
	
	/** 
	 * push local to the object depending on the parent relationship. 
	 * update the abs variables if needed (matrix & locationAbs). 
	 **/
	protected void pushLocal() {
		transform.pushLocal();
	}
	
	protected void popLocal() {
		transform.popLocal();
	}
	
	// --- syntactic sugar for space change ---

	protected Vector[] absolute(Vector[] rels) {
		Vector[] ret = new Vector[rels.length];
		for (int i=0; i<rels.length; i++)
			ret[i] = transform.absolute(rels[i]);
		return ret;
	}
	
	protected Line[] absolute(Line[] rels) {
		Line[] ret = new Line[rels.length];
		for (int i=0; i<rels.length; i++)
			ret[i] = rels[i].absoluteFrom(this);
		return ret;
	}
}
