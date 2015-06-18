package cs211.tangiblegame.realgame;

import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.geo.Cube;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.realgame.Armement;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

//une classe pouvant intervenir dans une collision. ne réagit pas.
public class Starship extends Cube
{
	public static final float sizeFactor = 15f;
	private static final PVector size = PVector.mult( vec(4, 2, 8), sizeFactor); //for the collider
	public static final boolean displaySkybox = false;
	private static final boolean displayViseur = true;
	public float forceRatio = 15; //puissance du vaisseau
	public boolean hasCamera = true;
	
	private float forceDepl = 0; // [0,1] si de la plaque, -1.1 ou 1.1 si du clavier. fait avancer ou reculer le vaisseau
	private PVector forceRot = zero.get();
	MeteorSpawner champ;
	Armement armement;
	public static PShape skybox;
	public static PShape starship;
	
	public Starship(PVector location) {
		super(location, new Quaternion(), 200, size);
		PVector champSize = vec(10_000, 10_000, 15_000);
		this.champ = new MeteorSpawner(this, vec(0, 0, -champSize.z/6), champSize);
		this.armement = new Armement(this, 0, 1, 1);
	}
	
	protected void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0) {
			float fact = mass*(sq(size.x) + sq(size.y) + sq(size.z))/7;
			float invFact = 1/fact;
			super.inertiaMom = new PVector(fact, fact, fact);
			super.inverseInertiaMom = new PVector(invFact, invFact, invFact);
		}
	}
	
	public void update() {
		super.update();
		
		//champ.update(); TODO
		armement.update();
		
		app.imgAnalyser.buttonDetection.lock();
		if (app.imgAnalyser.buttonDetection.leftVisible) {
			armement.fire(app.imgAnalyser.buttonDetection.leftScore);
		}
		app.imgAnalyser.buttonDetection.unlock();
		
	}

	public void display() {
		app.noStroke();
		
		//1. la camera
		if (hasCamera) {
			PVector relCamPos = PVector.mult(new PVector(0, 6, 9), sizeFactor);
			PVector camPos = absolute(relCamPos);
			PVector or = PVector.mult(faces[3].normale.norm, 1);
			PVector focus = PVector.add(location, absUp(4 * sizeFactor));
			app.camera(camPos.x, camPos.y, camPos.z, focus.x, focus.y, focus.z, or.x, or.y, or.z);
		}
		
		//2. le vaisseau (corp, tête, viseur)
		app.pushMatrix();
		translate(location);
		if (displaySkybox)
			app.shape(skybox);
		app.pushMatrix();
		rotate(rotation);
		app.fill(50, 100, 125);
		//app.box(size.x, size.y, size.z);
		if (displayViseur) {
			//PVector to = PVector.mult(front, 10000);
			app.stroke(255, 0, 0, 50);
			app.line(0, 0, 0, 0, 0, -100000);
			app.noStroke();
		}
		app.shape(starship);
		//app.translate(0, dim[1], dim[2]/2);
		//app.sphere(dim[0]/2);
		popLocal();
		
	}
	
	protected void addForces() {
		
		//-- rotation - selon angle de la plaque
		PVector plateRot = app.imgAnalyser.rotation();
		/*plateRot = new PVector( //TODO
				PApplet.pow(plateRot.x/TangibleGame.inclinaisonMax, 0.875f), //-> x ^ 1.75
				PApplet.pow(plateRot.y/TangibleGame.inclinaisonMax, 0.875f),
				PApplet.pow(plateRot.z/TangibleGame.inclinaisonMax, 0.875f) );*/
		
		forceRot.add( PVector.mult(plateRot, 2) );
		PVector f = PVector.mult( forceRot, forceRatio );
		addForce(absolute(vec(0, 0, -150)), absolute( new PVector(-f.y , f.x), zero, rotation));
		addForce(absolute(vec(0, 50, 0)), absolute( new PVector(-f.z*3 , 0), zero, rotation));
		if ( PApplet.abs(forceRot.x) != 1.1f ) forceRot.x = 0;
		if ( PApplet.abs(forceRot.y) != 1.1f ) forceRot.y = 0;
		forceRot.z = 0;

		
		//-- déplacement - selon le bouton droite
		app.imgAnalyser.buttonStateLock.lock();
		float rightScore = app.imgAnalyser.buttonDetection.rightScore;
		app.imgAnalyser.buttonStateLock.unlock();
		
		if (forceDepl != 0 || rightScore > 0) {
			avance((forceDepl+rightScore)*forceRatio);
		}
			
			
		//si pas visible, on freine
		if ( rightScore == 0 ) {
			freine(0.15f);
		} else {
			freineDepl(0.005f);
			freineRot(0.05f);
		}
	}
	
	public void mouseDragged() {
		int diffX = app.mouseX-app.pmouseX;
		int diffY = app.mouseY-app.pmouseY;
		forceRot.add( new PVector(-diffY*forceRatio/50, diffX*forceRatio/50) );
		//float ratio = forceRatio / 10;
		//controlForce.add( new PVector(diffY*ratio, 0, diffX*ratio) );
	}
	
	public void keyPressed() {
		if (app.key == ' ')			forceDepl = 1.1f;
		else if  (app.key == 'b')	forceDepl = -1.1f;
			
		if (app.key == 'w') 		forceRot.x = 1.1f;
		else if (app.key == 's') 	forceRot.x = -1.1f;
		if (app.key == 'a')			forceRot.y = -1.1f;
		else if (app.key == 'd')	forceRot.y = 1.1f;
		
		if (app.key == 'e')	armement.fire(1);
		if (app.key >= '1' && app.key <= '5')
			armement.fireFromSlot(app.key-'1');
	}
	
	public void keyReleased() {
		if (app.key == ' ' && forceDepl > 0)		forceDepl = 0;
		else if  (app.key == 'b' && forceDepl < 0)	forceDepl = 0;
		
		if (app.key == 'w' && forceRot.x > 0) 		forceRot.x = 0;
		else if (app.key == 's' && forceRot.x < 0) 	forceRot.x = 0;
		if (app.key == 'a' && forceRot.y < 0) 		forceRot.y = 0;
		else if (app.key == 'd' && forceRot.y > 0) 	forceRot.y = 0;
	}
}