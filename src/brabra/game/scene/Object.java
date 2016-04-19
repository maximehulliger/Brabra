package brabra.game.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;

import brabra.ProMaster;
import brabra.Debug.Debugable;
import brabra.game.Observable.NQuaternion;
import brabra.game.Observable.NVector;
import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Quaternion;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import brabra.game.physic.geo.Vector;
import brabra.gui.ToolWindow;

/** 
 * A movable object with transforms (location, rotation) 
 * and family (parent, children). <p>
 * Gives methods to change a location between different space: <p>
 * - Absolute (abs): relative & local to the root. 
 * - Relative (rel): in this' space (relative to this). 
 * - Local (loc): in this space without this' rotation (with the parents' transforms).
 **/
public class Object extends ProMaster implements Debugable {
	/** 
	 * ParentRelationship define some ways to get in local space (via pushLocal()). default is Full (if parent).<p>
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
	
	// > core
	/** Position relative to the parent. */
	protected final NVector locationRel = new NVector(zero);
	/** Rotation relative to the parent. */
	protected final NQuaternion rotationRel = new NQuaternion(identity);
	
	// > absolute variable
	/** Absolute position. Equals locationRel if no parent. */
	protected final NVector locationAbs = new NVector(zero);
	/** Absolute rotation. Equals rotationRel if no parent. */
	protected final NQuaternion rotationAbs = new NQuaternion(identity);
	/** Matrix representing the matrix transformations till this object (lazy). */
	private PMatrix matrix = null;
	/** for the matrix and positions. */
	private boolean absValid = false;
	/** Indicate the modification of the body transform during the last frame. */
	
	// > Family managment
	private Object parent = null;
	private ParentRelationship parentRel = ParentRelationship.None;
	private final List<Object> children = new ArrayList<>();
	
	/** Flag used during the main update loop. */
	protected boolean updated = false;
	protected Scene scene = null;
	
	// > Flags, other
	private String name;
	private boolean transformChanged = false, locationChanged = false, rotationChanged = false;
	/** Indicate if the children changed. */
	private boolean childrenChanged = false, childrenChangedCurrent = false;
	/** Flag indicating if the object was ever validated via validate(Atts). */
	private boolean validated = false;
	
	/** Create a Body with this location & rotation. rotation can be null. */
	public Object(Vector location, Quaternion rotation) {
		locationRel.setOnChange(() -> {absValid=false;});
		rotationRel.setOnChange(() -> {absValid=false;});
		//locationAbs.setOnChange(() -> {});
		// we only set rel variables cause !absValid.
		locationRel.set(location);
		if (rotation != null) {
			rotationRel.set(rotation);
		}
		setName(getClass().getSimpleName());
	}
	
	/** Create a Body with this location & no initial rotation. */
	public Object(Vector location) {
		this(location, identity);
	}
	
	// --- Methods to override if wanted (of course basically everything is ;) ) ---

	/** Override it to display the object. */
	public void display() {}
	
	/** 
	 * Override it to validate the object when added from an xml file (after parent & children set). 
	 * Return true when this was valdated (only once). 
	 **/
	public boolean validate(Attributes atts) {
		if (!validated) {
			// parent first !
			setParent(atts.parent());
			final String parentRel = atts.getValue("parentRel");
			if (parentRel != null)
				setParentRel(ParentRelationship.fromString(parentRel));
			// other attributes
			final String name = atts.getValue("name");
			if (name != null)
				setName(name);
			final String cameraMode = atts.getValue("camera");
			if (cameraMode != null)
				game.camera.set(this, cameraMode, atts.getValue("cameraDist"));
			// focus: here because we want to do that with the children set.
			final String focus = atts.getValue("focus");
			if (focus != null && Boolean.parseBoolean(focus)) {
				final String force = atts.getValue("force");
				game.physicInteraction.setFocused(this, force != null ? Float.parseFloat(force) : -1);
			}
			final String debug = atts.getValue("debug");
		  	if (debug != null && Boolean.parseBoolean(debug)) {
		  		assert(!game.debug.followed.contains(this));
		  		game.debug.followed.add(this);
		  	}
		  	return validated = true;
		} else
			return false;
	}
	
	/** To react when the object is removed from the scene. should be called. */
	public void onDelete() {
		if (hasParent())
			parent.removeChild(this);
	}

	// --- Simple getters ---

	public boolean equals(java.lang.Object other) {
		return other == this;
	}

	public String toString() {
		return name;
	}
	
	public Object copy() {
		Object a = new Object(locationAbs);
		return a;
	}
	
	/** Return true if the object should consider his parent. */
	public boolean hasParent() {
		return parent != null && parentRel != ParentRelationship.None;
	}

	/** Return the parent of the object (even if parentRel is None -> can be null). */
	public Object parent() {
		return parent;
	}
	
	@SuppressWarnings("unchecked")
	/** To cast this object easily. return null if invalid. */
	public <T extends Object> T as(Class <T> as) {
		return as.isInstance(this) ? (T)this : null;
	}

