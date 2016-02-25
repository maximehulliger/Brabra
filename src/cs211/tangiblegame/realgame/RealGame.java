package cs211.tangiblegame.realgame;

import processing.core.PApplet;
import processing.event.MouseEvent;
import cs211.tangiblegame.Interface;
import cs211.tangiblegame.physic.Physic;
import cs211.tangiblegame.realgame.Armement;

public class RealGame extends Interface {
	public Physic physic;
	public Camera camera;
	public Starship starship;
	
	public RealGame() {
		XmlLoader.game = this;
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
		
		Prefab.file.load();
		
		/*
		int d = 1000;
		physic.colliders.add( );
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

		physic.displayAll();
		
		physic.doMagic();
		
		camera.gui();
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

	public void keyPressed() {
		if (starship != null)
			starship.keyPressed();
	}  
	
	public void keyReleased() {
		if (starship != null)
			starship.keyReleased();
		if (app.key == 'r')
			init();
		else if (app.key == 'c')
			camera.displayState();
		if (app.keyCode == PApplet.TAB)
			camera.nextMode();
	}
}
