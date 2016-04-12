package brabra.game;

import brabra.Interface;
import brabra.Debug;
import brabra.game.physic.Physic;
import brabra.game.scene.Camera;
import brabra.game.scene.Scene;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class RealGame extends Interface {
	
	public final Input input = new Input();
	public final GameDebug debug = new GameDebug();
	public final PhysicInteraction physicInteraction = new PhysicInteraction();
	public final Camera camera = new Camera();
	public final Scene scene = new Scene(this);
	
	private final XMLLoader xmlFile = new XMLLoader();
	private boolean playOnFocus;
	private boolean running = true;
	
	public boolean running() {
		return running;
	}

	public void setRunning(boolean running) {
		if (running != this.running) {
			this.running = running;
			debug.info(1, "game " + (running ? "running !" : "paused :)"));
		}
	}
		
	// --- life cycle ---
	
	public void init() {
		clearConsole();
		debug.info(0, "loading scene");
		scene.reset();
		scene.addNow(camera);
		debug.followed.clear();
		physicInteraction.setFocused(null, -1);
		xmlFile.load();
		playOnFocus = running;
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
			Physic.doMagic(scene);
			debug.update();
		}
		scene.displayAll();	
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
			setRunning(false);
		}
	}
	
	public void keyReleased() {
		input.keyReleased();
		if (app.key == 'r')
			setRunning(true);
		else if (app.keyCode == PApplet.TAB)
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
		 if (focused) {
			 if (!running)
				 setRunning(playOnFocus);
		 } else {
			 playOnFocus = running;
			 setRunning(running ? app.runWithoutFocus : false);
		 }
	}
	
	// --- Game debug ---

	public class GameDebug extends Debug {
		
	}
}
