package brabra.game;

import java.util.ArrayList;
import java.util.List;

import brabra.Brabra;
import brabra.Debug;
import brabra.ProMaster;
import brabra.game.physic.Body;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.Object;
import brabra.game.scene.weapons.Weaponry;
import processing.core.PApplet;

/**
 * Class to enable interaction with the scene's bodies.
 * Reacts to user input: wasd, space, alt, mouse drag, scroll.
 */
public final class PhysicInteraction extends ProMaster {
	/** Puissance of the interaction. */
	private static final float forceMin = 20, forceMax = 150, forceRange = forceMax - forceMin; 
	
	private float ratioTrans = 10, ratioRot = 0.004f;
	private float forceTrans = app.para.interactionForce()*ratioTrans, forceRot = app.para.interactionForce()*ratioRot;
	
	private Object focused = null;
	private Body focusedBody = null;
	private Weaponry weaponry = null;
	
	private List<Weaponry> weaponries = new ArrayList<>();
	
	public void addWeaponry(Weaponry w) {
		weaponries.add(w);
	}

	/** Set the force of interaction. if displayIfChange & changed, displayState. */
	public void setForce(float force) {
		if (force < forceMin) {
			Debug.err("interaction force min is "+forceMin+" ("+force+")");
			force = forceMin;
		} else if (force > forceMax) {
			Debug.err("interaction force max is "+forceMax+" ("+force+")");
			force = forceMax;
		}
		force = PApplet.constrain(force, forceMin, forceMax);
		if (force != app.para.interactionForce()) {
			app.para.setInteractionForce(force);
			this.forceTrans = force*ratioTrans;
			this.forceRot = force*ratioRot;
		}
	}

	/** Set focused. displayState. */
	public void setFocused(Object focused) {
		if (focused == null) {
			this.focused = this.focusedBody = null;
			this.weaponry = null;
		} else {
			this.focused = focused;
			this.focusedBody = focused.as(Body.class);
			this.weaponry = getWeaponry();
		}
	}
	
	public Object focused() {
		return focused;
	}
	
	public void updateWeaponry() {
		this.weaponry = getWeaponry();
	}
	
	/** Update interaction & apply forces. */
	public void update() {
		
		// force change
		if (game.input.scrollDiff != 0)
			app.para.setInteractionForce(
					PApplet.constrain(
							app.para.interactionForce() + game.input.scrollDiff*forceRange/Input.scrollRange,
							forceMin, forceMax));
		
		// apply forces to focused
		applyForces();
		
		// fire if needed
		if (weaponry != null) {
			if (app.imgAnalyser.running()) {
				float leftScore = app.imgAnalyser.buttonDetection.leftScore();
				if (leftScore > 0)
					weaponry.fire(-1, leftScore);
			}
			if (game.input.fire || game.input.fireDown)
				weaponry.fire(game.input.fireSlot);
		}
	}
	
	public void gui() {
		if (weaponry != null)
			weaponry.displayGui();
	}

	private void applyForces() {
		if (focusedBody != null) {
			// 1. rotation (plate, mouse, horizontal)
			Vector forceRot = zero.copy(); // [pitch, yaw, roll]
			//> from the plate
			if (app.imgAnalyser.running()) {
				// rotation selon angle de la plaque
				Vector plateRot = app.imgAnalyser.rotation();
				if (!plateRot.equals(zero)) {
					plateRot.mult(1/Brabra.inclinaisonMax); //over 1
					// on adoucit par x -> x ^ 1.75
					plateRot = new Vector(
							-PApplet.pow(PApplet.abs(plateRot.x), 1.75f) * sgn(plateRot.x), 
							-PApplet.pow(PApplet.abs(plateRot.y), 1.75f) * sgn(plateRot.y),
							PApplet.pow(PApplet.abs(plateRot.z), 1.75f) * sgn(plateRot.z));
					forceRot.add( plateRot.multBy(Brabra.inclinaisonMax*100 ) );
				}
			}
			//> add from input (horizon:ad)
			forceRot.add( yawAxis(game.input.horizontal*120) );
			//> add from mouse drag
			forceRot.add( pitchAxis(game.input.mouseDrag().y * -1f) );
			forceRot.add( rollAxis(game.input.mouseDrag().x * 1f) );
			
			forceRot.mult(this.forceRot);
			
			//> apply force rot: up (pitch, roll)
			if (forceRot.x != 0 || forceRot.z != 0) {
				Vector upAP = up(100);
				Vector forceRel = zero.copy();
				forceRel.add( front(forceRot.x * 3/2) );
				forceRel.add( right(forceRot.z * 3/2) );
				focusedBody.applyForce(focused.absolute(upAP), focused.absoluteDir(forceRel));
				//focusedBody.applyForceRel(upAP, forceRel);
			}
			//> apply force rot: front (yaw)
			if (forceRot.y != 0) {
				Vector frontAP = front(150);
				Vector frontForceRel = right(forceRot.y);
				focusedBody.applyForce(focused.absolute(frontAP), focused.absoluteDir(frontForceRel));
				//focusedBody.applyForceRel(frontAP, frontForceRel);
			}
			
			// 2. forward
			float rightScore = max(0, app.imgAnalyser.buttonDetection.rightScore());
			if (game.input.vertical != 0 || rightScore > 0)
				focusedBody.applyForceRel(front(150), front((game.input.vertical+rightScore) * forceTrans));
			
			// 3. brake
			if (app.para.braking()) {
				if (rightScore > 0) {
					// if the buttons are detected
					focusedBody.brakeDepl(0.1f);
					focusedBody.brakeRot(0.15f);
				} else {
					// keyboard: alt -> brake, space -> non-brake
					if (game.input.altDown)
						focusedBody.brake(0.35f);
					else if (game.input.spaceDown) {
						focusedBody.brakeDepl(0.001f);
						focusedBody.brakeRot(0.01f);
					} else
						focusedBody.brake(0.1f);
				}
			}
		}
	}
	
	// --- private ---
	
	private Weaponry getWeaponry() {
		for (Weaponry w : weaponries) {
			if (w.parent() == focused)
				return w;
		};
		return null;
	}
}
