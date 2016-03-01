package cs211.tangiblegame.physic;

import java.util.ArrayList;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.geo.Line;
import cs211.tangiblegame.geo.Line.Projection;
import cs211.tangiblegame.realgame.Armement;
import cs211.tangiblegame.realgame.Starship;
import processing.core.PApplet;
import processing.core.PVector;

public final class PhysicInteraction extends ProMaster {
	public float forceRatio = 5; //puissance de l'interaction

	public Body focused = null;
	public Armement armement = null;

	public boolean hasFocused() {
		return focused != null;
	}

	public void setFocused(Body focused) {
		this.focused = focused;
		armement = focused.getClass() == Starship.class ?
				((Starship)focused).armement : null;
	}

	public void update() {
		if (app.input.scrollDiff != 0) {
			forceRatio = map(app.input.scroll, 0, 1, 0.2f, 60);
			System.out.printf("ratio de force: %.2f\n",forceRatio);
		}
		
		if (focused != null)
			applyForces();

		if (armement != null) {
			armement.update();
			float leftScore = app.imgAnalyser.buttonDetection.leftScore();
			if (leftScore > 0)
				armement.fire(leftScore);
		}
	}

	public Collider raycast(PVector from, PVector dir) {
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

	private void applyForces() {
		// 1. rotation (plate, mouse, horizontal)
		PVector forceRot = zero.copy();

		if (app.imgAnalyser.running()) {
			// rotation selon angle de la plaque
			PVector plateRot = PVector.div(app.imgAnalyser.rotation(), TangibleGame.inclinaisonMax); //sur 1
			// on adoucit par x -> x ^ 1.75
			plateRot = new PVector(
					PApplet.pow(PApplet.abs(plateRot.x), 1.75f) * sgn(plateRot.x), 
					PApplet.pow(PApplet.abs(plateRot.y), 1.75f) * sgn(plateRot.y),
					PApplet.pow(PApplet.abs(plateRot.z), 1.75f) * sgn(plateRot.z));
			forceRot.add( PVector.mult(plateRot,  TangibleGame.inclinaisonMax/4 ) );
		}

		forceRot.add( up(app.input.horizontal) );
		forceRot.add( mult(app.input.deplMouse, forceRatio/100) );
		if (!forceRot.equals(zero)) {
			PVector f = PVector.mult( forceRot, forceRatio/600_000 );
			if (f.y != 0) {
				PVector yaw = right(f.y);
				PVector yawAP = front(150);
				focused.addForce(focused.absolute(yawAP), 
						absolute( yaw, zero, focused.rotation));
			}
			if (f.x != 0) {
				PVector pitch = up(f.x);
				PVector pitchAP = front(150);
				focused.addForce(focused.absolute(pitchAP), 
						absolute( pitch, zero, focused.rotation));
			}
			if (f.z != 0) {
				PVector roll = right(f.z);
				PVector rollAP = up(100);
				focused.addForce(focused.absolute(rollAP), 
						absolute( roll, zero, focused.rotation));
			}
		}

		// 2. forward
		float rightScore = app.imgAnalyser.buttonDetection.rightScore();
		if (app.input.vertical != 0 || rightScore > 0) {
			System.out.println("vertical: "+app.input.vertical);
			focused.avance((app.input.vertical+rightScore)*100*forceRatio);
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

	//----- gestion evenement

	public void keyPressed() {
		if (armement != null) {
			if (app.key == 'e')		armement.fire(1);
			if (app.key >= '1' && app.key <= '5')
				armement.fireFromSlot(app.key-'1');
		}
	}
}
