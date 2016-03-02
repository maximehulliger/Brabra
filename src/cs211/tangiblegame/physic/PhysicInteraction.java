package cs211.tangiblegame.physic;

import java.util.ArrayList;

import cs211.tangiblegame.Input;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.geo.Line;
import cs211.tangiblegame.geo.Line.Projection;
import cs211.tangiblegame.realgame.Armement;
import cs211.tangiblegame.realgame.Starship;
import processing.core.PApplet;
import processing.core.PVector;

public final class PhysicInteraction extends ProMaster {
	/** Puissance de l'interaction. */
	private static final float forceMin = 20, forceMax = 100, forceRange = forceMax - forceMin; 
	
	private float force = 40;
	private Body focused = null;
	private Armement armement = null;

	public boolean hasFocused() {
		return focused != null;
	}

	public void setFocused(Body focused) {
		this.focused = focused;
		armement = focused.getClass() == Starship.class ?
				((Starship)focused).armement : null;
	}
	
	public void setForce(float force) {
		this.force = PApplet.constrain(force, forceMin, forceMax);
		System.out.printf("force d'interaction: %.1f\n",force);
	}
	
	public void update() {
		// force change
		if (app.input.scrollDiff != 0) {
			force = PApplet.constrain(
					force + app.input.scrollDiff*forceRange/Input.scrollRange,
					forceMin, forceMax);
			System.out.printf("force d'interaction: %.1f\n",force);
		}
		
		if (focused != null)
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
		if (armement != null)
			armement.displayGui();
	}

	private void applyForces() {
		// 1. rotation (plate, mouse, horizontal)
		PVector forceRot = zero.copy(); // [pitch, yaw, roll]

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

		forceRot.add( front(-app.input.horizontal*force) );
		forceRot.add( vec(-app.input.deplMouse.y*force*0.1f, app.input.deplMouse.x*force*0.1f) );
		if (!forceRot.equals(zero)) {
			PVector f = PVector.mult( forceRot, force/app.frameRate );
			PVector frontAP = front(150);
			if (f.x != 0) {
				PVector pitch = up(f.x);
				focused.addForce(focused.absolute(frontAP), 
						absolute( pitch, zero, focused.rotation));
			}
			if (f.y != 0) {
				PVector yaw = right(f.y);
				focused.addForce(focused.absolute(frontAP), 
						absolute( yaw, zero, focused.rotation));
			}
			if (f.z != 0) {
				PVector roll = right(f.z*3/2);
				PVector rollAP = up(100);
				focused.addForce(focused.absolute(rollAP), 
						absolute( roll, zero, focused.rotation));
			}
		}

		// 2. forward
		float rightScore = max(0, app.imgAnalyser.buttonDetection.rightScore());
		if (app.input.vertical != 0 || rightScore > 0) {
			focused.avance((app.input.vertical+rightScore)*force*300/app.frameRate);
		}

		// 3. brake
		if ( rightScore == 0) {
			// space -> non-brake
			if (app.input.spaceDown) {
				focused.freineDepl(0.001f);
				focused.freineRot(0.1f);
			} else
				focused.freine(0.15f);
		} else {
			focused.freineDepl(0.1f);
			focused.freineRot(0.15f);
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

		for (Collider c : game.physic.colliders) {
			if (c.projetteSur(p1).comprend(0)&& c.projetteSur(p2).comprend(0)) {
				Projection proj = c.projetteSur(ray);
				if (proj.intersectionne(targetProj)) {
					candidates.add(c);
					candidatesDist.add(proj.de);
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
