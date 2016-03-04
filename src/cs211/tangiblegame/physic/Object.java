package cs211.tangiblegame.physic;

import cs211.tangiblegame.Debug.Debugable;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.PMatrix;
import processing.core.PVector;

/** A movable object (with transform). */
public class Object extends ProMaster implements Debugable {
	public enum Parency { 
		None, Follow, FollowPosition, FollowRotation;
		public static Parency fromString(String f) {
			if (f.equals("position"))
				return FollowPosition;
			else if (f.equals("none"))
				return None;
			else 
				return Follow;
		}
	}
	/** Position relative to the parent. */
	public final PVector locationRel;
	/** Absolute position. Equals location if no parents. */
	public final PVector location;
	/** Velocity relative to the parent. */
	public final PVector velocity = zero.copy();
	/** Rotation relative to the parent. */
	public final Quaternion rotation;
	public final Quaternion rotationVel = identity.copy();
	/** Indicate the modification of the body transform during the frame before the update. */
	public boolean transformChanged = false, locationChanged = false, locationRelChanged = false, rotationChanged = false;
	
	protected Object parent = null;
	protected String name = "MyObject";
	private boolean rotationChangedCurrent = false, locationChangedCurrent = false, locationRelChangedCurrent = false;
	
	private Parency parency = Parency.None;
	/** Matrice représentant les transformation jusqu'à cet objet (lazy). */
	private PMatrix matrix = null;
	private boolean matrixValid = false;
	
	/** Create a Body with this location & rotation. rotation can be null. */
	public Object(PVector locationRel, Quaternion rotation) {
		this.locationRel = new UVector(locationRel, () -> {locationRelChangedCurrent = true;});
		this.rotation = new UQuaternion(rotation, () -> {rotationChangedCurrent = true;});
		this.location = new UVector(locationRel, () -> {
			locationChangedCurrent = true;
			game.debug.err("modification of absolute location not yet supported.");
			});
	}
	
	/** Create a Body with this location & no initial rotation. */
	public Object(PVector location) {
		this(location, null);
	}

	/** To display the object. */
	public void display() {}
	
	/** To display the state of the object in the console. */
	public void displayState() {}
	
	/** To react when the object is removed from the scene. */
	public void onDelete() {}
	
	// --- some setters ---

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
	
	public void setParent(Object parent, Parency parency) {
		if (parent == null || parency == Parency.None) {
			this.parent = null;
			this.parency = Parency.None;
		} else {
			this.parent = parent;
			this.parency = parency;
		}
	}
	
	// --- update stuff (+transformChanged) ---

	/** update the local absolute variables if needed. check the parent (of course ^^). **/
	//public void updateAbs() {}
	
	/** 
	 * 	Update l'etat (location & rotation). 
	 * 	Overriden to update abs after transformChanged.
	 * */
	public void update() {
		//1. movement
		boolean velZero = velocity.equals(zero);
		boolean velZeroEps = isZeroEps(velocity, true);
		if (!velZeroEps) {
			PVector depl = velocity;
			locationRel.add( depl );
			location.set(absolute(zero));
		} else if (!velZero && velZeroEps)
			game.debug.log(6, "\""+this+"\" stopped moving.");
			
		//2. rotation
		boolean rotZero = rotationVel.equals(identity);
		boolean rotZeroEps = rotationVel.isZeroEps(true);
		if (!rotZeroEps)
			rotation.rotate( rotationVel );
		else if (!rotZero && rotZeroEps)
			game.debug.log(6, "\""+this+"\" stopped rotating.");
		
		// just to be sure :p
		if (game.physic.deltaTime == 0) {
			assert(velZero);
			assert(rotZero);
		}
		
		//3. check changes
		locationChanged = locationChangedCurrent;
		locationRelChanged = locationRelChangedCurrent;
		rotationChanged = rotationChangedCurrent;
		locationChangedCurrent = false;
		locationRelChangedCurrent = false;
		rotationChangedCurrent = false;
		
		transformChanged = locationRelChanged || rotationChanged || locationChanged;
		if (transformChanged)
			matrixValid = false;
	}

