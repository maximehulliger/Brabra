package cs211.tangiblegame;

import cs211.tangiblegame.Missile.LanceMissile;
import cs211.tangiblegame.geo.*;
import cs211.tangiblegame.imageprocessing.ImageProcessing;
import cs211.tangiblegame.physic.*;
import processing.core.*;
import processing.event.MouseEvent;


public class TangibleGame extends PApplet {
	private static final long serialVersionUID = 338280650599573653L;

	//--parametres
	private static final int ratioSize = 4; //généralement de 2 (640x360) à 5 (1920x1080)
	//private static final float pasRotY = PI/8; //le pas de rotation du plateau sur y, en radian
	private static final float tempsTransition = 0.5f;
	private static final boolean drawAxis = false;
	
	//--interne
	private enum Mode {Jeu, TransUp, TransDown, Placement}; //mode: up->vers contrôle
	private Mode mode;
	public float etat; //entre 0 (jeu) et 1 (Placement)
	private boolean run;
	public Physic physic;
	private Starship starship;
	private ImageProcessing imgProcessing;

	//----- setup et boucle d'update (draw)

	public void setup() {
		ProMaster.init(this);
		size(16*20*ratioSize, 9*20*ratioSize, P3D);
		Cylinder.initCylinder();
		loadRessources();
		initGame();
		//Quaternion.test();
	}

	public void draw() {
		updateMode();
		imgProcessing.update();

		placeCamEtLum();
		
		//update display everything
		physic.displayAll();
		Cylinder.displayCylinders();
		if (drawAxis)
			drawAxis();
		

		
		if (run)
			physic.doMagic();
		
		camera();
		hint(DISABLE_DEPTH_TEST);
		starship.armement.displayGui();
		imgProcessing.display();
		hint(ENABLE_DEPTH_TEST);
	}
	
	private void drawAxis() {
		float far = 10000;
		stroke(255, 0, 0);
		line(0, 0, 0, 0, far, 0);
		stroke(0, 0, 255);
		line(0, 0, 0, far, 0, 0);
		line(0, 0, 0, 0, 0, far);
	}
	
	private void loadRessources() {
		LanceMissile.missileImg = loadImage("missile.png");
		int[] pixels = LanceMissile.missileImg.pixels;
		for (int i=0; i<pixels.length; i++)
			if (pixels[i] == color(0))
				pixels[i] = color(0, 0);
		
		MeteorSpawner.meteor = loadShape("asteroid.obj");
		Starship.skybox = loadShape("skybox.obj");
		Starship.skybox.scale(100);
		Starship.starship = loadShape("starship.obj");
		Starship.starship.scale(15);
		Missile.missile = loadShape("rocket.obj");
		/*Missile.missile.scale(2);
		Missile.missile5 = loadShape("rocket.obj");
		Missile.missile5.scale(5);*/
		
	}

	private void initGame() {
		
		setMode(Mode.Jeu);
		
		imgProcessing = new ImageProcessing();
		
		physic = new Physic();
		starship = new Starship( vec(0, 100, 0), imgProcessing );
		//Mover mover = new Mover( vec(0, 120, -5) );
		//Plane sol = new Plane(ProMaster.zero, ProMaster.zero);
		//physic.colliders.add(mover);
		physic.colliders.add(starship);
		//physic.colliders.add( sol );
		
		physic.colliders.add( new Missile.Objectif(vec(0,100,-500), 20));
		
		//Cube cube1 = new Cube( base, ProMaster.zero.get(), 5, vec(300, 30,300) );
		//Cube cube2 = new Cube( dessus , vec(0, 0, QUARTER_PI), 1, vec(30, 30, 30) );
		
		//physic.colliders.add( cube1 );
		//physic.colliders.add( cube2 );
		//cube2.applyImpulse(cube2.location, new PVector(0, -1, 0));
		
		
	}
	
	private PVector vec(float x, float y, float z) {
		return new PVector(x, y, z);
	}

	private void placeCamEtLum() { 
		if (!starship.hasCamera) {
			float distss = 300;
			PVector posVue = starship.location.get();
			PVector posCam = PVector.add( new PVector(distss, distss, distss), posVue );
			camera(posCam.x, posCam.y, posCam.z, posVue.x, posVue.y, posVue.z, 0, -1, 0);
		}
		
		//lum
		//ambientLight(255, 255, 255);
		//directionalLight(50, 100, 125, 0, -1, 0);
		//le bg
		if (!Starship.displaySkybox)
			background(200);
	}

	//-------- Gestion Evenements

	public void mouseDragged() {
		if (run) 
			starship.mouseDragged();
	}

	public void mouseWheel(MouseEvent event) {
		float delta = - event.getCount(); //delta negatif si vers l'utilisateur
		starship.forceRatio = PApplet.constrain( starship.forceRatio + 0.05f*delta , 0.1f, 2 );
	}

	public void keyReleased() {
		starship.keyReleased();
		if (key != CODED) return;

		//shift: mode contrôle
		if (keyCode == SHIFT || keyCode == CONTROL) {
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
		starship.keyPressed();
		
		//q: recommence la partie
		if (key == 'q') {
			initGame();
		}
		
		//tab: switch camera
		if (keyCode == TAB) {
			starship.hasCamera = !starship.hasCamera;
		}
		
		if (key != CODED)
			return; 

		//gauche droite: tourne la plaque  
		/*if (keyCode == LEFT) {
			starship.rotation.y = ProMaster.entrePiEtMoinsPi(starship.rotation.y - pasRotY);
		} else if (keyCode == RIGHT) {
			starship.rotation.y = ProMaster.entrePiEtMoinsPi(starship.rotation.y + pasRotY);
		}*/

		//shift: mode contrôle
		if (keyCode == SHIFT || keyCode == CONTROL) {
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
			//nouveau cylindre ! :D
			//on trouve la position sur l'ecran (en pixel) de l'extrêmité du terrain
			PVector pos3D = new PVector(-Starship.size.x, 0, Starship.size.z);
			PVector pos2D = new PVector( screenX(pos3D.x, pos3D.y, pos3D.z), screenY(pos3D.x, pos3D.y, pos3D.z) );
			PVector pos2DCentre = new PVector( pos2D.x - width/2, pos2D.y - height/2 );
			//on trouve l'échelle du terrain sur l'écran (par rapport au centre de l'écran)
			PVector echelle = new PVector( Starship.size.x/pos2DCentre.x, Starship.size.z/pos2DCentre.y );
			PVector newCylinderPos = new PVector( -(mouseX - width/2)*echelle.x , 0, (mouseY - height/2)*echelle.y );

			//on l'ajoute uniquement s'il est sur le terrain
			if (standInScene(newCylinderPos))
				Cylinder.cylindersPos.add(newCylinderPos);
		}
	}

	private boolean standInScene(PVector pos) {
		return ProMaster.isConstrained(pos.x, -Starship.size.x/2, Starship.size.x/2) &&
				ProMaster.isConstrained(pos.z, -Starship.size.z/2, Starship.size.z/2); //over x and z coordonate
	}

	//------ update et changement de mode

	private void updateMode() {
		switch (mode) {
		case Jeu:
		case Placement:
			break;
		case TransUp:
			etat = PApplet.constrain(etat + 1.0f/tempsTransition/frameRate, 0, 1);
			if (etat == 1)
				setMode(Mode.Placement);
			break;
		case TransDown:
			etat = PApplet.constrain(etat - 1.0f/tempsTransition/frameRate, 0, 1);
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