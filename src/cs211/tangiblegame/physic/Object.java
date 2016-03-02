package cs211.tangiblegame.physic;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.PVector;

/** An movable object with transform. */
public class Object extends ProMaster {
	public final PVector location;
	public final PVector velocity = zero.copy();
	public final Quaternion rotation;
	public final Quaternion rotationVel = identity.copy();
	/** Indicate the modification of the body transform during the frame before the update. */
	public boolean transformChanged = true;

	public Body parent = null;
	protected String name = "Body";
	private boolean transformChangedCurrent = false;
	
	/** create a Body with this location & rotation. rotation can be null */
	public Object(PVector location, Quaternion rotation) {
		this.location = new TVector(location);
		this.rotation = new TQuaternion(rotation);
	}
	
	/** create a Body with this location & no initial rotation */
	public Object(PVector location) {
		this(location, null);
	}

	/** to react when the object is removed from the scene */
	public void onDelete() {}

	// --- update stuff (+transformChanged)
	
	/** applique les forces et update l'etat */
	public void update() {
		if (!isZeroEps(velocity, true)) {
			location.add( mult(velocity, game.physic.deltaTime) );
		}
		if (!rotationVel.isZeroEps(true)/*!rotationVel.equals(identity)*/) {
			rotation.rotate( rotationVel );
		}

		//check changes
		transformChanged = transformChangedCurrent;
		transformChangedCurrent = false;
		
		if (TangibleGame.verbosity > 4 && transformChanged) {
			System.out.println(
					"--- "+this+" transforms changed ---\n"
					+ "rotation: \n"+rotation.toStringAll()+"\n"
					+ "vitesse Ang.: "+rotationVel.angle()+" rad/frame."
					);
		}
	}

	/** PVector notifiant le body des changements */
	private class TVector extends PVector {
		private static final long serialVersionUID = 5162673540041216409L;
		private static final String errMsg = "don't touch my transform that way";
		
		public TVector(PVector v) {
			super(v.x,v.y,v.z);
		}

		public PVector set(PVector v) {
			transformChangedCurrent = true;
			return super.set(v);
		}
		
		public PVector set(float[] source) {
			transformChangedCurrent = true;
			return super.set(source);
		}

		public PVector set(float x, float y, float z) {
			transformChangedCurrent = true;
			return super.set(x,y,z);
		}

		public PVector set(float x, float y) {
			transformChangedCurrent = true;
			return super.set(x,y,z);
		}
		
		public PVector add(PVector v) {
			transformChangedCurrent = true;
			return super.add(v);
		}
		
		public PVector sub(PVector v) {
			transformChangedCurrent = true;
			return super.sub(v);
		}
		
		public PVector mult(float f) { throw new IllegalArgumentException(errMsg); }
		public PVector div(float f) { throw new IllegalArgumentException(errMsg); }
		public PVector limit(float f) { throw new IllegalArgumentException(errMsg); }
		public PVector setMag(float f) { throw new IllegalArgumentException(errMsg); }
		public PVector normalize() { throw new IllegalArgumentException(errMsg); }
	}
	
	/** Quaternion notifiant le body des changements */
	private class TQuaternion extends Quaternion {
		public TQuaternion(Quaternion q) {
			super((q == null) ? identity : q);
		}
		
		public Quaternion set(Quaternion v) {
			transformChangedCurrent = true;
			return super.set(v);
		}
	}

	// --- some setters

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

	// --- conversion vector global <-> local

	/**retourne la position de rel, un point relatif au body en absolu*/
	public PVector absolute(PVector rel) {
		PVector relAbs = absolute(rel, location, rotation);
		if (parent != null)
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
		if (parent != null)
			return local(parent.local(abs), location, rotation);
		else
			return local(abs, location, rotation);
	}
	
	/** return the pos in front of the body at dist from location */
	public PVector absFront(float dist) {
		return absolute(PVector.mult(front, dist), zero, rotation);
	}
	
	/** return the pos in front of the body at dist from location */
	public PVector absUp(float dist) {
		return absolute(PVector.mult(up, dist), zero, rotation);
	}
	
	protected PVector velocityAt(PVector loc) {
		PVector relVel = velocity.copy();
		/*PVector rotVelAxis = rotationVel.rotAxis();
		if (!isZeroEps( rotationVel.angle ));
			relVel.add( rotVelAxis.cross(PVector.sub(loc, location)) );*/
		if (parent != null)
			return PVector.add( parent.velocityAt(loc), relVel);
		else
			return relVel;
	}
	
	protected void pushLocal() {
		if (parent != null)
			parent.pushLocal();
		app.pushMatrix();
		translate(location);
		app.pushMatrix();
		rotateBy(rotation);
	}
	
	protected void popLocal() {
		app.popMatrix();
	    app.popMatrix();
	    if (parent != null)
			parent.popLocal();
	}
}
