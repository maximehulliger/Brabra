package cs211.tangiblegame;

import cs211.tangiblegame.Missile.LanceMissile;
import cs211.tangiblegame.geo.*;
import cs211.tangiblegame.imageprocessing.ImageProcessing;
import cs211.tangiblegame.physic.*;
import cs211.tangiblegame.trivial.TrivialGame;
import processing.core.*;
import processing.event.MouseEvent;


public class TangibleGame extends PApplet {
	private static final long serialVersionUID = 338280650599573653L;

	//--parametres
	private static final int ratioSize = 4; //généralement de 2 (640x360) à 5 (1920x1080)
	private static final boolean drawAxis = false;
	
	//--interne
	public ImageProcessing imgProcessing;
	TrivialGame trivialGame = null;
	private enum Mode {Menu, Game, TrivialGame}; //mode: up->vers contrôle
	
	private Mode mode = Mode.TrivialGame;
	public boolean run = false;
	public boolean paused = false; //pour la vidéo
	public Physic physic;
	private Starship starship;
	
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
		switch (mode) {
		case Menu: {
			return;
		}
		case Game: {
			imgProcessing.update();
			placeCamEtLum();
			
			//update & display everything
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
			return;
		}
		case TrivialGame: {
			imgProcessing.update();
			trivialGame.draw();
			
			camera();
			hint(DISABLE_DEPTH_TEST);
			imgProcessing.display();
			hint(ENABLE_DEPTH_TEST);
			return;
		}
		}
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
		
		Cylinder.initCylinder();
	}

	private void initGame() {
		imgProcessing = new ImageProcessing();
		run = true;
		if (mode == Mode.Game) {
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
		} else if (mode == Mode.TrivialGame) {
			trivialGame = new TrivialGame(imgProcessing);
		}
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
		switch (mode) {
		case Menu:
		case TrivialGame:
			return;
		case Game: {
			starship.keyReleased();
			if (key != CODED) return;
			return;
		}
		
		}
	}

	public void keyPressed() {
		
		
		switch (mode) {
		case Game: {
			starship.keyPressed();
			
			
			//tab: switch camera
			if (keyCode == TAB) {
				starship.hasCamera = !starship.hasCamera;
			}
		}
		case TrivialGame: {
			trivialGame.keyPressed();
		}
		//pour tous les jeux:
		{
			//q: recommence la partie
			if (key == 'q') {
				initGame();
			}
			//p: met en pause la vidéo
			if (key == 'p') {
				if (paused) 
					imgProcessing.cam.play();
				else 
					imgProcessing.cam.pause();
				paused = !paused;
			}
		}
		default:
			return;
		}
	}  

	public void mouseReleased() {
		switch (mode) {
		case TrivialGame:
			trivialGame.mouseReleased();
			return;
		default:
			return;
		}
	}
}