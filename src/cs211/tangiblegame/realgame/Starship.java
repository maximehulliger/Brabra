package cs211.tangiblegame.realgame;

import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.geo.Plane;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.realgame.Armement;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

//une classe pouvant intervenir dans une collision. ne réagit pas.
public class Starship extends Plane//Cube
{
	public static final float distSqBeforeRemove = 12_000*12_000; //distance du vaisseau avant remove
	public static final float sizeFactor = 15f;
	private static final PVector size = PVector.mult( vec(7, 2, 8), sizeFactor); //for the collider
	private static final boolean displayViseur = true;
	public float forceRatio = 15; //puissance du vaisseau
	
	MeteorSpawner champ;
	Armement armement;
	public static PShape starship;
	
	public Starship(PVector location) {
		super(location, new Quaternion(), 200, size);
		PVector champSize = vec(5000, 5000, 8000);
		this.champ = new MeteorSpawner(this, vec(0, 0, -champSize.z/6), champSize);
		this.armement = new Armement(this, 0, 1, 1);
		setName("Starship");
	}
	
	public void setMass(float mass) {
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
		
		//champ.update();
		armement.update();
		
		app.imgAnalyser.buttonStateLock.lock();
		if (app.imgAnalyser.leftButtonVisible) {
			armement.fire(app.imgAnalyser.leftButtonScore);
		}
		app.imgAnalyser.buttonStateLock.unlock();
		
	}

	public void display() {
		app.noStroke();
		//2. le vaisseau (+viseur)
		app.pushMatrix();
		translate( location );
		app.pushMatrix();
		rotate(rotation);
		app.fill(50, 100, 125);
		if (displayViseur) {
			app.stroke(255, 0, 0, 150);
			app.line(0, -1, 0, 0, -1, -100000);
			app.noStroke();
		}
		translate( vec(0, -10, 20) );
		app.shape(starship);
		popLocal();
		if (drawCollider) {
			app.fill(255, 0, 0, 100);
			super.display();
		}
	}
	
	protected void addForces() {
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
		
		PVector f = PVector.mult( forceRot, forceRatio/(sizeFactor*sizeFactor*300) );	
 		addForce(absolute(vec(0, 0, -150)), absolute( new PVector(f.y*inertiaMom.y*2/3, f.x*inertiaMom.x), zero, rotation));
		addForce(absolute(vec(0, 100, 0)), absolute( new PVector(-f.z*inertiaMom.z , 0), zero, rotation));
		
		//-- déplacement - selon ws et le bouton droite
		float forceDepl = 0;
		if (keyDownAvance)	forceDepl += 1;
		if (keyDownRecule)	forceDepl -= 1;
		
		app.imgAnalyser.buttonStateLock.lock();
		float rightScore = app.imgAnalyser.rightButtonScore;
		app.imgAnalyser.buttonStateLock.unlock();
		
		if (forceDepl != 0 || rightScore > 0) {
			avance((forceDepl+rightScore)*100*forceRatio);
		}
			
			
		//-- si pas visible et pas debrayé (espace -> non-frein), on freine
		if ( rightScore == 0) {
			if (debraie) {
				freineDepl(0.001f);
				freineRot(0.1f);
			} else
				freine(0.15f);
		} else {
			freineDepl(0.1f);
			freineRot(0.15f);
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
		if (app.key == 'e')		armement.fire(1);
		if (app.key >= '1' && app.key <= '5')
			armement.fireFromSlot(app.key-'1');
	}
	
	public void keyReleased() {
		if (app.key == ' ')		debraie = false;
		if (app.key == 'w') 	keyDownAvance = false;
		if (app.key == 's') 	keyDownRecule = false;
		if (app.key == 'a')		keyDownTourneGauche = false;
		if (app.key == 'd')		keyDownTourneDroite = false;
	}
}