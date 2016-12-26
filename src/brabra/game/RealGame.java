package brabra.game;

import brabra.Interface;
import brabra.Parameters;

import java.util.Observable;
import java.util.Observer;

import org.ode4j.ode.OdeHelper;

import brabra.Debug;
import brabra.game.scene.Camera;
import brabra.game.scene.Scene;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class RealGame extends Interface implements Observer {
	
	public final Input input = new Input();
	public final PhysicInteraction physicInteraction = new PhysicInteraction();
	public Scene scene = null;
	public Scene.Model sceneModel = new Scene.Model();
	
	public Camera camera = null; //created on show to be independent of processing
	
	// --- life cycle ---
	
	public RealGame() {
		app.para.addObserver(this);
	}

	public void onShow() {
		clearConsole();
		Debug.info(0, "creating scene");
		OdeHelper.initODE2(0);
		scene = new Scene(this);
		sceneModel.scene = scene;
		camera = new Camera();
		
		
		physicInteraction.setFocused(null);
		Scene.loader.loadLocalFiles();
		Scene.loader.load();
		
		if (app.imgAnalysisStarted) {
			app.setImgAnalysis(true);
			app.imgAnalyser.play(true, false);
		}
	}

	public void onHide() {
		scene.clear();
		OdeHelper.closeODE();
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
		}
		scene.displayAll();	
	}
	
	public void gui() {
		game.physicInteraction.gui();
		input.gui();
		camera.gui();
		if (app.imgAnalysis)
			app.imgAnalyser.gui();
	}

	// --- events ---
	
	public void keyPressed() {
		input.keyPressed();
	}
	
	public void keyReleased() {
		input.keyReleased();
		if (app.keyCode == PApplet.TAB)
			camera.changeMode();
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

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == Parameters.Change.Running) {
			if (app.imgAnalysis) {
				if (running())
					app.imgAnalyser.play(true, false);
				else
					app.imgAnalyser.stop();
			}
		}
	}
}
