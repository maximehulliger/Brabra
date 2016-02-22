package cs211.tangiblegame.realgame;

import processing.core.PApplet;
import processing.event.MouseEvent;
import cs211.tangiblegame.Camera;
import cs211.tangiblegame.Interface;
import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.physic.Physic;
import cs211.tangiblegame.realgame.Armement;

public class RealGame extends Interface {
	public Physic physic;
	public Camera camera;
	private Starship starship;
	
	public RealGame() {
		XmlLoader.game = this;
		Prefab.game = this;
		Armement.missileImg = app.loadImage("missile.jpg");
		int[] pixels = Armement.missileImg.pixels;
		for (int i=0; i<pixels.length; i++)
			if (pixels[i] == app.color(0))
				pixels[i] = app.color(0, 0);
		
		MeteorSpawner.meteor = app.loadShape("asteroid.obj");
		Camera.skybox = app.loadShape("skybox.obj");
		Camera.skybox.scale(10000);
		Starship.starship = app.loadShape("starship.obj");
		Starship.starship.scale( Starship.sizeFactor );
		Armement.missile = app.loadShape("rocket.obj");
	}
	

	public void init() {
		physic = new Physic();
		camera = new Camera();
		//starship = new Starship( vec(0, 100, -700) );
		//physic.colliders.add(starship);
		
		
		//mover.applyImpulse( PVector.add(mover.location, vec(0, 0, 0)), vec(0, 0, 1));
		
		//physic.colliders.add( new Shield(vec(0, 100, 0), vec(200, 20, 80)));
		
		
		//Cube cube1 = new Cube( base, ProMaster.zero.get(), 5, vec(300, 30,300) );
		//Cube cube2 = new Cube( dessus , vec(0, 0, QUARTER_PI), 1, vec(30, 30, 30) );
		
		Prefab.file.load();
		/*Prefab.add("floor", zero);
		Prefab.add("ball", vec(0, 20, 0));
		Prefab.add("ball", vec(0, 10, 0)).applyImpulse(vec(0, 10, 0));
		Prefab.add("box", vec(5, 10, 0));*/
		
		
		/*
		int d = 1000;
		physic.colliders.add( new Armement.Objectif(vec(0,100,-d), vie));
		physic.colliders.add( new Armement.Objectif(vec(0,100,d), vie));
		physic.colliders.add( new Armement.Objectif(vec(d,100,-2*d), vie));
		physic.colliders.add( new Armement.Objectif(vec(-d,100,-2*d), vie));
		physic.colliders.add( new Armement.Objectif(vec(0,2*d,100), vie));
		physic.colliders.add( new Armement.Objectif(vec(0, -d,0), vie));
		physic.colliders.add( new Armement.Objectif(vec(d,d,d), vie));
		physic.colliders.add( new Armement.Objectif(vec(-d,-d,d), vie));
		physic.colliders.add( new Armement.Objectif(vec(2*d, 2*d,-d), vie));
		physic.colliders.add( new Armement.Objectif(vec(d,100,-d), vie));*/
	}
	
	public void wakeUp() {
		app.imgAnalyser.detectButtons = true;
		app.imgAnalyser.play(false);
	}
	
	public void draw() {
		camera.place();

		//update & display everything
		physic.displayAll();
		
		physic.doMagic();
		
		//GUI
		app.camera();
		app.hint(PApplet.DISABLE_DEPTH_TEST);
		if (starship != null)
			starship.armement.displayGui();
		if (TangibleGame.imgAnalysis)
			app.imgAnalyser.displayCtrImg();
		app.hint(PApplet.ENABLE_DEPTH_TEST);
	}

	//-------- EVENTS
	
	public void mouseDragged() {
		if (starship != null)
			starship.mouseDragged();
	}
	
	public void mouseWheel(MouseEvent event) {
		if (starship != null) {
			float delta = - event.getCount(); //delta negatif si vers l'utilisateur
			starship.forceRatio = PApplet.constrain( starship.forceRatio + 0.05f*delta , 0.2f, 60 );
		}
	}

	public void keyReleased() {
		if (starship != null)
			starship.keyReleased();
		if (app.key == 'r')
			init();
	}

	public void keyPressed() {
		if (starship != null)
			starship.keyPressed();
		
		//tab: switch camera
		if (app.keyCode == PApplet.TAB) {
			camera.nextMode();
		}
	}  

	public void mouseReleased() {
		
	}
}
