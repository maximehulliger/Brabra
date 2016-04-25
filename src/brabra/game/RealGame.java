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
	public final Scene scene = new Scene(this);
	
	private Camera camera = null; //lazy to be independent of processing
	
	private final XMLLoader xmlFile = new XMLLoader();
	
	// --- life cycle ---
	
	public void onShow() {
		clearConsole();
		debug.info(0, "loading scene");
		scene.reset();
		physicInteraction.setFocused(null, -1);
		xmlFile.load();
		app.imgAnalyser.detectButtons = true;
		app.imgAnalyser.play(false);
	}
	
	public Camera camera() {
		if (camera == null) {
			camera = new Camera();
			scene.add(camera);
		}
		return camera;
	}
	
	public void onHide() {
		scene.reset();
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
		camera.gui();
	}

	// --- events ---
	
	public void keyPressed() {
		input.keyPressed();
	}
	
	public void keyReleased() {
		input.keyReleased();
		if (app.keyCode == PApplet.TAB)
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
