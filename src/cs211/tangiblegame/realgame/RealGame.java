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
		camera = new Camera();
		physicInteraction = new PhysicInteraction();
		xmlFile.load();
		debug.followed.add(camera);
	}
	
	public void wakeUp() {
		app.imgAnalyser.detectButtons = true;
		app.imgAnalyser.play(false);
	}
	
	// mother method of all life:
	public void draw() {
		physic.updateAll();
		physicInteraction.update();
		camera.place();
		physic.doMagic();
		physic.displayAll();
		debug.update();
	}
	
	public void gui() {
		game.physicInteraction.gui();
	}
		
	// --- events ---
	
	public void keyReleased() {
		if (app.key == 'r')
			init();
		else if (app.key == 'c')
			camera.displayState();
		if (app.keyCode == PApplet.TAB)
			camera.nextMode();
		if (app.key == 'p') {
			physic.paused = ! physic.paused;
			debug.msg(1, physic.paused ? "paused :)" : "play !");
		}
	}
}
