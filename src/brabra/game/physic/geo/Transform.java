package brabra.game.physic.geo;

import java.util.Observable;

import brabra.game.Observable.NQuaternion;
import brabra.game.Observable.NVector;
import brabra.game.scene.Object;
import processing.core.PMatrix;

/** Represent the transformation of this transform's object. */
public class Transform extends ProTransform {
	// > core
	/** Position relative to the parent. */
	public final NVector position = new NVector();
	/** Rotation relative to the parent. */
	public final NQuaternion rotation = new NQuaternion();

	/** Matrix representing the matrix transformations till this object (lazy). */
	private PMatrix matrixAbs = null;
	/** for the matrix and positions. */
	private boolean absValid = false;
	
	public Transform() {
		position.addOnChange(() -> unvalidateAbs());
		rotation.addOnChange(() -> unvalidateAbs());
	}

	private void unvalidateAbs() {
		absValid = false;
	}
	
	/** Copy the other transform into this and return this. */
	public Transform copy(Transform other) {
		position.set(other.position);
		rotation.set(other.rotation);
		return this;
	}

	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    return (other instanceof Transform) 
	    		? equals((Transform)other) : false;
	}

	public boolean equals(Transform other) {
		return rotation.equals(other.rotation) && position.equals(other.position);
	}
	
	// --- life cycle ---

	/** 
	 * 	Update the state of this transform and his children. 
	 **/
	public void update() {
		// update changes
		position.update();
		rotation.update();
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
			app.pushMatrix();
			app.resetMatrix(); //we're working clean here !
			translate(position);
			rotateBy(rotation);
			matrixAbs = app.getMatrix();
			app.popMatrix();
			
			absValid = true;
		}
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
		return absolute(rel, position, rotation);
	}

	public Vector relative(Vector posAbs) {
		return relative(posAbs, position, rotation);
	}

	public Vector relativeFromLocal(Vector posLoc) {
		return relative(posLoc, Vector.zero, rotation);
	}

	public Vector localFromRel(Vector posRel) {
		return absolute(posRel, Vector.zero, rotation);
	}
	
	// --- direction conversion: local <-> *absolute* <-> relative ---
	
	/** Return the absolute direction from a relative direction in the body space. result is only rotated -> same norm. */
	public Vector absoluteDir(Vector dirRel) {
		return absolute(dirRel, Vector.zero, rotation);
	}

	/** Return the relative direction in the body body space from an absolute direction. result is only rotated -> same norm. */
	public Vector relativeDir(Vector dirAbs) {
		return relative(dirAbs, Vector.zero, rotation);
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
		Mass, Name, Size
	}
	
	public final ObjectModel model = new ObjectModel();
	
	/** To let someone watch this object */
	public final class ObjectModel extends Observable {
		
		public void notifyChange(Change change) {
			synchronized (this) {
				setChanged();
				notifyObservers(change);
			}
		}
	}
}
