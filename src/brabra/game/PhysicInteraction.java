package brabra.game;

import java.util.ArrayList;

import brabra.ProMaster;
import brabra.Brabra;
import brabra.game.physic.Body;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Line.Projection;
import brabra.game.scene.weapons.Weaponry;
import brabra.game.scene.Object;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Class to enable interaction with the scene's bodies.
 * Reacts to user input: wasd, space, alt, mouse drag, scroll.
 */
public final class PhysicInteraction extends ProMaster {
	/** Puissance de l'interaction. */
	private static final float forceMin = 20, forceMax = 100, forceRange = forceMax - forceMin; 
	
	private float force = 40, ratioTrans = 10, ratioRot = 0.004f;
	private float forceTrans = force*ratioTrans, forceRot = force*ratioRot;
	private Body focusedBody = null;
	private Object focused = null;
	private Weaponry armement = null;

	/** Set focused and force (except if force = -1). displayState. */
	public void setFocused(Object focused, float force) {
		if (force != -1)
			setForce(force, false);
		setFocused(focused);
	}

	public void displayState() {
		if (hasFocused()) {
			String armed = armement != null ? "armed " : "";
			game.debug.info(2, "interaction focused on "+armed+"\""+focused+"\" with force = "+force);
		} else
			game.debug.info(2, "interaction not focused");
	}

	public boolean hasFocused() {
		return focused != null;
	}
	
	/** Update interaction & apply forces. */
	public void update() {
		game.debug.setCurrentWork("interaction");
		//check for armement changes (in focused children)
		if (hasFocused() && focused.childrenChanged()) {
			updateWeaponry();
		}
		
		// force change
		if (game.input.scrollDiff != 0) {
			force = PApplet.constrain(
					force + game.input.scrollDiff*forceRange/Input.scrollRange,
					forceMin, forceMax);
			game.debug.msg(1, String.format("force d'interaction: %.1f\n",force));
		}
		
		if (hasFocused() && focusedBody != null)
			applyForces();

		// fire if needed
		if (armement != null) {
			armement.update();
			if (app.imgAnalyser.running()) {
				float leftScore = app.imgAnalyser.buttonDetection.leftScore();
				if (leftScore > 0)
					armement.fire(-1, leftScore);
			}
			if (game.input.fire)
				armement.fire(game.input.fireSlot);
		}
	}
	
	public void gui() {
		game.debug.setCurrentWork("interaction gui");
		if (armement != null)
			armement.displayGui();
	}

	private void applyForces() {
		// 1. rotation (plate, mouse, horizontal)
		PVector forceRot = zero.copy(); // [pitch, yaw, roll]
		//> from the plate
		if (app.imgAnalyser.running()) {
			// rotation selon angle de la plaque
			PVector plateRot = PVector.div(app.imgAnalyser.rotation(), Brabra.inclinaisonMax); //sur 1
			// on adoucit par x -> x ^ 1.75
			plateRot = new PVector(
					PApplet.pow(PApplet.abs(plateRot.x), 1.75f) * sgn(plateRot.x), 
					PApplet.pow(PApplet.abs(plateRot.y), 1.75f) * sgn(plateRot.y),
					PApplet.pow(PApplet.abs(plateRot.z), 1.75f) * sgn(plateRot.z));
			forceRot.add( mult(plateRot,  Brabra.inclinaisonMax/4 ) );
		}
		//> add from input (horizon:ad)
		forceRot.add( yawAxis(game.input.horizontal*120) );
		//> add from mouse drag
		forceRot.add( pitchAxis(game.input.mouseDrag().y * -1f) );
		forceRot.add( rollAxis(game.input.mouseDrag().x * 1f) );
		//> apply force rot: up (pitch, roll)
		if (forceRot.x != 0 || forceRot.z != 0) {
			PVector upAP = up(100);
			PVector forceRel = zero.copy();
			forceRel.add( front(forceRot.x * this.forceRot * 3/2) );
			forceRel.add( right(forceRot.z * this.forceRot * 3/2) );
			focusedBody.applyForce(focused.absolute(upAP), focused.absoluteDir(forceRel));
		}
		//> apply force rot: front (yaw)
		if (forceRot.y != 0) {
			PVector frontAP = front(150);
			PVector frontForceRel = right(forceRot.y * this.forceRot);
			focusedBody.applyForce(focused.absolute(frontAP), focused.absoluteDir(frontForceRel));
		}

		// 2. forward
		float rightScore = max(0, app.imgAnalyser.buttonDetection.rightScore());
		if (game.input.vertical != 0 || rightScore > 0) {
			focusedBody.applyForceRel(front(150), front((game.input.vertical+rightScore) * forceTrans));
		}

		// 3. brake
		if ( rightScore > 0) {
			// if the buttons are detected
			focusedBody.freineDepl(0.1f);
			focusedBody.freineRot(0.15f);
		} else {
			// keyboard: alt -> brake, space -> non-brake
			if (game.input.altDown)
				focusedBody.freine(0.35f);
			else if (game.input.spaceDown) {
				focusedBody.freineDepl(0.001f);
				focusedBody.freineRot(0.01f);
			} else
				focusedBody.freine(0.1f);
		}
	}

	public static Collider raycast(PVector from, PVector dir) {
		assert(!dir.equals(zero));
		Line ray = new Line(from, add(from,dir), true);
		PVector other = vec(1,1,1);
		PVector target1 = new PVector();
		PVector target2 = new PVector();
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
	
	private void updateWeaponry() {
		armement = (Weaponry)focused.childThat(c -> c instanceof Weaponry);
	}
	
	/** Set the force of interaction. if displayIfChange & changed, displayState. */
	private void setForce(float force, boolean displayIfChange) {
		if (force < forceMin) {
			game.debug.err("interaction force min is "+forceMin+" ("+force+")");
			force = forceMin;
		} else if (force > forceMax) {
			game.debug.err("interaction force max is "+forceMax+" ("+force+")");
			force = forceMax;
		}
		force = PApplet.constrain(force, forceMin, forceMax);
		if (force != this.force) {
			this.force = force;
			if (displayIfChange)
				displayState();
		}
	}

	/** Set focused. displayState. */
	private void setFocused(Object focused) {
		this.focused = focused;
		this.focusedBody = focused instanceof Body ? (Body)focused : null;
		updateWeaponry();
		displayState();
	}
}
