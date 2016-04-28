package brabra.game.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import brabra.ProMaster;
import brabra.game.Observable.NQuaternion;
import brabra.game.Observable.NVector;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import processing.core.PMatrix;

/** Represent the transformation of this transform's object. */
public class Transform extends ProMaster {
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
	protected final List<Transform> children = new ArrayList<>();
	/** Indicate if the children changed. */
	private boolean childrenChanged = false, childrenChangedCurrent = false;
	
	public final Object object;
	
	public Transform(Object master) {
		locationRel.addOnChange(() -> unvalidateAbs());
		rotationRel.addOnChange(() -> unvalidateAbs());
		this.object = master;
	}

	private void unvalidateAbs() {
		absValid = false;
		children.forEach(t -> t.unvalidateAbs());
	}
	
	public void copy(Transform other) {
		locationRel.set(other.locationRel);
		locationRel.set(other.locationRel);
		if (other.absValid) {
			locationAbs.set(other.locationAbs);
			locationAbs.set(other.locationAbs);
			absValid = true;
			matrixAbs = other.matrixAbs;
		}
	}
	
	public void set(Vector location, Quaternion rotation) {
		locationRel.set(location);
		if (rotation != null)
			rotationRel.set(rotation);
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

	/** Move this object with an absolute depl. */
	public void move(Vector deplAbs) {
		locationRel.add(localDir(deplAbs));
		locationAbs.add(deplAbs);
	}
	
	public void moveRel(Vector deplLoc) {
		locationRel.add(deplLoc);
		locationRel.add(absoluteDirFromLocal(deplLoc));
	}

	public void rotate(Quaternion rotAbs) {
		rotationRel.rotate(rotAbs);
		rotationAbs.rotate(rotAbs);
	}

	// --- State getters ---

	/** Return if the transforms of the object or one of his parent changed during last frame. */
	public boolean transformChanged() { 
		return locationRel.hasChanged() || rotationRel.hasChanged() || (hasParent() && parent.transformChanged()) ;
	}
	
	/** Return true if the children list was changed during last frame. */
	public boolean childrenChanged() {
		return childrenChanged;
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
		public static ParentRelationship fromString(String f) {
			if (f!=null && f.equals("static"))
				return Static;
			else if (f!=null && f.equals("none"))
				return None;
			else 
				return Full;
		}
	}

	// --- Family getters ---

	/** Return true if the object should consider his parent. */
	public boolean hasParent() {
		return parent != null && parentRel != ParentRelationship.None;
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
	
	/** Return the fist child (dfs) that satisfy the predicate, */
	public Object childThat(Function<Object, Boolean> predicate) {
		for (Transform t : children) {
			final Object child = t.object;
			if (predicate.apply(child))
				return child;
			Object forChild = child.childThat(predicate);
			if (forChild != null)
				return forChild;
		}
		return null;
	}

	/** Return the fist parent that satisfy the predicate, */
	public Object parentThat(Function<Object, Boolean> predicate) {
		return hasParent()
				? (predicate.apply(parent().object) ? parent().object : parent().parentThat(predicate))
				: null;
	}
	
	// --- family modifiers ---

	/** 
	 * Set the new parent object of this object. This will now follow the parent and 
	 * apply this' and parent's loc and rot (depending on parentRel) to get in local space. 
	 * If newParent is null, parentRel should be None or null, otherwise set parentRel to Full if it was None. 
	 * Set the transform/push relationship from this with his parent (no parent -> root).
	 * Return true if the parent changed.
	 **/
	public boolean setParent(Transform newParent, ParentRelationship newParentRel) {
		if (newParent == null || newParentRel == null) {
			newParentRel = ParentRelationship.None;
			newParent = null;
		}
		
		final boolean parentChanged = newParent != this.parent;
		final boolean parentRelChanged = newParentRel != this.parentRel;
		if (parentChanged || parentRelChanged) {
			// check input
			if (newParent == this)
				game.debug.err("Un objet ne sera pas son propre parent !");
			if (newParent != null && newParent.isChildOf(this))
				game.debug.err(toString()+": new parent "+newParent+" already related ! (plz no childhood vicious cylce)");
			else {
				// remove this child from old parent
				if (hasParent())
					parent.children.remove(this);
				
				// update & changes
				this.parentRel = newParentRel;
				this.parent = newParent;
				
				// add this child to new parent
				if (hasParent())
					parent.addChild(this);
				
				// always valid
				updateAbs();
			}
			return true;
		} else
			return false;
	}

	/** parent should be set before. */
	private void addChild(Transform newChild) {
		if (!children.contains(newChild)) {
			children.add(newChild);
			childrenChangedCurrent = true;
		}
		assert(newChild.parent == this);
	}
	

	/** set parent to null. */
	private void removeChild(Transform oldChild) {
		if (children.remove(oldChild)) {
			assert(oldChild.parent == this);
			childrenChangedCurrent = true;
			oldChild.setParent(null, null);
		}
	}
	
	// --- life cycle ---

	/** 
	 * 	Update the state of the transform. 
	 **/
	protected void update() {
			// update changes
			locationRel.update();
			rotationRel.update();
			locationAbs.update();
			rotationAbs.update();
			
			childrenChanged = childrenChangedCurrent;
			childrenChangedCurrent = false;
	}

	/** 
	 * Update the local absolute variables if needed. check the parent (of course ^^). 
	 * Compute them from relative location & rotation. 
	 * Call updateAbs() on children with updated set to true.
	 * Should be called at first by child class.
	 * Return true if something was updated.
	 **/
	protected void updateAbs() {
		if (!absValid) {
			app.pushMatrix();
			app.resetMatrix(); //we're working clean here !
			
			// depending on parent relation
			switch(parentRel) {
			case Full:
				parent.pushLocal();
				translate(locationRel);
				rotateBy(rotationRel);
				locationAbs.set(model(zero));
				rotationAbs.set( parent.rotation().rotatedBy(rotationRel) );
				matrixAbs = app.getMatrix();
				parent.popLocal();
				break;
			case Static:
				locationAbs.set( add(parent.location(), locationRel) );
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
	protected void pushLocal() {
		//app.pushMatrix();
		app.applyMatrix(matrixAbs);
	}
	
	protected void popLocal() {
		//app.popMatrix();
	}

	// --- conversion position local <-> *absolute* <-> relative ---
	
	/** Retourne la position de rel, un point relatif au body en absolu. */
	public Vector absolute(Vector rel) {
		Vector inParentSpace = absolute(rel, locationRel, rotationRel);
		return hasParent() ? parent.absolute(inParentSpace) : inParentSpace;
	}

	public Vector relative(Vector posAbs) {
		return relative(hasParent() ? parent.relative(posAbs) : posAbs, locationRel, rotationRel);
	}

	public Vector relativeFromLocal(Vector posLoc) {
		return relative(hasParent() ? parent.relative(posLoc) : posLoc, zero, rotationRel);
	}

	public Vector local(Vector posAbs) {
		return posAbs.minus(location());
	}

	public Vector localFromRel(Vector posRel) {
		return absolute(posRel, zero, rotationRel);
	}
	
	/** Retourne la position absolue de posLoc, un point local au body. */
	public Vector absoluteFromLocal(Vector posLoc) {
		return add( location(), posLoc );
	}
	
	// --- direction conversion: local <-> *absolute* <-> relative ---
	
	/** Return the absolute direction from a relative direction in the body space. result is only rotated -> same norm. */
	public Vector absoluteDir(Vector dirRel) {
		Vector inParentSpace = absolute(dirRel, zero, rotationRel);
		return hasParent() ? parent.absoluteDir(inParentSpace) : inParentSpace;
	}

	/** Return the absolute direction from a relative direction in the body space. result is only rotated -> same norm. */
	public Vector absoluteDirFromLocal(Vector dirLoc) {
		return hasParent() ? parent().absoluteDir(dirLoc) : dirLoc;
	}

	/** Return the relative direction in the body body space from an absolute direction. result is only rotated -> same norm. */
	public Vector relativeDir(Vector dirAbs) {
		return hasParent() 
				? relative(parent.relativeDir(dirAbs), zero, rotationRel) 
				: relative(dirAbs, zero, rotationRel);
	}

	/** Return the local direction in the object space from an absolute direction. result is only rotated -> same norm. */
	public Vector localDir(Vector dirAbs) {
		return hasParent() ? relative(parent.localDir(dirAbs), zero, parent.rotationRel) : dirAbs;
	}
	
	/** Return the local direction in the object space regardless of this' direction. result is only rotated -> same norm. */
	public Vector localDirFromRel(Vector dirRel) {
		return absolute(localDir(dirRel), zero, rotationRel);
	}
	
}