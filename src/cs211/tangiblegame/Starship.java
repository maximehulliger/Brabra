package cs211.tangiblegame;

import cs211.tangiblegame.Missile.Armement;
import cs211.tangiblegame.geo.Cube;
import cs211.tangiblegame.imageprocessing.ImageProcessing;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

//une classe pouvant intervenir dans une collision. ne réagit pas.
public class Starship extends Cube
{
	public static final PVector size = new PVector(60, 30, 120);
	public static final boolean displaySkybox = true;
	private static final boolean displayViseur = true;
	public float forceRatio = 15; //puissance du vaisseau
	public boolean hasCamera = true;
	private final ImageProcessing imgProcessing;
	
	private PVector forceRot = zero.get();
	private PVector forceDepl = zero.get();
	private boolean freine = false;
	MeteorSpawner champ;
	Armement armement;
	public static PShape skybox;
	public static PShape starship;
	
	public Starship(PVector location, ImageProcessing imgProcessing) {
		super(location, zero, 200, size);
		this.imgProcessing = imgProcessing;
		PVector champSize = vec(10_000, 10_000, 15_000);
		this.champ = new MeteorSpawner(vec(0, 0, -champSize.z/6), champSize);
		this.armement = new Armement(this);
		this.champ.parent = this;
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
		
		if (transformChanged) {
			PVector pRot = new PVector(rotation.x, 0, rotation.z);
			if (pRot.mag() > PApplet.HALF_PI)
				pRot.setMag( PApplet.HALF_PI );
			rotation.x = pRot.x;
			rotation.z = pRot.z;

		}
		
		champ.update();
		armement.update();
	}

	public void display() {
		app.noStroke();
		
		//1. la camera
		if (hasCamera) {
			PVector relCamPos = new PVector(0, 75, 130);
			PVector camPos = absolute(relCamPos);
			PVector or = PVector.mult(faces[3].normale.norm, 1);
			app.camera(camPos.x, camPos.y, camPos.z, location.x, location.y, location.z, or.x, or.y, or.z);
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
		if (!forceRot.equals(zero)) {
			addForce(absolute(vec(0, 30, 0)), absolute(forceRot, zero, rotation));
			//angularVelocity.add(controlForce);
			forceRot = zero.get();
		}
		if (!forceDepl.equals(zero)) {
			avance(forceDepl.z*forceRatio*4);
			rotation.y -= forceDepl.x/50;
			//addForce(absolute(vec(0, 0, -50)), absolute( PVector.mult(forceDepl, forceRatio), zero, rotation));
		}
		if (freine) {
			freine(0.15f);
		} else {
			freine(0.05f);
		}
	}
	
	public void mouseDragged() {
		int diffX = app.mouseX-app.pmouseX;
		int diffY = app.mouseY-app.pmouseY;
		forceRot.add( new PVector(-diffX*forceRatio/10, 0, diffY*forceRatio/10) );
		//float ratio = forceRatio / 10;
		//controlForce.add( new PVector(diffY*ratio, 0, diffX*ratio) );
	}
	
	public void keyPressed() {
		if (app.key == ' ')			freine = true;
			
		if (app.key == 'w') 		forceDepl.z = 1;
		else if (app.key == 's') 	forceDepl.z = -1;
		if (app.key == 'a')			forceDepl.x = 1;
		else if (app.key == 'd')	forceDepl.x = -1;
		
		if (app.key == 'e')	armement.tire();
		if (app.key >= '1' && app.key <= '4')
			armement.tire(app.key-'1');
	}
	
	public void keyReleased() {
		if (app.key == ' ')			freine = false;
		
		if (app.key == 'w' && forceDepl.z == 1) 		forceDepl.z = 0;
		else if (app.key == 's' && forceDepl.z == -1) 	forceDepl.z = 0;
		if (app.key == 'a' && forceDepl.x == 1) 		forceDepl.x = 0;
		else if (app.key == 'd' && forceDepl.x == -1) 	forceDepl.x = 0;
	}
}