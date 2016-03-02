package cs211.tangiblegame.realgame;

import processing.core.PApplet;

import cs211.tangiblegame.Interface;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.physic.Physic;
import cs211.tangiblegame.physic.PhysicInteraction;
import cs211.tangiblegame.realgame.Armement;

public class RealGame extends Interface {
	public Physic physic;
	public PhysicInteraction physicInteraction;
	public Camera camera;
	
	private XMLLoader xmlFile = new XMLLoader();
	
	public RealGame() {
		ProMaster.game = this;
		Armement.missileImg = app.loadImage(app.dataPath+"missile.jpg");
		int[] pixels = Armement.missileImg.pixels;
		for (int i=0; i<pixels.length; i++)
			if (pixels[i] == app.color(0))
				pixels[i] = app.color(0, 0);
		
		MeteorSpawner.meteor = app.loadShape(app.dataPath+"asteroid.obj");
		Camera.skybox = app.loadShape(app.dataPath+"skybox.obj");
		Camera.skybox.scale(10000);
		Starship.starship = app.loadShape(app.dataPath+"starship.obj");
		Starship.starship.scale( Starship.sizeFactor );
		Armement.missile = app.loadShape(app.dataPath+"rocket.obj");
	}

	public void init() {
		physic = new Physic();
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
		camera.place();
		physicInteraction.update();
		physic.doMagic();
		physic.displayAll();
	}
	
	public void gui() {
		game.physicInteraction.gui();
	}
		
	//-------- EVENTS
	
	public void keyReleased() {
		if (app.key == 'r')
			init();
		else if (app.key == 'c')
			camera.displayState();
		if (app.keyCode == PApplet.TAB)
			camera.nextMode();
	}
}
