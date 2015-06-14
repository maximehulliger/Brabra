package cs211.tangiblegame.trivial;

import cs211.tangiblegame.Interface;
import cs211.tangiblegame.geo.Cylinder;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class TrivialGame extends Interface {
	//-- parametres
	final static PVector tailleTerrain = new PVector(400, 20, 300);
	private final static float tempsTransition = 0.5f;
	private final static float pasRotY = PApplet.PI/8; //le pas de rotation de l'angle y, en radian
	private static final float plateMaxAngle = PApplet.PI/6;
	
	//-- interne
	private enum Mode { 
		Jeu,       
		TransUp,  	//vers contrôle
		TransDown,	//revient à jeu
		Placement  
	};
	private final Mover mover;
	private Mode mode;
	private float etat; //entre 0 (jeu) et 1 (controle)
	private float tiltSpeed;
	Cylinders cylinders;
	PVector platRot;
	
	public TrivialGame() {
		Cylinders.trivialGame = this;
		this.mover = new Mover(15, 1, this);
		Cylinder.initCylinder();
	}
	
	public void init() {
		cylinders = new Cylinders();
		mode = Mode.Jeu;
		etat = 0; //entre 0 (jeu) et 1 (controle)
		tiltSpeed = 1;
		platRot = zero.get();
	}
	
	public void draw() {
		updateMode();
		app.imgProcessing.update();
		placeCamEtLum();
		rotateScene();
		
		//le terrain
		app.fill(200);
		app.pushMatrix();
		app.translate(0, -tailleTerrain.y/2, 0);
		app.fill(100, 100, 100);
		app.box(tailleTerrain.x, tailleTerrain.y, tailleTerrain.z);
		app.popMatrix();

		//une boule
		if (mode == Mode.Jeu)
			mover.update();
		mover.display();

		//un cylindre
		Cylinder.displayCylinders();
		
		app.camera();
		app.hint(PApplet.DISABLE_DEPTH_TEST);
		app.imgProcessing.displayCtrImg();
		app.hint(PApplet.ENABLE_DEPTH_TEST);

	}
	
	void rotateScene() {
		//roation du plateau
		float ratioEtat = 1-etat; //pour forcer une rotation nulle en mode contrôle.
		platRot.x = PApplet.constrain(-app.imgProcessing.rotation.x, -plateMaxAngle, plateMaxAngle);
		platRot.y = 0;//PApplet.constrain(imgProcessing.rotation.y, -plateMaxAngle, plateMaxAngle);
		platRot.z = PApplet.constrain(-app.imgProcessing.rotation.y, -plateMaxAngle, plateMaxAngle);
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

	public void mouseWheel(MouseEvent event) {
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
			etat = 0;
			break;
		case Placement:
		case TransUp:
		case TransDown:
			break;
		}
		mode = m;
	}
}
