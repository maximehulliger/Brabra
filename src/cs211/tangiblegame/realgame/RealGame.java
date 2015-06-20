package cs211.tangiblegame.realgame;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;
import cs211.tangiblegame.Interface;
import cs211.tangiblegame.physic.Physic;
import cs211.tangiblegame.realgame.Armement;

public class RealGame extends Interface {
	private static final boolean drawAxis = false;
	
	public Physic physic;
	private Starship starship;
	
	public RealGame() {
		Armement.missileImg = app.loadImage("missile.jpg");
		int[] pixels = Armement.missileImg.pixels;
		for (int i=0; i<pixels.length; i++)
			if (pixels[i] == app.color(0))
				pixels[i] = app.color(0, 0);
		
		MeteorSpawner.meteor = app.loadShape("asteroid.obj");
		Starship.skybox = app.loadShape("skybox.obj");
		Starship.skybox.scale(100);
		Starship.starship = app.loadShape("starship.obj");
		Starship.starship.scale( Starship.sizeFactor );
		Armement.missile = app.loadShape("rocket.obj");
	}
	

	public void init() {
		physic = new Physic();
		starship = new Starship( vec(0, 100, 0) );
		//Mover mover = new Mover( vec(0, 120, -5) );
		//Plane sol = new Plane(ProMaster.zero, ProMaster.zero);
		//physic.colliders.add(mover);
		physic.colliders.add(starship);
		//physic.colliders.add( sol );
		
		int vie = 200;
		physic.colliders.add( new Armement.Objectif(vec(0,100,-500), vie));
		physic.colliders.add( new Armement.Objectif(vec(0,100,500), vie));
		physic.colliders.add( new Armement.Objectif(vec(400,100,-800), vie));
		physic.colliders.add( new Armement.Objectif(vec(-400,100,-800), vie));
		physic.colliders.add( new Armement.Objectif(vec(0,800,100), vie));
		physic.colliders.add( new Armement.Objectif(vec(0, -500,0), vie));
		physic.colliders.add( new Armement.Objectif(vec(400,400,400), vie));
		physic.colliders.add( new Armement.Objectif(vec(-400,-400,400), vie));
		physic.colliders.add( new Armement.Objectif(vec(600, 800,-500), vie));
		physic.colliders.add( new Armement.Objectif(vec(400,100,-500), vie));
		
		//Cube cube1 = new Cube( base, ProMaster.zero.get(), 5, vec(300, 30,300) );
		//Cube cube2 = new Cube( dessus , vec(0, 0, QUARTER_PI), 1, vec(30, 30, 30) );
		
		//physic.colliders.add( cube1 );
		//physic.colliders.add( cube2 );
		//cube2.applyImpulse(cube2.location, new PVector(0, -1, 0));
	}
	
	public void wakeUp() {
		app.imgAnalyser.detectButtons = true;
		app.imgAnalyser.play(false);
	}
	
	public void draw() {
		placeCamEtLum();

		//update & display everything
		physic.displayAll();
		if (drawAxis)
			drawAxis();
		
		physic.doMagic();

		app.camera();
		app.hint(PApplet.DISABLE_DEPTH_TEST);
		starship.armement.displayGui();
		app.imgAnalyser.displayCtrImg();
		if (starship.hasCamera) { //TODO not working
			app.fill(255, 255, 255, 255);
			app.point(app.width/2, app.height/2);
		}
		app.hint(PApplet.ENABLE_DEPTH_TEST);
	}

	private void placeCamEtLum() { 
		if (!starship.hasCamera) {
			float distss = 300;
			PVector posVue = starship.location.get();
			PVector posCam = PVector.add( new PVector(distss, distss, distss), posVue );
			app.camera(posCam.x, posCam.y, posCam.z, posVue.x, posVue.y, posVue.z, 0, -1, 0);
		}
		
		//lum
		//ambientLight(255, 255, 255);
		//directionalLight(50, 100, 125, 0, -1, 0);
		//le bg
		if (!Starship.displaySkybox)
			app.background(200);
	}

	private void drawAxis() {
		float far = 10000;
		app.stroke(255, 0, 0);
		app.line(0, 0, 0, 0, far, 0);
		app.stroke(0, 0, 255);
		app.line(0, 0, 0, far, 0, 0);
		app.line(0, 0, 0, 0, 0, far);
	}

	//-------- EVENTS
	
	public void mouseDragged() {
		starship.mouseDragged();
	}

	public void mouseWheel(MouseEvent event) {
		float delta = - event.getCount(); //delta negatif si vers l'utilisateur
		starship.forceRatio = PApplet.constrain( starship.forceRatio + 0.05f*delta , 0.1f, 2 );
	}

	public void keyReleased() {
		starship.keyReleased();
	}

	public void keyPressed() {
		starship.keyPressed();
		
		//tab: switch camera
		if (app.keyCode == PApplet.TAB) {
			starship.hasCamera = !starship.hasCamera;
		}
	}  

	public void mouseReleased() {
		
	}
}
