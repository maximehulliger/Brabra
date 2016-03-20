package brabra.game;

import brabra.Interface;
import brabra.ProMaster;
import brabra.Debug;
import brabra.game.physic.Physic;
import brabra.game.scene.Camera;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class RealGame extends Interface {
	
	public Input input;
	public Physic physic;
	public PhysicInteraction physicInteraction;
	public Camera camera;
	public GameDebug debug = new GameDebug();
	
	private XMLLoader xmlFile = new XMLLoader();
	private boolean playOnFocus;
	
	public RealGame() {
		ProMaster.game = this;
	}

	public void init() {
		clearConsole();
		debug.info(0, "loading scene");
		debug.followed.clear();
		input = new Input();
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
		input.update();
		camera.place();
		physicInteraction.update();
		physic.updateAll();
		physic.doMagic();
		physic.displayAll();
		if (physic.running)
			debug.update();
	}
	
	public void gui() {
		game.physicInteraction.gui();
		input.gui();
		game.debug.setCurrentWork("user events");
	}
		
	// --- events ---
	
	public void keyPressed() {
		input.keyPressed();
		if (app.key == 'r') {
			init();
			physic.running = false;
		}
	}
	
	public void keyReleased() {
		input.keyReleased();
		if (app.key == 'r')
			physic.running = true;
		else if (app.key == 'c') {
			debug.info(2, physic.objects().size()+" objects in scene:");
			physic.objects().forEach(o -> o.displayState());
			//camera.displayState();
		} else if (app.keyCode == PApplet.TAB)
			camera.nextMode();
		else if (app.key == 'p') {
			physic.running = !physic.running;
			debug.msg(1, "physic " + physic.state());
		}
	}

	public void mouseDragged() {
		input.mouseDragged();
	}

	public void mouseWheel(MouseEvent event) {
		input.mouseWheel(event);
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
	
	// --- Game debug ---

	public class GameDebug extends Debug {
		
	}
}
