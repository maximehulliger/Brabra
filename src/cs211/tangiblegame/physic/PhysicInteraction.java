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
import processing.event.MouseEvent;

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
		if (focused != null)
			applyForces();
		
		if (armement != null) {
			armement.update();
			app.imgAnalyser.buttonStateLock.lock();
			if (app.imgAnalyser.leftButtonVisible) {
				armement.fire(app.imgAnalyser.leftButtonScore);
			}
			app.imgAnalyser.buttonStateLock.unlock();
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
		PVector forceRot = zero.copy();
		
		//-- rotation - selon angle de la plaque et sd + souris
		PVector plateRot = PVector.div(app.imgAnalyser.rotation(), TangibleGame.inclinaisonMax); //sur 1
		// on adoucit par x -> x ^ 1.75
		plateRot = new PVector(
				PApplet.pow(PApplet.abs(plateRot.x), 1.75f) * sgn(plateRot.x), 
				PApplet.pow(PApplet.abs(plateRot.y), 1.75f) * sgn(plateRot.y),
				PApplet.pow(PApplet.abs(plateRot.z), 1.75f) * sgn(plateRot.z));
		forceRot.add( PVector.mult(plateRot,  TangibleGame.inclinaisonMax/4 ) );
		
		forceRot.add( PVector.div(forceMouse, 3));
		forceMouse.set( zero );
		if (keyDownTourneGauche)	forceRot.z -= 1;
		if (keyDownTourneDroite)	forceRot.z += 1;
		
		PVector f = PVector.mult( forceRot, forceRatio/600_000 );	
		focused.addForce(focused.absolute(vec(0, 0, -150)), absolute( new PVector(f.y*focused.inertiaMom().y*2/3, f.x*focused.inertiaMom().x), zero, focused.rotation));
		focused.addForce(focused.absolute(vec(0, 100, 0)), absolute( new PVector(-f.z*focused.inertiaMom().z , 0), zero, focused.rotation));
		
		//-- déplacement - selon ws et le bouton droite
		float forceDepl = 0;
		if (keyDownAvance)	forceDepl += 1;
		if (keyDownRecule)	forceDepl -= 1;
		
		app.imgAnalyser.buttonStateLock.lock();
		float rightScore = app.imgAnalyser.rightButtonScore;
		app.imgAnalyser.buttonStateLock.unlock();
		
		if (forceDepl != 0 || rightScore > 0) {
			focused.avance((forceDepl+rightScore)*100*forceRatio);
		}
			
		//-- si pas visible et pas debrayé (espace -> non-frein), on freine
		if ( rightScore == 0) {
			if (debraie) {
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
	
	private PVector forceMouse = zero.copy();
	private boolean debraie = false;
	private boolean keyDownAvance = false;
	private boolean keyDownRecule = false;
	private boolean keyDownTourneGauche = false;
	private boolean keyDownTourneDroite = false;
	
	public void mouseDragged() {
		int diffX = app.mouseX-app.pmouseX;
		int diffY = app.mouseY-app.pmouseY;
		forceMouse.add( new PVector(-diffY*forceRatio/50, -diffX*forceRatio/50) );
	}
	
	public void keyPressed() {
		if (app.key == ' ')		debraie = true;
		if (app.key == 'w') 	keyDownAvance = true;
		if (app.key == 's') 	keyDownRecule = true;
		if (app.key == 'a')		keyDownTourneGauche = true;
		if (app.key == 'd')		keyDownTourneDroite = true;
		
		if (armement != null) {
			if (app.key == 'e')		armement.fire(1);
			if (app.key >= '1' && app.key <= '5')
				armement.fireFromSlot(app.key-'1');
		}
	}
	
	public void keyReleased() {
		if (app.key == ' ')		debraie = false;
		if (app.key == 'w') 	keyDownAvance = false;
		if (app.key == 's') 	keyDownRecule = false;
		if (app.key == 'a')		keyDownTourneGauche = false;
		if (app.key == 'd')		keyDownTourneDroite = false;
	}
	
	public void mouseWheel(MouseEvent event) {
		float delta = - event.getCount(); //delta negatif si vers l'utilisateur
		forceRatio = PApplet.constrain( forceRatio + 0.05f*delta , 0.2f, 60 );
		System.out.printf("ratio de force: %.2f\n",forceRatio);
	}
}
