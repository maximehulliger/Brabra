package cs211.tangiblegame.trivial;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Cylinder;
import cs211.tangiblegame.imageprocessing.ImageProcessing;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class TrivialGame extends ProMaster {
	//-- parametres
	final static PVector tailleTerrain = new PVector(400, 20, 300);
	private final static float tempsTransition = 0.5f;
	private final static float pasRotY = PApplet.PI/8; //le pas de rotation de l'angle y, en radian
	
	//-- interne
	private enum Mode { 
		Jeu,       
		TransUp,  	//vers contrôle
		TransDown,	//revient à jeu
		Placement   
	};
	private final ImageProcessing imgProcessing;
	final Cylinders cylinders = new Cylinders();
	private final Mover mover;
	
	private Mode mode = Mode.Jeu;
	private float etat = 0; //entre 0 (jeu) et 1 (controle)
	private boolean run = true;
	private float tiltSpeed = 1;
	PVector platRot = zero.get();
	
	public TrivialGame(ImageProcessing imgProcessing) {
		this.imgProcessing = imgProcessing;
		Cylinders.trivialGame = this;
		this.mover = new Mover(15, 1, this);
	}
	
	public void draw() {
		updateMode();
		placeCamEtLum();
		rotateScene();
		app.noStroke();


		//le terrain
		app.fill(200);
		app.pushMatrix();
		app.translate(0, -tailleTerrain.y/2, 0);
		app.fill(100, 100, 100);
		app.box(tailleTerrain.x, tailleTerrain.y, tailleTerrain.z);
		app.popMatrix();

		//une boule
		if (run && app.run)
			mover.update();
		mover.display();

		//un cylindre
		Cylinder.displayCylinders();
	}
	private static final float plateMaxAngle = PApplet.PI/6;
	void rotateScene() {
		//roation du plateau
		float ratioEtat = 1-etat; //pour forcer une rotation nulle en mode contrôle.
		platRot.x = PApplet.constrain(-imgProcessing.rotation.x, -plateMaxAngle, plateMaxAngle);
		platRot.y = 0;//PApplet.constrain(imgProcessing.rotation.y, -plateMaxAngle, plateMaxAngle);
		platRot.z = PApplet.constrain(-imgProcessing.rotation.z, -plateMaxAngle, plateMaxAngle);
		app.rotateX(platRot.x * ratioEtat);
		app.rotateY(platRot.y * ratioEtat);
		app.rotateZ(platRot.z * ratioEtat);
	}

	void placeCamEtLum()
	{ 
		//les 2 points; initial et final, de jeu et de controle.
		float jeuZ = 600, jeuY = 150;
		float contrZ = 1, contrY = 300;

		float decalageMil = 0; // le rapport entre
		// la distance entre le point de jeu et celui de controle et
		// la distance entre le point milieu et le centre de rotation.

		float milieuZ = (jeuZ+contrZ)/2, milieuY = (jeuY+contrY)/2;
		PVector pente = new PVector(0, contrZ-jeuZ, -(contrY-jeuY));
		float centreRotZ = milieuZ+decalageMil*pente.z,  centreRotY = milieuY+decalageMil*pente.y;
		float diffRotJeuZ = jeuZ-centreRotZ, diffRotJeuY = jeuY-centreRotY;
		float rayon = PApplet.sqrt(diffRotJeuZ*diffRotJeuZ+diffRotJeuY*diffRotJeuY);
		float minAngle = PApplet.acos((jeuZ-centreRotZ)/rayon);
		float maxAngle = PApplet.acos((contrZ-centreRotZ)/rayon);

		float angle = minAngle + (maxAngle-minAngle)*etat;
		float posCamZ = centreRotZ+PApplet.cos(angle)*rayon, posCamY = centreRotY+PApplet.sin(angle)*rayon;
		app.camera(0, posCamY, posCamZ, 0, 0, 0, 0, -1, 0);

		/* debug
	  println("pos cam Z,Y: "+posCamZ+", "+posCamY);
	  println("centreRot Z,Y: "+centreRotZ+", "+centreRotY);
	  println("angle sur x: "+angle+", rayon: "+rayon);*/

		//lum
		app.ambientLight(255, 255, 255);
		app.directionalLight(50, 100, 125, 0, -1, 0);

		//le bg
		app.background(200);
	}


	public void keyReleased() {
		//shift: mode contrôle
		if (app.keyCode == PApplet.SHIFT || app.keyCode == PApplet.CONTROL) {
			switch (mode) {
			case Placement:
			case TransUp:
				setMode(Mode.TransDown);
			default:
				break;
			}
		}
	}

	public void keyPressed() {
		if (app.key != PApplet.CODED)
			return; 

		//gauche droite: tourne la plaque  
		if (app.keyCode == PApplet.LEFT) {
			platRot.y = entrePiEtMoinsPi(platRot.y - pasRotY);
		}
		else if (app.keyCode == PApplet.RIGHT) {
			platRot.y = entrePiEtMoinsPi(platRot.y + pasRotY);
		}

		//shift: mode contrôle
		if (app.keyCode == PApplet.SHIFT || app.keyCode == PApplet.CONTROL) {
			switch (mode) {
			case Jeu:
			case TransDown:
				setMode(Mode.TransUp);
			default:
				break;
			}
		}
	}  

	public void mouseReleased() {
		if (mode == Mode.Placement) {
			cylinders.placeCylinder();
		}
	}

	void mouseWheel(MouseEvent event) {
		float delta = - event.getCount(); //negatif si vers l'utilisateur
		tiltSpeed = PApplet.constrain( tiltSpeed + 0.05f*delta , 0.2f, 2 );
	}

	private void updateMode() {
		switch (mode) {
		case Jeu:
		case Placement:
			break;
		case TransUp:
			etat = PApplet.constrain(etat + 1.0f/tempsTransition/app.frameRate, 0, 1);
			if (etat == 1)
				setMode(Mode.Placement);
			break;
		case TransDown:
			etat = PApplet.constrain(etat - 1.0f/tempsTransition/app.frameRate, 0, 1);
			if (etat == 0)
				setMode(Mode.Jeu);
			break;
		}
	}

	private void setMode(Mode m) {
		switch (m) {
		case Jeu:
			run = true;
			etat = 0;
			break;
		case Placement:
		case TransUp:
		case TransDown:
			run = false;
			break;
		}
		mode = m;
	}
}