	public String getStateUpdate() {
		if (transformChanged) {
			final String headStart = "--- ", headEnd = " ---\n";
			final String presentation = "\""+this+"\""
					+ (parent==null ? "" : " (on \""+parent+"\")")+" ";
			final String moveType = (locationChanged?"abs":"")
					+ (locationChanged && locationRelChanged ? " + " : "")
					+ (locationRelChanged?"rel":"");
			final String transType = (rotationChanged ? "rot + " : "") + moveType;
			final String locStr = locationChanged || locationRelChanged
					? "location: "+location
					+ (parent==null ? "" : "\nlocationRel: "+locationRel)
					+ (velocity.equals(zero) ? "" : "\nvitesse: "+velocity+" -> "+velocity.mag()+" unit/frame.") 
					: "";
			final String rotStr = 
					"rotation: "+rotation
					+ (rotationVel.isZeroEps(false) 
							? "" : "\nvitesse Ang.: "+rotationVel);
			if ((locationChanged || locationRelChanged) && rotationChanged)
				return headStart+presentation+"transforms("+transType+") changed"+headEnd
					+ locStr+"\n"
					+ rotStr;
			else if (locationChanged || locationRelChanged) {
				return headStart+presentation+"location("+moveType+") changed"+headEnd
					+ locStr;
			} else //rotation changed
				return headStart+presentation+"rotation changed"+headEnd
					+ rotStr;
		} else
			return null;
	}
	
	/** PVector notifiant le body des changements (mais refuse les modification impures). */
	private class UVector extends PVector {
		private static final long serialVersionUID = 5162673540041216409L;
		private static final String errMsg = "don't touch my transform that way";
		private final Runnable onChange;
		
		public UVector(PVector v, Runnable onChange) {
			super(v.x,v.y,v.z);
			onChange.run();
			this.onChange = onChange;
		}

		public PVector set(PVector v) {
			v = super.set(v);
			onChange.run();
			return v;
		}
		
		public PVector set(float[] source) {
			PVector v = super.set(source);
			onChange.run();
			return v;
		}

		public PVector set(float x, float y, float z) {
			PVector v = super.set(x,y,z);
			onChange.run();
			return v;
		}

		public PVector set(float x, float y) {
			PVector v = super.set(x,y);
			onChange.run();
			return v;
		}
		
		public PVector add(PVector v) {
			v = super.add(v);
			onChange.run();
			return v;
		}
		
		public PVector sub(PVector v) {
			v = super.sub(v);
			onChange.run();
			return v;
		}
		
		public PVector mult(float f) { throw new IllegalArgumentException(errMsg); }
		public PVector div(float f) { throw new IllegalArgumentException(errMsg); }
		public PVector limit(float f) { throw new IllegalArgumentException(errMsg); }
		public PVector setMag(float f) { throw new IllegalArgumentException(errMsg); }
		public PVector normalize() { throw new IllegalArgumentException(errMsg); }
	}
	
	/** Quaternion notifiant le body des changements. */
	private class UQuaternion extends Quaternion {
		private final Runnable onChange;
		
		public UQuaternion(Quaternion q, Runnable onChange) {
			super((q == null) ? identity : q);
			this.onChange = onChange;
		}
		
		public Quaternion set(Quaternion v) {
			onChange.run();
			return super.set(v);
		}
	}
	/** retourne l'orientation locale (parent pas pris en compte) */
	public PVector orientation() {
		return absolute(down, zero, rotation);
	}
	
	// --- conversion vector global <-> local ---

	/** Retourne la position de rel, un point relatif au body en absolu. */
	public PVector absolute(PVector rel) {
		PVector relAbs = absolute(rel, locationRel, rotation);
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
			return local(parent.local(abs), locationRel, rotation);
		else
			return local(abs, locationRel, rotation);
	}
	
	/** return the pos in front of the body at dist from location */
	public PVector absFront(float dist) {
		return absolute(front(dist), zero, rotation);
	}
	
	/** Return the pos in front of the body at dist from location. */
	public PVector absUp(float dist) {
		return absolute(up(dist), zero, rotation);
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
		if (matrixValid && parency == Parency.Follow) {
			app.applyMatrix(matrix);
			app.pushMatrix();
		} else if (parency != Parency.None) {
			boolean translate = parency != Parency.FollowRotation;
			boolean rotate = parency != Parency.FollowPosition;
			if (matrixValid && translate && rotate) {
				app.applyMatrix(matrix);
				app.pushMatrix();
			} else {
				if (parent != null)
					parent.pushLocal();
				if (translate)
					translate(locationRel);
				if (rotate)
					rotateBy(rotation);
				app.pushMatrix();
				matrix = app.getMatrix();
				
				matrixValid = true;
			}
		}
	}
	
	protected void popLocal() {
		if (parency != Parency.None) {
			app.popMatrix();
		}
	}
}
