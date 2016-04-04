package brabra.game;

import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;

/** Observable/Notifying classes with update (state flag per frame) */
public interface Observable {

	/** Set the method executed when the value changes. */
	public void setOnChange(Runnable onChange);
	
	/** Update the changed value (to changedCurrent & set changedCurrent to false). */
	public void update();
	
	/** Return true if the value changed during the last frame. */
	public boolean hasChanged();
	
	/** Return true if the value changed during the current frame. */
	public boolean hasChangedCurrent();
	
	/** Reset the flags to false */
	public void reset();
	
	// --- implementation for Vector & Quaternion ---
	
	/** Vector notifying on change (after changes. not on creation). With a change flag.  */
	public class NVector extends Vector implements Observable {
		private static final long serialVersionUID = 5162673540041216409L;
		private Runnable onChange;
		private boolean changedCurrent = false, changed = false;
		
		public NVector(Vector v, Runnable onChange) { 
			super(v.x,v.y,v.z); 
			setOnChange(onChange); 
		}

		public NVector(Vector v) { 
			this(v, null);
		}
		
		public void setOnChange(Runnable onChange) {
			this.onChange = onChange;
		}
		
		public void update() {
			changed = changedCurrent;
			changedCurrent = false;
		}

		public boolean hasChanged() {
			return changed;
		}
		
		public boolean hasChangedCurrent() {
			return changedCurrent;
		}
		
		public void reset() {
			changed = changedCurrent = false;
		}
		
		// first apply, set changed to true, then notify.
		public Vector set(Vector v) { super.set(v); return onChange(); }
		public Vector set(float[] source) { super.set(source); return onChange(); }
		public Vector set(float x, float y, float z) { super.set(x,y,z); return onChange(); }
		public Vector set(float x, float y) { super.set(x,y); return onChange(); }
		public Vector add(Vector v) { super.add(v); return onChange(); }
		public Vector sub(Vector v) { super.sub(v); return onChange(); }
		public Vector mult(float f) { super.mult(f); return onChange(); }
		public Vector div(float f) { super.mult(f); return onChange(); }
		public Vector limit(float f) { super.mult(f); return onChange(); }
		public Vector setMag(float f) { super.mult(f); return onChange(); }
		public Vector normalize() { super.normalize(); return onChange(); }
		
		private Vector onChange() {
			changedCurrent=true;
			if (onChange != null)
				onChange.run();
			return this;
		}
	}
	
	/** Quaternion notifying on change (after change. not on creation). */
	public static class NQuaternion extends Quaternion implements Observable {
		private Runnable onChange;
		private boolean changed = false, changedCurrent = false;

		public NQuaternion(Quaternion q, Runnable onChange) {
			super(q);
			setOnChange(onChange);
		}

		public NQuaternion(Quaternion q) {
			this(q, null);
		}

		public void setOnChange(Runnable onChange) {
			this.onChange = onChange;
		}
		
		public void update() {
			changed = changedCurrent;
			changedCurrent = false;
		}

		public boolean hasChanged() {
			return changed;
		}

		public boolean hasChangedCurrent() {
			return changedCurrent;
		}

		public void reset() {
			changed = changedCurrent = false;
		}
		
		public Quaternion set(float w, float x, float y, float z) {
			super.set(w,x,y,z);
			return onChange();
		}

		public Quaternion set(Quaternion quat) {
			super.set(quat);
			return onChange();
		}

		private Quaternion onChange() {
			if (onChange != null)
				onChange.run();
			changedCurrent = true;
			return this;
		}
	}
	
	/*public class Flag implements Notifying {
		
	}*/
}