	/** Return the absolute location of the object. update things if needed. */
	public Vector location() {
		updateAbs();
		return locationAbs;
	}
	
	/** Return the absolute location of the object. update things if needed. */
	public Vector locationRel() {
		updateAbs();
		return locationRel;
	}
	
	/** for now return the relative rotation. */
	public Quaternion rotation() {
		updateAbs();
		return rotationAbs;
	}
	
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

	public boolean stateChanged() {
		return transformChanged;
	}

	/** Return true if this object was validated once in his life. */
	public boolean validated() {
		return validated;
	}

	/** Return if the transforms of the object or one of his parent changed during last frame. */
	public boolean transformChanged() { 
		return transformChanged;
	}

	/** Return if the transforms of the object or one of his parent changed during last frame. */
	public boolean locationChanged() { 
		return locationChanged;
	}

	/** Return if the transforms of the object or one of his parent changed during last frame. */
	public boolean rotationChanged() { 
		return rotationChanged;
	}

	/** Return true if the children list was changed during last frame. */
	public boolean childrenChanged() {
		return childrenChanged;
	}

	/** Return true if the absolute variables are valid (check the parent). */
	protected boolean absValid() {
		return (hasParent() && !parent.absValid()) ? false : absValid;
	}

	/** To display the state of the object in the console. */
	public void displayState() {
		debug.info(2, presentation()+" "+state(false));
	}
	
	/** return the presentation of the object with the name in evidence and the parent if exists. */
	public String presentation() {
		return "> "+this+" <" + (hasParent() ? " "+parentRel+" after \""+parent+"\"" : "");
	}

	public String getStateUpdate() {
		if (stateChanged()) {
			final String moveType = (locationAbs.hasChanged() ? "abs" : "")
					+ ((locationAbs.hasChanged() && locationRel.hasChanged()) ? " + " : "")
					+ (locationRel.hasChanged() ? "rel" : "");
			final String transType = (rotationChanged ? "rot" : "")
					+ ((rotationChanged && locationChanged) ? " + "+moveType : moveType);
			final String changeName = "transforms("+transType+") changed";
			final String changeStr = state(true);
					
			return presentation() + " " + changeName + " ---" + changeStr;
		} else
			return "";
	}
	
	/** if onlyChange is false, return all the object's transforms for the console. */
	protected String state(boolean onlyChange) {
		boolean loc, rot;
		if (onlyChange) {
			loc = locationChanged;
			rot = rotationChanged;
		} else
			loc = rot = false;
		
		return (loc ? "\nlocation: "+locationAbs : "")
				+ (!hasParent() ? "" : "\nlocationRel: "+locationRel)
				+ (rot ? "\nrotation rel: "+rotationRel : "");
	}
	
	// --- Other getters ---
	
	/** Return true if this is a children of other. */
	public boolean isChildOf(Object parent) {
		for (Object p=parent(); p!=null; p=p.parent())
			if (p == parent) 
				return true;
		return false;
	}

	/** Return true if this has a parentRel link with other */
	public boolean isRelated(Object other) {
		assert (other != this && other != null);
		// case #1: one parent of the other
		if (isChildOf(other) || other.isChildOf(this))
			return true;
		// case #2: common parent -> check for all parent of this if other is a child.
		for (Object parent=parent(); parent!=null; parent=parent.parent()) {
			if (other.isChildOf(parent) || other.isChildOf(parent))
				return true;
		}
		return false;
	}
	
