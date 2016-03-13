package cs211.tangiblegame.realgame;

import processing.core.PApplet;

import cs211.tangiblegame.Debug.GameDebug;
import cs211.tangiblegame.Interface;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.physic.Physic;
import cs211.tangiblegame.physic.PhysicInteraction;
import cs211.tangiblegame.realgame.Armement;

public class RealGame extends Interface {
	public Physic physic;
	public PhysicInteraction physicInteraction;
	public Camera camera;
	public GameDebug debug = new GameDebug();
	
	private XMLLoader xmlFile = new XMLLoader();
	private boolean playOnFocus;
	
	public RealGame() {
		ProMaster.game = this;
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
		clearConsole();
		debug.info(0, "loading scene");
		physic = new Physic();
		playOnFocus = physic.running;
		camera = new Camera();
		physicInteraction = new PhysicInteraction();
		xmlFile.load();
	}
	
	public void wakeUp() {
		app.imgAnalyser.detectButtons = true;
		app.imgAnalyser.play(false);
	}
	
	// mother method of all life:
	public void draw() {
		physicInteraction.update();
		physic.updateAll();
		debug.update();
		physic.doMagic();
		camera.place();
		physic.displayAll();
	}
	
	public void gui() {
		game.physicInteraction.gui();
		game.debug.setCurrentWork("user events");
	}
		
	// --- events ---
	
	public void keyPressed() {
		if (app.key == 'r') {
			init();
			physic.running = false;
		}
	}
	
	public void keyReleased() {
		if (app.key == 'r')
			physic.running = true;
		else if (app.key == 'c')
			camera.displayState();
		if (app.keyCode == PApplet.TAB)
			camera.nextMode();
		if (app.key == 'p') {
			physic.running = !physic.running;
			debug.msg(1, "physic " + physic.state());
		}
	}
	
	public void onFocusChange(boolean focused) {
		 if (focused && !physic.running) {
			 physic.running = playOnFocus;
			 //debug.msg(1, "physic " + physic.state());
		 } else if (!focused) {
			 playOnFocus = physic.running;
			 if (physic.running){
				 physic.running = false;
				 //debug.msg(1, "physic " + physic.state());
			 }
		 }
	}
}
