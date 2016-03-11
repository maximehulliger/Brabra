package cs211.tangiblegame.physic;

import java.util.ArrayList;
import java.util.List;

import cs211.tangiblegame.Debug.Debugable;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.PMatrix;
import processing.core.PVector;

/** A movable object with transforms (location, rotation, parent), velocity and rotational velocity. */
public class Object extends ProMaster implements Debugable {
	/** 
	 * ParentRelationship define some ways to get in local space (via pushLocal()). <p>
	 * Full: this will follow the parent (with the parent loc & rot). <p>
	 * Relative: this will follow relatively the parent absolute loc (ignore parent final rotation). <p>
	 **/
	 // StaticRot: this will follow statically the parent absolute loc and apply (after) rotate.
	public enum ParentRelationship {
		Full, Static, None/*StaticRot*/;
		public static ParentRelationship fromString(String f) {
			if (f!=null && f.equals("static"))
				return Static;
			//else if (f!=null && f.equals("staticRot"))
			//	return StaticRot;
			else 
				return Full;
		}
	}
	
	/** Position relative to the parent. */
	protected final NVector locationRel;
	/** Absolute position. Equals location if no parents. */
	protected final NVector locationAbs;
	/** Velocity relative to the parent. */
	protected final NVector velocityRel = new NVector(zero);
	/** Rotation relative to the parent. */
	protected final NQuaternion rotationRel;
	protected final NQuaternion rotationRelVel = new NQuaternion(identity);
	/** flag used during the main update loop. */
	/*pkg*/boolean updated = false;
	
	private Object parent = null;
	private ParentRelationship parentRel = ParentRelationship.Full;
	private final List<Object> children = new ArrayList<>();
	private String name = "MyObject";
	/** Indicates if the object was moving last frame. */
	private boolean moving = false, rotating = false;
	/** Matrice représentant les transformation (full) jusqu'à cet objet (lazy). */
	private PMatrix matrix = null;
	/** for the matrix and positions. */
	private boolean absValid = false;
	/** Indicate the modification of the body transform during the frame before the update. */
	private boolean transformChanged = false, locationChanged = false, rotationChanged = false;
	
	/** Create a Body with this location & rotation. rotation can be null. */
	public Object(PVector locationRel, Quaternion rotation) {
		this.locationRel = new NVector(locationRel, () -> {absValid=false;});
		this.rotationRel = new Quaternion.NQuaternion(rotation);
		this.locationAbs = new NVector(locationRel, () -> {
			game.debug.err("modification of absolute location not yet supported.");//TODO called?
			});
	}
	
	/** Create a Body with this location & no initial rotation. */
	public Object(PVector location) {
		this(location, identity);
	}

	/** To display the object. */
	public void display() {}
	
