package cs211.tangiblegame.realgame;

import processing.core.PApplet;
import processing.event.MouseEvent;
import cs211.tangiblegame.Interface;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.physic.Physic;
import cs211.tangiblegame.realgame.Armement;

public class RealGame extends Interface {
	public Physic physic;
	public Camera camera;
	public PhysicInteraction physicInteraction;
	
	public RealGame() {
		ProMaster.game = this;
		Armement.missileImg = app.loadImage(TangibleGame.dataPath+"missile.jpg");
		int[] pixels = Armement.missileImg.pixels;
		for (int i=0; i<pixels.length; i++)
			if (pixels[i] == app.color(0))
				pixels[i] = app.color(0, 0);
		
		MeteorSpawner.meteor = app.loadShape(TangibleGame.dataPath+"asteroid.obj");
		Camera.skybox = app.loadShape(TangibleGame.dataPath+"skybox.obj");
		Camera.skybox.scale(10000);
		Starship.starship = app.loadShape(TangibleGame.dataPath+"starship.obj");
		Starship.starship.scale( Starship.sizeFactor );
		Armement.missile = app.loadShape(TangibleGame.dataPath+"rocket.obj");
	}

	public void init() {
		physic = new Physic();
		camera = new Camera();
		physicInteraction = new PhysicInteraction();
		Prefab.file.load();
	}
	
	public void wakeUp() {
		app.imgAnalyser.detectButtons = true;
		app.imgAnalyser.play(false);
	}
	
	public void draw() {
		
		physicInteraction.update();
		
		physic.doMagic();
		
		camera.place();
		
		physic.displayAll();
	}
	
	public void gui() {
		if (game.physicInteraction.armement != null)
			game.physicInteraction.armement.displayGui();
	}
		

	//-------- EVENTS
	
	public void mouseDragged() {
		physicInteraction.mouseDragged();
	}
	
	public void mouseWheel(MouseEvent event) {
		physicInteraction.mouseWheel(event);
	}

	public void keyPressed() {
		physicInteraction.keyPressed();
	}  
	
	public void keyReleased() {
		physicInteraction.keyReleased();
		if (app.key == 'r')
			init();
		else if (app.key == 'c')
			camera.displayState();
		if (app.keyCode == PApplet.TAB)
			camera.nextMode();
	}
}
