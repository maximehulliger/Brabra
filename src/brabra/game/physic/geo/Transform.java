package brabra.game.physic.geo;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.function.Function;

import brabra.Debug;
import brabra.game.Observable.NQuaternion;
import brabra.game.Observable.NVector;
import brabra.game.scene.Object;
import processing.core.PMatrix;

/** Represent the transformation of this transform's object. */
public class Transform extends ProTransform {
	// > core
	/** Position relative to the parent. */
	public final NVector locationRel = new NVector();
	/** Rotation relative to the parent. */
	public final NQuaternion rotationRel = new NQuaternion();
	
	// > absolute variable
	/** Absolute position. Equals locationRel if no parent. */
	private final NVector locationAbs = new NVector();
	/** Absolute rotation. Equals rotationRel if no parent. */
	private final NQuaternion rotationAbs = new NQuaternion();
	/** Matrix representing the matrix transformations till this object (lazy). */
	private PMatrix matrixAbs = null;
	/** for the matrix and positions. */
	private boolean absValid = false;
	
	// > Family
	private Transform parent = null;
	private ParentRelationship parentRel = ParentRelationship.None;
	public final List<Transform> children = new ArrayList<>();
	
	public Transform(Vector location, Quaternion rotation) {
		locationRel.set(location);
		rotationRel.set(rotation);
		locationRel.addOnChange(() -> unvalidateAbs());
		rotationRel.addOnChange(() -> unvalidateAbs());
	}

	public Transform(Vector location) {
		this(location, Quaternion.identity);
	}

	public Transform() {
		this(Vector.zero);
	}
	
	private void unvalidateAbs() {
		absValid = false;
		children.forEach(t -> t.unvalidateAbs());
	}
	