	/** To display the state of the object in the console. */
	public void displayState() {}
	
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
		return transformChanged();
	}
	
	public boolean hasParent() {
		return parent != null;
	}
	
	public Object parent() {
		return parent;
	}

	/** Return the absolute location of the object. update things if needed. */
	public PVector location() {
		return locationAbs;
	}
	
	/** for now return the relative rotation. */
	public Quaternion rotation() {
		return rotationRel;
	}
	
	public PVector velocity() {
		return velocityRel;
	}

	/** Return if the transforms of the object or one of his parent changed during last frame. */
	public boolean transformChanged() { 
		return transformChanged;
	}

	/** Return if the transforms of the object or one of his parent changed during last frame. */
	public boolean locationChanged() { 
		return transformChanged;
	}

	/** Return if the transforms of the object or one of his parent changed during last frame. */
	public boolean rotationChanged() { 
		return rotationChanged;
	}

	/** Return true if this has a parentRel link with other */
	public boolean isRelated(Object other) {
		if (other == this)
			throw new IllegalArgumentException("Object: isRelated called on himself !");
		return other != null && (isChildren(other) || other.isChildren(this));
	}
	
	/** Return true if this is a children of other. */
	public boolean isChildren(Object parent) {
		Object p = this.parent();
		while (p != null) {
			if (p == parent)
				return true;
			p = p.parent;
		}
		return false;
	}

	// --- Setters ---
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Object withName(String name) {
		setName(name);
		return this;
	}

	/** 
	 * Set the transform/push relationship from this with his parent (parent null -> root).
	 * see ParentRelationship for more.
	 **/
	public void setParentRel(ParentRelationship rel) {
		this.parentRel = rel;
	}
	
	/** 
	 * Set the parent object of this object. This will now follow the parent and 
	 * apply this loc and rot (depending on parentRel) to get in local space. 
	 * set parentRel to Full. parent can be null.
	 **/
	public void setParent(Object newParent) {
		if (newParent == this)
			throw new IllegalArgumentException("Un objet ne sera pas son propre parent !");
		if (isRelated(newParent))
			throw new IllegalArgumentException("Object: new parent already related !");
		if (hasParent())
			parent.children.remove(this);
		parentRel = ParentRelationship.Full;
		parent = newParent;
		if (hasParent())
			parent.children.add(this);
	}
	
	// --- update stuff (+transformChanged) ---

	/** 
	 * update the local absolute variables if needed. check the parent (of course ^^). 
	 * Compute them from relative location & rotation. should be called by children. 
	 **/
	public void updateAbs() {
		if (!absValid) {
			if (hasParent())
				parent.pushLocal(); //update for parent is in the pushlocal
			app.pushMatrix();
			translate(locationRel);
			rotateBy(rotationRel);
			matrix = app.getMatrix();
			app.popMatrix();
			locationAbs.set(model(zero));
			if (hasParent())
				parent.popLocal();
			absValid = true;
		}
	}
	
	/** 
	 * 	Update l'etat (location & rotation). 
	 * 	Overriden to update abs after transformChanged.
	 * */
	public void update() {
		if (!game.physic.paused) {
			//1. movement
			boolean velZero = velocityRel.equals(zero);
			boolean velZeroEps = velZero || isZeroEps(velocityRel, true);
			if (!velZeroEps) {
				if (!moving) {
					game.debug.log(6, this+" started moving.");
					moving = true;
				}
				PVector depl = velocityRel;
				locationRel.add( depl );
				locationAbs.set(absolute(zero));
			} else if (moving) {
				game.debug.log(6, this+" stopped moving.");
				moving = false;
			}
				
			//2. rotation
			boolean rotZero = rotationRelVel.isIdentity();
			boolean rotZeroEps = rotZero || rotationRelVel.isZeroEps(true);
			if (!rotZeroEps) {
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
		
		//3. check changes
		locationRel.update();
		locationAbs.update();
		rotationRel.update();
		rotationRelVel.update();
		
		rotationChanged = rotationRel.hasChanged();
		locationChanged = locationRel.hasChanged() || locationAbs.hasChanged();
		transformChanged = rotationChanged || locationChanged;
		if (transformChanged)
			absValid = false;
	}
	
	/** return the presentation of the object with the name in evidence and the parent if exists. */
	public String presentation() {
		return "> "+this+" <" + (!hasParent() ? "" : " (on \""+parent+"\")");
	}

	public String getStateUpdate() {
		if (transformChanged) {
			final String headStart = "", headEnd = " ---";
			final String presentation = presentation() + " ";
			final String moveType = (locationAbs.hasChanged() ? "abs" : "")
					+ (locationAbs.hasChanged() && locationRel.hasChanged() ? " + " : "")
					+ (locationRel.hasChanged() ? "rel":"");
			final String transType = (rotationChanged ? "rot + " : "") + moveType;
			final String changeName = (transformChanged ?
					"transforms("+transType+") changed" :
						(locationChanged ? "location("+moveType+") changed"
								: "rotation changed"));
			final String changeStr = 
					(locationChanged ? "\nlocation: "+locationAbs : "")
					+ (!hasParent() ? "" : "\nlocationRel: "+locationRel)
					+ (velocityRel.equals(zero) ? "" : "\nvitesse rel: "+velocityRel+" -> "+velocityRel.mag()+" unit/frame.") 
					+ (rotationChanged ? "\nrotation rel: "+rotationRel : "")
					+ (rotationRelVel.isZeroEps(false)	? "" : "\nvitesse Ang.: "+rotationRelVel);
			return headStart + presentation + " " + changeName + headEnd + changeStr;
		} else
			return "";
	}
	
	/** retourne l'orientation locale (parent pas pris en compte) */
	public PVector orientation() {
		updateAbs();
		return absolute(down, zero, rotationRel);
	}
	
	// --- conversion vector global <-> local ---

	/** Retourne la position de rel, un point relatif au body en absolu. */
	public PVector absolute(PVector rel) {
		PVector relAbs = absolute(rel, locationRel, rotationRel);
		if (hasParent())
			return parent.absolute(relAbs);
		else
			return relAbs;
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
	
	/** return the pos in front of the body at dist from location */
	public PVector absFront(float dist) {
		return absolute(front(dist), zero, rotationRel);
	}
	
	/** Return the pos in front of the body at dist from location. */
	public PVector absUp(float dist) {
		return absolute(up(dist), zero, rotationRel);
	}
	
	protected PVector velocityAt(PVector loc) {
		PVector relVel = velocityRel.copy();
		/*PVector rotVelAxis = rotationVel.rotAxis();
		if (!isZeroEps( rotationVel.angle ));
			relVel.add( rotVelAxis.cross(PVector.sub(loc, location)) );*/
		if (hasParent())
			return PVector.add( parent.velocityAt(loc), relVel);
		else
			return relVel;
	}
	
	/** push local to the children depending on the parent relationship. update the abs variables if needed (matrix&locationAbs). */
	protected void pushLocal() {
		// then the current object
		switch (parentRel) {
		case Full:
			updateAbs();
			app.pushMatrix();
			app.applyMatrix(matrix);
			break;
		case Static: 
			if (hasParent()) {
				parent.updateAbs();
				app.pushMatrix();
				translate(parent.location());
			}
			app.pushMatrix();
			translate(locationRel);
			rotateBy(rotationRel);
			break;
		case None:
			app.pushMatrix();
			translate(locationRel);
			rotateBy(rotationRel);
			break;
		/*case StaticRot:
			translate(locationRel);
			resetRotMatrix();
			app.pushMatrix();
			break;*/
		}
	}
	
	protected void popLocal() {
		app.popMatrix();
		if (hasParent()) {
			switch (parentRel) {
			case Static:
				app.popMatrix();
				break;
			default:
				break;
			}
		}
	}
}
