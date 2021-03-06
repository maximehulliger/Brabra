package brabra.game;

import java.util.ArrayList;

import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import processing.core.PVector;

/** Observable/Notifying classes with update (state flag per frame) */
public interface Observable {

	/** Add a method to be executed when the value changes. return it to not forget it :) */
	public Runnable addOnChange(Runnable onChange);

	public void removeOnChange(Runnable onChange);
	
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
		private ArrayList<Runnable> onChange = new ArrayList<>(2);
		private boolean changedCurrent = false, changed = false;
		
		public NVector(Vector v, Runnable onChange) { 
			super(v); 
			addOnChange(onChange); 
		}

		public NVector(Vector v) {
			super(v);
		}

		public NVector() { 
			this(zero);
		}

		// --- From observable ---

		public Runnable addOnChange(Runnable onChange) {
			if (onChange != null)
				this.onChange.add(onChange);
			return onChange;
		}
		
		public void removeOnChange(Runnable onChange) {
			if (!this.onChange.remove(onChange))
				throw new IllegalArgumentException("onChange runnable wasn't to run !");
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

		/** Set both flags to false. */
		public void reset() {
			changed = changedCurrent = false;
		}

		/** Set the value and reset the flags. */
		public void reset(Vector newValue) {
			super.set(newValue);
			reset();
		}
		
		// first apply, set changed to true, then notify.
		public Vector set(PVector v) { super.set(v); return onChange(); }
		public Vector set(float[] source) { super.set(source); return onChange(); }
		public Vector set(float x, float y, float z) { super.set(x,y,z); return onChange(); }
		public Vector set(float x, float y) { super.set(x,y); return onChange(); }
		public Vector add(PVector v) { super.add(v); return onChange(); }
		public Vector sub(PVector v) { super.sub(v); return onChange(); }
		public Vector mult(float f) { super.mult(f); return onChange(); }
		public Vector div(float f) { super.mult(f); return onChange(); }
		public Vector limit(float f) { super.mult(f); return onChange(); }
		public Vector setMag(float f) { super.mult(f); return onChange(); }
		public Vector normalize() { super.normalize(); return onChange(); }

		// --- On Change ---

		private Vector onChange() {
			changedCurrent=true;
			onChange.forEach(r -> r.run());
			return this;
		}
	}
	
	/** Quaternion notifying on change (after change. not on creation). */
	public static class NQuaternion extends Quaternion implements Observable {
		private ArrayList<Runnable> onChange = new ArrayList<>();
		private boolean changed, changedCurrent;

		public NQuaternion(Quaternion q, Runnable onChange) {
			super(q);
			reset();
		}
		
		public NQuaternion() {
			this(identity, null);
		}
		
		// --- From observable ---

		public Runnable addOnChange(Runnable onChange) {
			this.onChange.add(onChange);
			return onChange;
		}
		
		public void removeOnChange(Runnable onChange) {
			if (!this.onChange.remove(onChange))
				throw new IllegalArgumentException("onChange runnable wasn't to run !");
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
		
		// --- Overload ---
		
		public void set(float w, Vector xyz) { super.set(w,xyz); onChange(); }
		public void set(Vector rotAxis, float angle) { super.set(rotAxis, angle); onChange(); }
		public void set(Quaternion quat) { super.set(quat); onChange(); }
		public void setAngle(float angle) { super.setAngle(angle); onChange(); }
		public void rotate(Quaternion r) { super.rotate(r); onChange(); }

		private Quaternion onChange() {
			changedCurrent = true;
			if (onChange != null)
				onChange.forEach(r -> r.run());
			return this;
		}
	}
}
