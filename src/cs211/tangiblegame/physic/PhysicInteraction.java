package cs211.tangiblegame.physic;

import java.util.ArrayList;

import cs211.tangiblegame.Input;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.geo.Line;
import cs211.tangiblegame.geo.Line.Projection;
import cs211.tangiblegame.realgame.Weaponry;
import cs211.tangiblegame.realgame.Weaponry.Armed;
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
	private Body focused = null;
	private Weaponry armement = null;

	/** Set focused. displayState. */
	public void setFocused(Body focused) {
		this.focused = focused;
		armement = focused instanceof Armed ?
				((Armed)focused).armement() : null;
		displayState();
	}
	
	/** Set focused and force. displayState. */
	public void setFocused(Body focused, float force) {
		this.focused = focused;
		armement = focused instanceof Armed ?
				((Armed)focused).armement() : null;
		setForce(force, false);
		displayState();
	}
	
	/** Set the force of interaction. if displayIfChange & changed, displayState. */
	public void setForce(float force, boolean displayIfChange) {
		force = PApplet.constrain(force, forceMin, forceMax);
		if (force != this.force) {
			this.force = force;
			if (displayIfChange)
				displayState();
		}
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
		
		// force change
		if (app.input.scrollDiff != 0) {
			force = PApplet.constrain(
					force + app.input.scrollDiff*forceRange/Input.scrollRange,
					forceMin, forceMax);
			game.debug.msg(1, String.format("force d'interaction: %.1f\n",force));
		}
		
		if (hasFocused() && game.physic.running)
			applyForces();

		// fire if needed
		if (armement != null) {
			armement.update();
			if (app.imgAnalyser.running()) {
				float leftScore = app.imgAnalyser.buttonDetection.leftScore();
				if (leftScore > 0)
					armement.fire(-1, leftScore);
			}
			if (app.input.fire)
				armement.fire(app.input.fireSlot);
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
			PVector plateRot = PVector.div(app.imgAnalyser.rotation(), TangibleGame.inclinaisonMax); //sur 1
			// on adoucit par x -> x ^ 1.75
			plateRot = new PVector(
					PApplet.pow(PApplet.abs(plateRot.x), 1.75f) * sgn(plateRot.x), 
					PApplet.pow(PApplet.abs(plateRot.y), 1.75f) * sgn(plateRot.y),
					PApplet.pow(PApplet.abs(plateRot.z), 1.75f) * sgn(plateRot.z));
			forceRot.add( mult(plateRot,  TangibleGame.inclinaisonMax/4 ) );
		}
		//> add from input (horizon:ad)
		forceRot.add( yawAxis(app.input.horizontal*120) );
		//> add from mouse drag
		forceRot.add( pitchAxis(app.input.mouseDrag().y * -1f) );
		forceRot.add( rollAxis(app.input.mouseDrag().x * 1f) );
		//> apply force rot: up (pitch, roll)
		if (forceRot.x != 0 || forceRot.z != 0) {
			PVector upAP = up(100);
			PVector forceRel = zero.copy();
			forceRel.add( front(forceRot.x * this.forceRot * 3/2) );
			forceRel.add( right(forceRot.z * this.forceRot * 3/2) );
			focused.applyForce(focused.absolute(upAP), focused.absoluteDir(forceRel));
		}
		//> apply force rot: front (yaw)
		if (forceRot.y != 0) {
			PVector frontAP = front(150);
			PVector frontForceRel = right(forceRot.y * this.forceRot);
			focused.applyForce(focused.absolute(frontAP), focused.absoluteDir(frontForceRel));
		}

		// 2. forward
		float rightScore = max(0, app.imgAnalyser.buttonDetection.rightScore());
		if (app.input.vertical != 0 || rightScore > 0) {
			focused.applyForceRel(front(150), front((app.input.vertical+rightScore) * forceTrans));
		}

		// 3. brake
		if ( rightScore > 0) {
			// if the buttons are detected
			focused.freineDepl(0.1f);
			focused.freineRot(0.15f);
		} else {
			// keyboard: alt -> brake, space -> non-brake
			if (app.input.altDown)
				focused.freine(0.35f);
			else if (app.input.spaceDown) {
				focused.freineDepl(0.001f);
				focused.freineRot(0.01f);
			} else
				focused.freine(0.1f);
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

		for (Collider c : game.physic.activeColliders()) {
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
}
