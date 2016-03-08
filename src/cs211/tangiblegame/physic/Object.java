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
	 * StaticRot: this will follow statically the parent absolute loc and apply (after) rotate.
	 **/
	public enum ParentRelationship {
		Full, Static, StaticRot;
		public static ParentRelationship fromString(String f) {
			if (f!=null && f.equals("static"))
				return Static;
			else if (f!=null && f.equals("staticRot"))
				return StaticRot;
			else 
				return Full;
		}
	}
	
	/** Position relative to the parent. */
	public final PVector locationRel;
	/** Absolute position. Equals location if no parents. */
	public final PVector locationAbs;
	/** Velocity relative to the parent. */
	public final PVector velocityRel = zero.copy();
	/** Rotation relative to the parent. */
	public final Quaternion rotationRel;
	public final Quaternion rotationRelVel = identity.copy();
	/** Indicate the modification of the body transform during the frame before the update. */
	public boolean transformChanged = false, locationAbsChanged = false, locationRelChanged = false, rotationChanged = false;
	/** flag used during the main update loop. */
	/*pkg*/boolean updated = false;
	
	private Object parent = null;
	private ParentRelationship parentRel = ParentRelationship.Full;
	private final List<Object> children = new ArrayList<>();
	private String name = "MyObject";
	private boolean rotationChangedCurrent = true, locationAbsChangedCurrent = true, locationRelChangedCurrent = true;
	/** Indicates if the object was moving last frame. */
	private boolean moving = false, rotating = false;
	/** Matrice représentant les transformation jusqu'à cet objet (lazy). */
	private PMatrix matrix = null;
	private boolean matrixValid = false;
	
	/** Create a Body with this location & rotation. rotation can be null. */
	public Object(PVector locationRel, Quaternion rotation) {
		this.locationRel = new NVector(locationRel, () -> {locationRelChangedCurrent = true;});
		this.rotationRel = new Quaternion.NQuaternion(rotation, () -> {rotationChangedCurrent = true;});
		this.locationAbs = new NVector(locationRel, () -> {
			locationAbsChangedCurrent = true;
			game.debug.err("modification of absolute location not yet supported.");
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
	
	/** To react when the object is removed from the scene. */
	public void onDelete() {}
	
	// --- some getters / setters ---

	public void setName(String name) {
		this.name = name;
	}
	
	public Object withName(String name) {
		setName(name);
		return this;
	}
	
	public String toString() {
		return name;
	}
	
	public boolean hasParent() {
		return parent != null;
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
	
	/** 
	 * Set the transform/push relationship from this with his parent (parent null -> root).
	 * see ParentRelationship for more.
	 **/
	public void setParentRel(ParentRelationship rel) {
		this.parentRel = rel;
	}
	
	public Object parent() {
		return parent;
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
	
	// --- update stuff (+transformChanged) ---

	/** update the local absolute variables if needed. check the parent (of course ^^). **/
	//public void updateAbs() {}
	
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
		locationAbsChanged = locationAbsChangedCurrent;
		locationRelChanged = locationRelChangedCurrent;
		rotationChanged = rotationChangedCurrent;
		locationAbsChangedCurrent = false;
		locationRelChangedCurrent = false;
		rotationChangedCurrent = false;
		
		transformChanged = locationRelChanged || rotationChanged || locationAbsChanged;
		if (transformChanged)
			matrixValid = false;
	}
	
	/** return the presentation of the object with the name in evidence and the parent if exists. */
	public String presentation() {
		return "> "+this+" <" + (!hasParent() ? "" : " (on \""+parent+"\")");
	}

	public String getStateUpdate() {
		if (transformChanged) {
			final String headStart = "", headEnd = " ---";
			final String presentation = presentation()+" ";
			final String moveType = (locationAbsChanged?"abs":"")
					+ (locationAbsChanged && locationRelChanged ? " + " : "")
					+ (locationRelChanged?"rel":"");
			final String transType = (rotationChanged ? "rot + " : "") + moveType;
			final String changeName = ((locationAbsChanged || locationRelChanged) && rotationChanged ?
					"transforms("+transType+") changed" :
						(locationAbsChanged || locationRelChanged ? "location("+moveType+") changed"
								: "rotation changed"));
			final String changeStr = 
					(locationAbsChanged || locationRelChanged ? "\nlocation: "+locationAbs : "")
					+ (!hasParent() ? "" : "\nlocationRel: "+locationRel)
					+ (velocityRel.equals(zero) ? "" : "\nvitesse rel: "+velocityRel+" -> "+velocityRel.mag()+" unit/frame.") 
					+ (rotationChanged ? "\nrotation rel: "+rotationRel : "")
					+ (rotationRelVel.isZeroEps(false)	? "" : "\nvitesse Ang.: "+rotationRelVel);
			return headStart + presentation + " " + changeName + headEnd + changeStr;
		} else
			return null;
	}
	
	/** retourne l'orientation locale (parent pas pris en compte) */
	public PVector orientation() {
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
	
	/** push local to the children depending on the parent relationship. */
	protected void pushLocal() {
		assert (parent != this);
		// first push the parent.
		if (hasParent())
			parent.pushLocal();
		// then the current object
		switch (parentRel) {
		case Full:
			if (matrixValid) {
				app.applyMatrix(matrix);
				app.pushMatrix();
			} else {
				translate(locationRel);
				rotateBy(rotationRel);
				app.pushMatrix();
				matrix = app.getMatrix();
				matrixValid = true;
			}
			break;
		case Static: //TODO
			rotateBy(rotationRel);
			app.pushMatrix();
			break;
		case StaticRot: //TODO
			translate(locationRel);
			app.pushMatrix();
			break;
		}
	}
	
	protected void popLocal() {
		app.popMatrix();
		if (hasParent()) {
			parent.popLocal();
		}
	}
}