	/** Copy the other transform into this and return this. */
	public Transform copy(Transform other) {
		locationRel.set(other.locationRel);
		rotationRel.set(other.rotationRel);
		return this;
	}

	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    return (other instanceof Transform) 
	    		? equals((Transform)other) : false;
	}

	public boolean equals(Transform other) {
		boolean forRel = rotationRel.equals(other.rotationRel) && locationRel.equals(other.locationRel);
		boolean forAbs = rotation().equals(other.rotation()) && locationAbs.equals(other.locationAbs);
		return forRel && forAbs;
	}
	
	// --- Transform simple getters/modifier ---

	/** Return the absolute location of the object. update things if needed. the Vector should not be modified. */
	public Vector location() {
		updateAbs();
		return locationAbs;
	}
	
	/** Return the absolute rotation. update things if needed. the Quaternion should not be modified. */
	public Quaternion rotation() {
		updateAbs();
		return rotationAbs;
	}

	/** Move this object with an absolute movement. */
	public void move(Vector deplAbs) {
		moveRel(localDir(deplAbs));
	}
	
	/** Move this object with an absolute movement. */
	public void moveRel(Vector deplLoc) {
		locationRel.add(deplLoc);
	}

	/** Rotate this object with an absolute rotation (same as relative). */
	public void rotate(Quaternion rotAbs) {
		rotationRel.rotate(rotAbs);
	}

	// >>> Family <<<
	
	/** 
	 * ParentRelationship define some ways to get in local space (via pushLocal()). 
	 * Default is Full (if parent is set).<p>
	 * Full: this will follow the parent (with the parent loc & rot). <p>
	 * Relative: this will follow relatively the parent absolute loc (ignore parent final rotation). <p>
	 * None: ignore parent but keep the parent field.
	 **/
	public enum ParentRelationship {
		Full, Static, None;
		
		public ParentRelationship next() {
			return values()[(this.ordinal()+1) % values().length];
		}
		
		public static ParentRelationship fromString(String f) {
			if (f.equals("static"))
				return ParentRelationship.Static;
			else if (f.equals("full") || f.equals("relative"))
				return ParentRelationship.Full;
			else if (f.equals("fixed") || f.equals("none"))
				return ParentRelationship.None;
			else {
				Debug.err("Parent relationship unknown: \""+f+"\", taking None");
				return ParentRelationship.None;
			}
		}
	}

	// --- Family getters ---

	/** Return true if the object should consider his parent. */
	public boolean hasParent() {
		return parent != null;
	}

	/** Return the parent of the object (even if parentRel is None -> can be null). */
	public Transform parent() {
		return parent;
	}

	/** Return the parent of the object (even if parentRel is None -> can be null). */
	public ParentRelationship parentRel() {
		return parentRel;
	}
	
	/** Return true if this is a children of other. */
	public boolean isChildOf(Transform parent) {
		for (Transform p=parent(); p!=null; p=p.parent())
			if (p == parent) 
				return true;
		return false;
	}

	/** Return true if this has a parentRel link with other */
	public boolean isRelated(Transform other) {
		assert (other != this && other != null);
		// case #1: one parent of the other
		if (isChildOf(other) || other.isChildOf(this))
			return true;
		// case #2: common parent -> check for all parent of this if other is a child.
		for (Transform parent=parent(); parent!=null; parent=parent.parent()) {
			if (other.isChildOf(parent) || other.isChildOf(parent))
				return true;
		}
		return false;
	}
	
	/** Return the fist child (dfs) that satisfy the predicate or null. */
	public Transform childThat(Function<Transform, Boolean> predicate) {
		for (Transform child : children) {
			if (predicate.apply(child))
				return child;
			Transform forChild = child.childThat(predicate);
			if (forChild != null)
				return forChild;
		}
		return null;
	}

	/** Return the fist parent that satisfy the predicate, */
	public Transform parentThat(Function<Transform, Boolean> predicate) {
		return hasParent()
				? (predicate.apply(parent()) ? parent() : parent().parentThat(predicate))
				: null;
	}
	
	// --- family modifiers ---

	/**  
	 * Set the transform/push relationship from this with his parent (no parent -> root).
	 * This will now follow the parent and apply this' and parent's 
	 * location and rotation (depending on parentRel) to get in local space. 
	 * If newParent is null, parentRel should be None or null, 
	 * otherwise set parentRel to Full if it was null.
	 * Return true if the parent changed.
	 **/
	public void setParent(Transform newParent, ParentRelationship newParentRel) {
		if (newParent == null || newParentRel == ParentRelationship.None) {
			newParentRel = ParentRelationship.None;
			newParent = null;
		} else if (newParentRel == null)
			newParentRel = ParentRelationship.Full;
		
		final boolean parentChanged = newParent != this.parent;
		final boolean parentRelChanged = newParentRel != this.parentRel;
		if (parentChanged || parentRelChanged) {
			// check input
			if (newParent == this)
				Debug.err("Un objet ne sera pas son propre parent !");
			if (newParent != null && newParent.isChildOf(this))
				Debug.err(toString()+": new parent "+newParent+" already related ! (plz no childhood vicious cylce)");
			else {
				final Transform oldParent = parent;
				
				// update & changes
				this.parentRel = newParentRel;
				this.parent = newParent;

				// notify self & the children
				unvalidateAbs();

				// update children lists + notification
				if (parentChanged) {
					// remove this child from old parent
					if (oldParent != null) {
						oldParent.children.remove(this);
						oldParent.model.notifyChange(Change.Children);
					}
					// add this child to new parent
					if (parent != null) {
						parent.addChild(this);
						parent.model.notifyChange(Change.Children);
					}
				}
				model.notifyChange(Change.Parentship);
			}
		}
	}

	/** parent should be set before. */
	private void addChild(Transform newChild) {
		if (!children.contains(newChild)) {
			children.add(newChild);
		}
		assert(newChild.parent == this);
	}
	

	/** set parent to null. */
	private void removeChild(Transform oldChild) {
		if (children.remove(oldChild)) {
			assert(oldChild.parent == this);
			oldChild.setParent(null, null);
		}
	}
	
	// --- life cycle ---

	/** 
	 * 	Update the state of this transform only. 
	 **/
	public void update() {
		// update changes
		locationRel.update();
		rotationRel.update();
		locationAbs.update();
		rotationAbs.update();
		
		// update the children
		children.forEach(t -> t.update());
	}

	/** 
	 * Update the local absolute variables if needed. check the parent (of course ^^). 
	 * Compute them from relative location & rotation. 
	 * Call updateAbs() on children with updated set to true.
	 * Should be called at first by child class.
	 * Return true if something was updated.
	 **/
	private void updateAbs() {
		if (!absValid) {
			// check the parent
			if (hasParent() && !parent.absValid)
				parent.updateAbs();
			
			app.pushMatrix();
			app.resetMatrix(); //we're working clean here !
			
			// depending on parent relation
			switch(parentRel) {
			case Full:
				parent.pushLocal();
				translate(locationRel);
				rotateBy(rotationRel);
				locationAbs.set(model(Vector.zero));
				rotationAbs.set( parent.rotation().rotatedBy(rotationRel) );
				matrixAbs = app.getMatrix();
				parent.popLocal();
				break;
			case Static:
				locationAbs.set( parent.location().plus(locationRel) );
				rotationAbs.set(rotationRel);
				translate(locationAbs);
				rotateBy(rotationRel);
				matrixAbs = app.getMatrix();
				break;
			default: //None
				translate(locationRel);
				rotateBy(rotationRel);
				matrixAbs = app.getMatrix();
				locationAbs.set(locationRel);
				rotationAbs.set(rotationRel);
				break;
			}
			app.popMatrix();
			
			// caus' modification from inside -> no need of notif.
			locationAbs.reset();
			rotationAbs.reset();
			
			absValid = true;
		}
	}

	/** To react when the object is removed from the scene. should be called. */
	public void onDelete() {
		if (hasParent())
			parent.removeChild(this);
	}

	// --- push & pop local ---
	
	/** 
	 * push local to the object depending on the parent relationship. 
	 * update the abs variables if needed (matrix & locationAbs). 
	 **/
	public void pushLocal() {
		updateAbs();
		app.pushMatrix();
		app.applyMatrix(matrixAbs);
	}
	
	public void popLocal() {
		app.popMatrix();
	}

	// --- conversion position relative <-> *absolute* <-> local ---
	
	/** Retourne la position de rel, un point relatif au body en absolu. */
	public Vector absolute(Vector rel) {
		Vector inParentSpace = absolute(rel, locationRel, rotationRel);
		return hasParent() ? parent.absolute(inParentSpace) : inParentSpace;
	}

	public Vector relative(Vector posAbs) {
		return relative(hasParent() ? parent.relative(posAbs) : posAbs, locationRel, rotationRel);
	}

	public Vector relativeFromLocal(Vector posLoc) {
		return relative(hasParent() ? parent.relative(posLoc) : posLoc, Vector.zero, rotationRel);
	}

	public Vector localFromRel(Vector posRel) {
		return absolute(posRel, Vector.zero, rotationRel);
	}
	
	// --- direction conversion: local <-> *absolute* <-> relative ---
	
	/** Return the absolute direction from a relative direction in the body space. result is only rotated -> same norm. */
	public Vector absoluteDir(Vector dirRel) {
		Vector inParentSpace = absolute(dirRel, Vector.zero, rotationRel);
		return hasParent() ? parent.absoluteDir(inParentSpace) : inParentSpace;
	}

	/** Return the relative direction in the body body space from an absolute direction. result is only rotated -> same norm. */
	public Vector relativeDir(Vector dirAbs) {
		return hasParent() 
				? relative(parent.relativeDir(dirAbs), Vector.zero, rotationRel) 
				: relative(dirAbs, Vector.zero, rotationRel);
	}
	
	/** Return the absolute direction from a relative direction in the body space. result is only rotated -> same norm. */
	public Vector absoluteDirFromLocal(Vector dirLoc) {
		return hasParent() ? parent().absoluteDir(dirLoc) : dirLoc;
	}

	/** Return the local direction in the object space from an absolute direction. result is only rotated -> same norm. */
	public Vector localDir(Vector dirAbs) {
		return hasParent() ? relative(parent.localDir(dirAbs), Vector.zero, parent.rotationRel) : dirAbs;
	}
	
	/** Return the local direction in the object space regardless of this' direction. result is only rotated -> same norm. */
	public Vector localDirFromRel(Vector dirRel) {
		return absolute(localDir(dirRel), Vector.zero, rotationRel);
	}
	
	protected Vector[] absolute(Vector[] rels) {
		Vector[] ret = new Vector[rels.length];
		for (int i=0; i<rels.length; i++)
			ret[i] = absolute(rels[i]);
		return ret;
	}
	
	protected Line[] absolute(Line[] rels) {
		Line[] ret = new Line[rels.length];
		for (int i=0; i<rels.length; i++)
			ret[i] = rels[i].absoluteFrom(this);
		return ret;
	}

	// --- Observation ---
	
	public enum Change {
		Transform, Velocity, RotVelocity, DisplayCollider, 
		Mass, Name, Size, Parentship, Children
	}
	
	public final ObjectModel model = new ObjectModel();
	
	/** To let someone watch this object */
	public final class ObjectModel extends Observable {
		
		public void notifyChange(Change change) {
			synchronized (this) {
				setChanged();
				notifyObservers(change);
				children.forEach(c -> c.model.notifyChange(change));
			}
		}
	}
}