	/** Return the fist child (dfs) that satisfy the predicate, */
	public Object childThat(Function<Object, Boolean> predicate) {
		for (Object child : children) {
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
				? (predicate.apply(parent()) ? parent() : parent().parentThat(predicate))
				: null;
	}

	// --- Setters ---
	
	public void setName(String name) {
		this.name = name;
	}
	
	/** Set the name and return the object (for "new Object().withName("pope")"). */
	public Object withName(String name) {
		setName(name);
		return this;
	}

	/** 
	 * Set the transform/push relationship from this with his parent (parent null -> root).
	 * see ParentRelationship for more. does nothing if rel is null.
	 **/
	public void setParentRel(ParentRelationship rel) {
		assert (rel != null);
		if (rel != parentRel) {
			assert(rel == ParentRelationship.None || parent != null);
			final boolean hadParent = hasParent();
			parentRel = rel;
			final boolean hasParent = hasParent();
			if (hadParent && !hasParent)
				parent.children.remove(this);
			else if (hasParent && ! hadParent)
				parent.addChild(this);
			absValid = false;
		}
	}
	
	/** 
	 * Set the new parent object of this object. This will now follow the parent and 
	 * apply this' and parent's loc and rot (depending on parentRel) to get in local space. 
	 * Set parentRel to Full if it was None. Parent can be null (then parentRel -> None)
	 * Return true if the parent changed.
	 **/
	public boolean setParent(Object newParent) {
		if (newParent != parent) {
			if (newParent == this)
				throw new IllegalArgumentException("Un objet ne sera pas son propre parent !");
			if (newParent != null && isRelated(newParent))
				throw new IllegalArgumentException(presentation()+": new parent "+newParent+" already related !");
			// remove this child from old parent
			if (hasParent())
				parent.removeChild(this);
			parent = newParent;
			// update parentRel
			if (newParent == null)
				setParentRel(ParentRelationship.None);
			else if (parentRel == ParentRelationship.None)
				setParentRel(ParentRelationship.Full);
			// add this child to new parent
			if (hasParent())
				parent.addChild(this);
			return true;
		} else
			return false;
	}
	
	/** parent should be set before. */
	private void addChild(Object newChild) {
		if (!children.contains(newChild)) {
			children.add(newChild);
			childrenChangedCurrent = true;
			newChild.absValid = false;
		}
		assert(newChild.parent == this);
	}

	/** set parent to null. */
	private void removeChild(Object oldChild) {
		if (children.remove(oldChild)) {
			assert(oldChild.parent == this);
			childrenChangedCurrent = true;
			oldChild.parent = null;
			oldChild.absValid = false;
		}
	}

	// --- push & pop local ---
	
	/** 
	 * push local to the object depending on the parent relationship. 
	 * update the abs variables if needed (matrix & locationAbs). 
	 **/
	protected void pushLocal() {
		updateAbs();
		assert (!app.getMatrix().equals(new PMatrix3D())); //sinon app.resetMatrix();
		app.pushMatrix();
		app.applyMatrix(matrix);
	}
	
	protected void popLocal() {
		app.popMatrix();
	}
	
	// --- Update stuff (+transformChanged) ---

	/** Called before all objects' update. */
	protected void beforeUpdate() {
		this.updated = false;
		childrenChanged = childrenChangedCurrent;
		childrenChangedCurrent = false;
	}

	/** 
	 * 	Update the state from relative variables deltas (locationRelVel & rotationRelVel).
	 * 	Called every frame with the updated flag. update parent first.
	 * 	Should be called at first by children. 
	 * 	Return true if the object was updated or false if it already was for this frame.
	 **/
	protected boolean update() {
		if (scene == null)
			throw new IllegalArgumentException("The object \""+toString()+"\" should be added to the scene before updating it.");
		if (hasParent() && !parent.updated) {
			boolean pu = parent.update();
			assert(pu);
			assert(!updated);
		}
		if (!updated) {
			game.debug.setCurrentWork("physic: updating \""+presentation()+"\"");
			// update changes
			locationRel.update();
			locationAbs.update();
			rotationRel.update();
			rotationAbs.update();
			// update flags
			if (rotationChanged = rotationRel.hasChanged() || rotationAbs.hasChanged())
				model.notifyChange(Change.Rotation);
			if (locationChanged = locationRel.hasChanged() || locationAbs.hasChanged())
				model.notifyChange(Change.Location);
			transformChanged = rotationChanged || locationChanged;
			if (transformChanged)
				absValid = false;
			return updated = true;
		} else
			return false;
	}
	
	/** 
	 * Update the local absolute variables if needed. check the parent (of course ^^). 
	 * Compute them from relative location & rotation. 
	 * Call updateAbs() on children with updated set to true.
	 * Should be called at first by child class.
	 * Return true if something was updated.
	 **/
	protected boolean updateAbs() {
		if (!absValid()) {
			// 1. ask for the parent
			if (hasParent())
				parent.updateAbs();
			app.pushMatrix();
			app.resetMatrix(); //we're working clean here !
			switch(parentRel) {
			case Full:
				parent.pushLocal();
				translate(locationRel);
				rotateBy(rotationRel);
				locationAbs.set(model(zero));
				rotationAbs.set( parent.rotation().rotatedBy(rotationRel) );
				matrix = app.getMatrix();
				parent.popLocal();
				break;
			case Static:
				locationAbs.set( add(parent.location(), locationRel) );
				rotationAbs.set(rotationRel);
				translate(locationAbs);
				rotateBy(rotationRel);
				matrix = app.getMatrix();
				break;
			default: //None
				translate(locationRel);
				rotateBy(rotationRel);
				matrix = app.getMatrix();
				locationAbs.set(locationRel);
				rotationAbs.set(rotationRel);
				break;
			}
			app.popMatrix();
			// caus' modification from inside -> no need of notif.
			locationAbs.reset(); 
			rotationAbs.reset();
			// notify children
			for (Object o : children)
				o.absValid = false;
			absValid = true;
			return true;
		} else
			return false;
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
	
	// --- Observation ---
	
	public enum Change {
		Location, Rotation, Velocity, RotVelocity, DisplayCollider, Mass
	}
	
	protected final ObjectModel model = new ObjectModel();
	
	public void addObserver(Observer o) {
		model.addObserver(o);
	}
	
	/** To let someone watch this object */
	protected final static class ObjectModel extends Observable {
		public void notifyChange(Change change) {
			ToolWindow.run(() -> {
				synchronized (this) {
					setChanged();
					notifyObservers(change);
				}
			});
		}
	}
	
	// --- syntactic sugar for space change ---

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
}
