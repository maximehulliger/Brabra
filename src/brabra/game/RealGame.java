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
	public final PhysicInteraction physicInteraction = new PhysicInteraction();
	public final Scene scene = new Scene(this);
	
	public Camera camera = null; //created on show to be independent of processing
	
	// --- life cycle ---
	
	public void onShow() {
		clearConsole();
		Debug.info(0, "loading scene");
		camera = new Camera();
		physicInteraction.setFocused(null);
		scene.loader.loadLocalFiles();
		scene.loader.load();
		app.imgAnalyser.play(true, false);
	}

	public void onHide() {
		scene.clear();
		app.setImgAnalysis(false);
	}
	
	// mother method of all life:
	public void draw() {
		// we place the camera before updating the objects to get a cool visual effect (camera is one frame late in position).
		camera.place();
		if (running()) {
			input.update();
			physicInteraction.update();
			scene.updateAll();
			Physic.doMagic(scene);
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
