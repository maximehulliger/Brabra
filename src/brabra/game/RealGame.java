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
	public final Debug debug = new Debug();
	public final PhysicInteraction physicInteraction = new PhysicInteraction();
	public final Camera camera = new Camera();
	public final Scene scene = new Scene(this);
	
	private final XMLLoader xmlFile = new XMLLoader();
	
	// --- life cycle ---
	
	public void init() {
		clearConsole();
		debug.info(0, "loading scene");
		scene.reset();
		scene.addNow(camera);
		debug.followed.clear();
		physicInteraction.setFocused(null, -1);
		xmlFile.load();
		app.imgAnalyser.detectButtons = true;
		app.imgAnalyser.play(false);
	}
	
	// mother method of all life:
	public void draw() {
		// we place the camera before updating the objects to get a cool visual effect (camera is one frame late in position).
		camera.place();
		if (running()) {
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
			setRunning(!running());
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

	private boolean running() {
		return app.para.running();
	}

	private void setRunning(boolean running) {
		app.para.setRunning(running);
	}
}
