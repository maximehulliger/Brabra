package brabra.game;

import java.util.ArrayList;

import brabra.ProMaster;
import brabra.Brabra;
import brabra.game.physic.Body;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Line.Projection;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.weapons.Weaponry;
import brabra.game.scene.Object;
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
	
	private Object focused;
	private Body focusedBody;
	private Weaponry weaponry;

	/** Set focused and force (except if force = -1). displayState. */
	public void setFocused(Object focused, float force) {
		if (force != -1)
			setForce(force);
		setFocused(focused);
	}

	public boolean hasFocused() {
		return focused != null;
	}
	
	/** Update interaction & apply forces. */
	public void update() {
		game.debug.setCurrentWork("interaction");
		//check for armement changes (in focused children)
		if (hasFocused() && focused.transform.childrenChanged()) {
			weaponry = getWeaponry();
		}
		
		// force change
		if (game.input.scrollDiff != 0) {
			
			app.para.setInteractionForce(
					PApplet.constrain(
							app.para.interactionForce() + game.input.scrollDiff*forceRange/Input.scrollRange,
					forceMin, forceMax));
		}
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
		game.debug.setCurrentWork("interaction gui");
		if (weaponry != null)
			weaponry.displayGui();
	}

	private void applyForces() {
		if (hasFocused() && focusedBody != null) {
			// 1. rotation (plate, mouse, horizontal)
			Vector forceRot = zero.copy(); // [pitch, yaw, roll]
			//> from the plate
			if (app.imgAnalyser.running()) {
				// rotation selon angle de la plaque
				Vector plateRot = app.imgAnalyser.rotation().multBy(1/Brabra.inclinaisonMax); //over 1
				// on adoucit par x -> x ^ 1.75
				plateRot = new Vector(
						PApplet.pow(PApplet.abs(plateRot.x), 1.75f) * sgn(plateRot.x), 
						PApplet.pow(PApplet.abs(plateRot.y), 1.75f) * sgn(plateRot.y),
						PApplet.pow(PApplet.abs(plateRot.z), 1.75f) * sgn(plateRot.z));
				forceRot.add( plateRot.multBy(Brabra.inclinaisonMax/4 ) );
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
				focusedBody.applyForce(focused.transform.absolute(upAP), focused.transform.absoluteDir(forceRel));
			}
			//> apply force rot: front (yaw)
			if (forceRot.y != 0) {
				Vector frontAP = front(150);
				Vector frontForceRel = right(forceRot.y);
				focusedBody.applyForce(focused.transform.absolute(frontAP), focused.transform.absoluteDir(frontForceRel));
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
	
	// --- Raycast ---

	public static Collider raycast(Vector from, Vector dir) {
		assert(!dir.equals(zero));
		Line ray = new Line(from, add(from,dir), true);
		Vector other = vec(1,1,1);
		Vector target1 = new Vector();
		Vector target2 = new Vector();
		dir.cross(other, target1);
		if (target1.equals(zero))
			other = vec(-2,-3,-5);
		dir.cross(other, target1);
		dir.cross(target1, target2);
		assert (!target1.equals(zero) && !!target2.equals(zero));
		Line p1 = new Line(from, target1, true);
		Line p2 = new Line(from, target2, true);
		Projection targetProj = new Line.Projection(0);
		ArrayList<Collider> candidates = new ArrayList<>();
		ArrayList<Float> candidatesDist = new ArrayList<>();

		for (Collider c : game.scene.activeColliders()) {
			if (c.projetteSur(p1).comprend(0)&& c.projetteSur(p2).comprend(0)) {
				Projection proj = c.projetteSur(ray);
				if (proj.intersectionne(targetProj)) {
					candidates.add(c);
					candidatesDist.add(proj.from);
				}
			}
		}

		if (candidates.size() == 0)
			return null;
		else if (candidates.size() == 1)
			return candidates.get(0);
		else {
			Collider best = null;
			float bestDe = Float.MAX_VALUE;
			for (int i=0; i<candidates.size(); i++) {
				float de = candidatesDist.get(i);
				if (bestDe > de) {
					bestDe = de;
					best = candidates.get(i);
				}
			}
			assert(best != null);
			return best;
		}
	}

	// --- private ---
	
	private Weaponry getWeaponry() {
		return focused == null ? null : (Weaponry)focused.childThat(c -> c instanceof Weaponry);
	}
	
	/** Set the force of interaction. if displayIfChange & changed, displayState. */
	private void setForce(float force) {
		if (force < forceMin) {
			game.debug.err("interaction force min is "+forceMin+" ("+force+")");
			force = forceMin;
		} else if (force > forceMax) {
			game.debug.err("interaction force max is "+forceMax+" ("+force+")");
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
	private void setFocused(Object focused) {
		this.focused = focused;
		this.focusedBody = focused instanceof Body ? (Body)focused : null;
		this.weaponry = getWeaponry();
	}
}
