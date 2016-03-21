package brabra.game;

import brabra.Interface;
import brabra.ProMaster;
import brabra.Debug;
import brabra.game.physic.Physic;
import brabra.game.scene.Camera;
import brabra.game.scene.Scene;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class RealGame extends Interface {
	
	public Input input;
	public Scene scene;
	public Physic physic;
	public PhysicInteraction physicInteraction;
	public Camera camera;
	public GameDebug debug = new GameDebug();
	
	private XMLLoader xmlFile = new XMLLoader();
	private boolean playOnFocus;
	private boolean running = true;
	
	public RealGame() {
		ProMaster.game = this;
	}

	public void init() {
		clearConsole();
		debug.info(0, "loading scene");
		debug.followed.clear();
		input = new Input();
		scene = new Scene();
		physic = new Physic();
		playOnFocus = running;
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
		if (running) {
			scene.beforeUpdateAll();
			input.update();
			physicInteraction.update();
			scene.updateAll();
			physic.doMagic();
			debug.update();
		}
		scene.displayAll();	
	}
	
	public void gui() {
		game.physicInteraction.gui();
		input.gui();
		game.debug.setCurrentWork("user events");
	}

	public String state() {
		return running ? "running !" : "paused :)";
	}
		
	// --- events ---
	
	public void keyPressed() {
		input.keyPressed();
		if (app.key == 'r') {
			init();
			setRunning(false);
		}
	}
	
	public void keyReleased() {
		input.keyReleased();
		if (app.key == 'r')
			setRunning(true);
		else if (app.key == 'c') {
			debug.info(2, scene.objects().size()+" objects in scene:");
			scene.objects().forEach(o -> o.displayState());
			//camera.displayState();
		} else if (app.keyCode == PApplet.TAB)
			camera.nextMode();
		else if (app.key == 'p') {
			setRunning(!running);
		}
	}

	public void mouseDragged() {
		input.mouseDragged();
	}

	public void mouseWheel(MouseEvent event) {
		input.mouseWheel(event);
	}

	public void mousePressed() {
		input.mousePressed();
	}
	
	public void mouseReleased() {
		input.mouseReleased();
	}
	
	public void onFocusChange(boolean focused) {
		 if (focused && !running) {
			 setRunning(playOnFocus);
		 } else if (!focused) {
			 playOnFocus = running;
			 setRunning(false);
		 }
	}
	
	public void setRunning(boolean running) {
		if (running != this.running) {
			this.running = running;
			debug.info(1, "physic " + state());
		}
	}
	
	// --- Game debug ---

	public class GameDebug extends Debug {
		
	}
}
