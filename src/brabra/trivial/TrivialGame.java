package brabra.trivial;

import brabra.Interface;
import brabra.Brabra;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class TrivialGame extends Interface {
	//-- parametres
	final static PVector tailleTerrain = new PVector(600, 20, 600);
	private final static float tempsTransition = 0.5f;
	private final static float pasRotY = PApplet.PI/8; //le pas de rotation de l'angle y, en radian
	private static final float ratioUpdate = 0.05f; // rapprochement de la rot. de la plaque vers la rot. de l'image chaque frame
	
	//-- interne
	private enum Mode { 
		Jeu,       
		TransUp,  	//vers contrôle
		TransDown,	//revient à jeu
		Placement  
	};
	private Mover mover;
	private Mode mode;
	private float etat; //entre 0 (jeu) et 1 (controle)
	private float tiltSpeed;
	Cylinders cylinders;
	PVector platRot;
	
	public TrivialGame() {
		Cylinders.trivialGame = this;
	}
	
	public void onShow() {
		app.imgAnalyser.play(false, false);
		mover = new Mover(this);
		cylinders = new Cylinders();
		tiltSpeed = 1;
		platRot = zero.copy();
		mode = Mode.Jeu;
		etat = 0; //entre 0 (jeu) et 1 (controle)
	}

	public void onHide() {
		app.setImgAnalysis(false);
	}
	
	public void draw() {
		updateMode();
		placeCamEtLum();
		
		app.noStroke();
		app.fill(100, 100, 100);
		
		rotateScene();
		
		//le terrain
		app.pushMatrix();
		app.translate(0, -tailleTerrain.y/2, 0);
		app.box(tailleTerrain.x, tailleTerrain.y, tailleTerrain.z);
		app.popMatrix();

		//une boule
		if (mode == Mode.Jeu || mode == Mode.TransDown)
			mover.update();
		mover.display();
		cylinders.displayCylinders();
	}
	
	void rotateScene() {
		//roation du plateau
		float ratioEtat = 1-etat; //pour forcer une rotation nulle en mode contrôle.
		PVector gameRotation = app.imgAnalyser.rotation();
		
		platRot.x = PApplet.constrain((platRot.x + ratioUpdate * (gameRotation.x - platRot.x)), -Brabra.inclinaisonMax, Brabra.inclinaisonMax);
		platRot.y = 0;//PApplet.constrain(imgAnalyser.rotation.y, -plateMaxAngle, plateMaxAngle);
		platRot.z = PApplet.constrain((platRot.z + ratioUpdate * (gameRotation.y - platRot.z)), -Brabra.inclinaisonMax, Brabra.inclinaisonMax);
		app.rotateX(platRot.x * ratioEtat);
		app.rotateY(platRot.y * ratioEtat);
		app.rotateZ(platRot.z * ratioEtat);
	}
	
	public void gui() {
		app.imgAnalyser.gui();
	}

	void placeCamEtLum()
	{ 
		//les 2 points; initial et final, de jeu et de controle.
		float jeuZ = 600, jeuY = 250;
		float contrZ = 1, contrY = 600;

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

		//lum
		app.ambientLight(255, 255, 255);
		app.directionalLight(50, 100, 125, 0, -1, 0);

		//le bg
		app.background(200);
	}


	public void keyReleased() {
		//shift: mode contr�le
		if (app.keyCode == PApplet.SHIFT || app.keyCode == PApplet.CONTROL) {
			app.imgAnalyser.play(false, false);
			
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
		//gauche droite: tourne la plaque  
		if (app.keyCode == PApplet.LEFT) {
			platRot.y = entrePiEtMoinsPi(platRot.y - pasRotY);
		}
		else if (app.keyCode == PApplet.RIGHT) {
			platRot.y = entrePiEtMoinsPi(platRot.y + pasRotY);
		}

		//shift: mode contr�le
		if (app.keyCode == PApplet.SHIFT || app.keyCode == PApplet.CONTROL) {
			switch (mode) {
			case Jeu:
			case TransDown:
				app.imgAnalyser.stop();
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
			etat = PApplet.constrain(etat + 1.0f/tempsTransition/Brabra.frameRate, 0, 1);
			if (etat == 1)
				setMode(Mode.Placement);
			break;
		case TransDown:
			etat = PApplet.constrain(etat - 1.0f/tempsTransition/Brabra.frameRate, 0, 1);
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
