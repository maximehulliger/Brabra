package cs211.tangiblegame.physic;

import java.util.ArrayList;
import java.util.List;

import cs211.tangiblegame.Debug.Debugable;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;

/** A movable object with transforms (location, rotation, parent), velocity and rotational velocity. */
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
	
	/** Position relative to the parent. */
	protected final NVector locationRel = new NVector(zero);
	/** Absolute position. Equals locationRel if no parent. */
	protected final NVector locationAbs = new NVector(zero);
	/** Velocity relative to the parent. */
	protected final NVector velocityRel = new NVector(zero);
	/** Rotation relative to the parent. */
	protected final NQuaternion rotationRel = new NQuaternion(identity);
	protected final NQuaternion rotationRelVel = new NQuaternion(identity);
	/** Flag used during the main update loop. */
	protected boolean updated = false;
	
	private Object parent = null;
	private ParentRelationship parentRel = ParentRelationship.None;
	private final List<Object> children = new ArrayList<>();
	private String name = "MyObject";
	/** Indicates if the object was moving last frame. */
	private boolean moving = false, rotating = false;
	/** Matrix representing the matrix transformations till this object (lazy). */
	private PMatrix matrix = null;
	/** for the matrix and positions. */
	private boolean absValid = false;
	/** Indicate the modification of the body transform during the last frame. */
	private boolean transformChanged = false, locationChanged = false, rotationChanged = false;
	
	/** Create a Body with this location & rotation. rotation can be null. */
	public Object(PVector location, Quaternion rotation) {
		locationRel.setOnChange(() -> {absValid=false;});
		rotationRel.setOnChange(() -> {absValid=false;});
		locationAbs.setOnChange(() -> {});
		locationRel.set(location);
		locationAbs.set(location);
		if (rotation != null) {
			rotationRel.set(rotation);
		}
	}
	
	/** Create a Body with this location & no initial rotation. */
	public Object(PVector location) {
		this(location, identity);
	}

	/** To display the object. */
	public void display() {}
	
	/** To display the state of the object in the console. */
	public void displayState() {
		debug.info(2, presentation()+" "+state(true, true, true, true));
	}
	
	/** To react when the object is removed from the scene. should be called. */
	public void onDelete() {
		if (hasParent())
			parent.children.remove(this);
	}
	
	// --- Getters ---

	public boolean equals(java.lang.Object other) {
		return other == this;
	}

	public String toString() {
		return name;
	}

	public boolean stateChanged() {
		return transformChanged;
	}
	
	/** Return true if the absolute variables are valid (check the parent). */
	protected boolean absValid() {
		if (hasParent() && !parent.absValid())
			return false;
		else
			return absValid;
	}
	
	/** Return true if the object should consider his parent. */
	public boolean hasParent() {
		return parent != null && parentRel != ParentRelationship.None;
	}
	
	/** Return the parent of the object (even if parentRel is None -> can be null). */
	public Object parent() {
		return parent;
	}

	/** Return the absolute location of the object. update things if needed. */
	public PVector location() {
		updateAbs();
		return locationAbs;
	}
	
	/** for now return the relative rotation. */
	public Quaternion rotation() {
		return rotationRel;
	}
	
	/** for now return the relative velocity. */
	public PVector velocity() {
		return velocityRel;
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

	/** Return true if this has a parentRel link with other */
	public boolean isRelated(Object other) {
		if (other == this || other == null)
			throw new IllegalArgumentException("Object "+other+" isRelated called on himself ! (or null)");
		// case #1: one parent of the other
		if (isChildren(other) || other.isChildren(this))
			return true;
		// case #2: common parent -> check for all parent of this if other is a child.
		for (Object parent=parent(); parent!=null; parent=parent.parent()) {
			if (other.isChildren(parent) || other.isChildren(parent))
				return true;
		}
		return false;
	}
	
	/** Return true if this is a children of other. */
	public boolean isChildren(Object parent) {
		for (Object p=parent(); p!=null; p=p.parent()) {
			if (p == parent)
				return true;
		}
		return false;
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
		if (rel != null) {
			if (rel != ParentRelationship.None && parent == null)
				parentRel = ParentRelationship.None;
			else 
				parentRel = rel;
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
				throw new IllegalArgumentException("Object: new parent already related !");
			if (hasParent())
				parent.children.remove(this);
			parent = newParent;
			if (newParent == null)
				setParentRel(ParentRelationship.None);
			else if (parentRel == ParentRelationship.None)
				setParentRel(ParentRelationship.Full);
			absValid = false;
			if (hasParent())
				parent.children.add(this);
			return true;
		} else
			return false;
	}
	
	// --- update stuff (+transformChanged) ---

	/** 
	 * 	Update the state from relative variables deltas (locationRelVel & rotationRelVel).
	 * 	Called every frame with the updated flag. update parent first.
	 * 	Should be called at first by children. 
	 * 	Return true if the object was updated or false if it already was for this frame.
	 **/
	public boolean update() {
		if (hasParent() && !parent.updated) {
			boolean pu = parent.update();
			assert(pu);
			assert(!updated);
		}
		if (!updated) {
			game.debug.setCurrentWork("physic: updating \""+this+"\"");
			updated = true;
			// 1. movement
			if (moving || velocityRel.hasChangedCurrent()) {
				if (!isZeroEps(velocityRel, false)) {
					if (!moving) {
						game.debug.log(6, this+" started moving.");
						moving = true;
					}
					locationRel.add( velocityRel );
					locationAbs.set(absolute(zero));
				} else if (moving) {
					game.debug.log(6, this+" stopped moving.");
					moving = false;
				}
			}
			// 2. rotation
			if (rotating || rotationRelVel.hasChangedCurrent()) {
				if (!rotationRelVel.isZeroEps(false)) {
					if (!rotating) {
						game.debug.log(6, this+" started rotating.");
						rotating = true;
					}
					rotationRel.rotate( rotationRelVel );
				} else if (rotating) {
					game.debug.log(6, this+" stopped rotating.");
					rotating = false;
				}
			}
			// 3. check changes
			locationRel.update();
			locationAbs.update();
			velocityRel.update();
			rotationRel.update();
			rotationRelVel.update();
			
			rotationChanged = rotationRel.hasChanged();
			locationChanged = locationRel.hasChanged() || locationAbs.hasChanged();
			transformChanged = rotationChanged || locationChanged;
			if (transformChanged)
				absValid = false;
			return true;
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
				locationAbs.set( model(zero) );
				matrix = app.getMatrix();
				parent.popLocal();
				break;
			case Static:
				locationAbs.set( add(parent.location(), locationRel) );
				translate(locationAbs);
				rotateBy(rotationRel);
				matrix = app.getMatrix();
				break;
			default: //None
				translate(locationRel);
				rotateBy(rotationRel);
				matrix = app.getMatrix();
				locationAbs.set(locationRel);
				break;
			}
			app.popMatrix();
			locationAbs.reset(); //caus' modification from inside -> no need of notif.
			for (Object o : children)
				o.absValid = false;
			absValid = true;
			return true;
		} else
			return false;
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
			final String changeStr = state(locationChanged, velocityRel.equals(zero),
					rotationChanged, rotationRelVel.isIdentity());
					
			return presentation() + " " + changeName + " ---" + changeStr;
		} else
			return "";
	}
	
	protected String state(boolean loc, boolean vel, boolean rot, boolean rotVel) {
		return (loc ? "\nlocation: "+locationAbs : "")
				+ (!hasParent() ? "" : "\nlocationRel: "+locationRel)
				+ (vel ? "" : "\nvitesse rel: "+velocityRel+" -> "+velocityRel.mag()+" unit/frame.") 
				+ (rot ? "\nrotation rel: "+rotationRel : "")
				+ (rotVel	? "" : "\nvitesse Ang.: "+rotationRelVel);
	}
	
	/** retourne l'orientation de l'objet (pour la camera) */
	public PVector orientation() {
		return absDir(down);
	}
	
	// --- conversion vector global <-> local ---

	/** Retourne la position de rel, un point relatif au body en absolu. */
	public PVector absolute(PVector rel) {
		PVector local = absolute(rel, locationRel, rotationRel);
		if (hasParent())
			return parent.absolute(local);
		else
			return local;
	}
	
	protected PVector[] absolute(PVector[] rels) {
		PVector[] ret = new PVector[rels.length];
		for (int i=0; i<rels.length; i++)
			ret[i] = absolute(rels[i]);
		return ret;
	}
	protected PVector local(PVector abs) {
		if (hasParent())
			return local(parent.local(abs), locationRel, rotationRel);
		else
			return local(abs, locationRel, rotationRel);
	}
	
	/** Return the relative direction vector to the body at in absolute. result is only rotated -> same norm. */
	public PVector absDir(PVector dir) {
		PVector inParentSpace = absolute(dir, zero, rotationRel);
		if (hasParent())
			return parent.absDir(inParentSpace);
		else 
			return inParentSpace;
	}
	
	protected PVector velocityAt(PVector loc) {
		PVector relVel = velocityRel.copy();
		/*PVector rotVelAxis = rotationVel.rotAxis();
		if (!isZeroEps( rotationVel.angle ));
			relVel.add( rotVelAxis.cross(PVector.sub(loc, location)) );*/
		if (hasParent())
			return PVector.add(parent.velocityAt(loc), relVel);
		else
			return relVel;
	}
	
	/** push local to the object depending on the parent relationship. update the abs variables if needed (matrix&locationAbs). */
	protected void pushLocal() {
		updateAbs();
		assert (!app.getMatrix().equals(new PMatrix3D()));
		app.pushMatrix();
		//app.resetMatrix();
		app.applyMatrix(matrix);
		//System.out.println("in "+presentation());
	}
	
	protected void popLocal() {
		app.popMatrix();
		//System.out.println("out "+presentation());
	}
}
