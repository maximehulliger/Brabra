package cs211.tangiblegame;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import javafx.application.Platform;
import processing.core.*;
import processing.event.MouseEvent;

import cs211.gui.ToolWindow;
import cs211.tangiblegame.calibration.Calibration;
import cs211.tangiblegame.imageprocessing.ImageAnalyser;
import cs211.tangiblegame.realgame.RealGame;
import cs211.tangiblegame.trivial.TrivialGame;

public class TangibleGame extends PApplet {
	public enum View {Menu, Calibration, TrivialGame, RealGame, None}
	public static final String name = "Brabra";
	
	//--- parametres
	/** [1-6]: user[1-3], dev[4-6] 6: one object debug. atm: 7. */
	public static final int verbosity = 7;
	/** Max tilt in radians that will be taken in account for the plate (detection) */
	public static final float inclinaisonMax = PApplet.PI/5;
	/** Main window size. */
	public final int width = 1080, height = 720;
	/** Indicates if this should be activated on start. */
	protected boolean imgAnalysis = false, toolWindow = false;
	
	//--- public
	public final Debug debug = new Debug();
	public ImageAnalyser imgAnalyser;
	public Input input;
	public ToolWindow fxApp;
	public PVector windowLoc;
		
	//--- interne
	private String basePath;
	private boolean imgAnalysisStarted = false, fxAppStarted = false;
	private Interface currentInterface, intMenu, intRealGame, intTrivialGame, intCalibration, intNone;
	private boolean over = false;
	
	// --- setup and life cycle (draw, dispose) ---
	
	public static void main(String args[]) {
		TangibleGame me = new TangibleGame();
		String[] sketchArgs = {"--location="+(int)me.windowLoc.x+","+(int)me.windowLoc.y, TangibleGame.class.getName()};
		PApplet.runSketch( sketchArgs, me );
	}
	
	public TangibleGame() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		windowLoc = new PVector((gd.getDisplayMode().getWidth() - width) / 2,
       			(gd.getDisplayMode().getHeight() - height) / 3);
		if (toolWindow)
			 windowLoc.x += ToolWindow.width/2;
	}
	
	public void settings() {
		size(width, height, "processing.opengl.PGraphics3D");
		String dataPath = dataPath("");
		basePath = (dataPath.substring(0, dataPath.lastIndexOf(name)+name.length())+"/bin/").replace('\\', '/');
		debug.info(2, "base path: "+basePath+" in "+System.getProperty("os.name"));
	}

	public void setup() {
		// 1. processing stuff.
		frameRate(30);
		float camZ = height / (2*tan(PI*60/360.0f));
		perspective(PI/3, width/(float)height, camZ/100, camZ*1000);
		surface.setTitle(name);
		// enable the frame and correct windowLoc.
		frame.pack();
		
        Insets insets = frame.getInsets();
        windowLoc.sub(insets.left, insets.top);
		
        // 2. our stuff
		setupGame();
		setView(View.RealGame);
		// show second window if needed
		setToolWindow(toolWindow);
	}

	/** setup all our stuff. */
	protected void setupGame() {
		ProMaster.app = this;
		ToolWindow.app = this;
		input = new Input();
		setImgAnalysis(imgAnalysis);
		setToolWindow(toolWindow);
	}
	
	public void draw() {
		// input + current interface
		input.update();
		currentInterface.draw();
		// gui
		hint(PApplet.DISABLE_DEPTH_TEST);
		camera();
		if (imgAnalysis && currentInterface!=intCalibration)
			imgAnalyser.gui();
		currentInterface.gui();
		hint(PApplet.ENABLE_DEPTH_TEST);
		// debug
		debug.update();
		
	}

	public void dispose() {
		debug.setCurrentWork("quiting");
		over = true;
		if (fxApp != null)
			Platform.exit();
		super.dispose();
		System.out.println("bye bye !");
	}
	
	// --- getters ---
	
	public String inputPath() {
		return dataPathTo("input");
	}
	
	public String dataPath() {
		return dataPathTo("data");
	}
	
	public String dataPathTo(String ext) {
		return basePath+ext+"/";
	}

	public boolean hasToolWindow() {
		return toolWindow;
	}
	
	public boolean hasImgAnalysis() {
		return imgAnalysis;
	}
	
	public boolean isOver() {
		return over;
	}
	
	// --- file loading ---
	
	public PShape loadShape(String filename) {
		return super.loadShape(dataPath()+filename);
	}
	
	public PImage loadImage(String file) {
		boolean abs = file.startsWith("C:") || file.startsWith("/");
		return super.loadImage(abs ? file : dataPath()+file);
	}
	
	// --- setters ---

	public void setVisible(boolean visible) {
		surface.setVisible(visible);
		if (toolWindow)
			fxApp.setVisible(visible);
		if (imgAnalysis && !imgAnalyser.running()) {
			imgAnalyser.play(false);
		}
	}
	
	/** Activate or deactivate (hide) the tool window. Initialize it (fxApp) if needed. */
	public void setToolWindow(boolean hasToolWindow) {
		toolWindow = hasToolWindow;
		if (!fxAppStarted && hasToolWindow) {
			fxAppStarted = true;
			debug.info(3, "starting tool window thread.");
			Master.launch(() -> {ToolWindow.run(this);} );
		} else if (fxAppStarted) {
			Platform.runLater(() -> fxApp.setVisible(hasToolWindow));
		}
	}
	
	/** Activate or deactivate the image analysis. Initialize it (imgAnalyser) if needed. */
	public void setImgAnalysis(boolean hasImgAnalysis) {
		imgAnalysis = hasImgAnalysis;
		if (imgAnalyser == null)
			imgAnalyser = new ImageAnalyser();
		if (imgAnalysisStarted && hasImgAnalysis) {
			debug.info(3, "starting img analysis thread.");
			Master.launch(() -> {imgAnalyser.run();} );
		}
	}
	
	/** Change the current view of the app. */
	public void setView(View view) {
		switch (view) {
		case Menu:
			if (intMenu == null)
				intMenu = new Menu();
			setInterface(intMenu);
			return;
		case Calibration:
			if (intCalibration == null) {
				intCalibration = new Calibration();
				intCalibration.init();
			}
			setInterface(intCalibration);
			return;
		case RealGame:
			if (intRealGame == null) {
				intRealGame = new RealGame();
				intRealGame.init();
			}
			setInterface(intRealGame);
			return;
		case TrivialGame:
			if (intTrivialGame == null) {
				intTrivialGame = new TrivialGame();
				intTrivialGame.init();
			}
			setInterface(intTrivialGame);
			return;
		case None:
			if (intNone == null) 
				intNone = new Interface() {
					public void init() {background(0);}
					public void draw() {}
				};
			setInterface(intNone);
			return;
		}
	}

	//-------- Gestion Evenements

	public void keyPressed() {
		if (key == 27) {
			key = 0; // on intercepte escape
			if (currentInterface != intMenu)
				setView(View.Menu);
			else
				exit();
		} else if (key == 'l')
			debug.log("parameters loading still to implement");//imgAnalyser.imgProc.selectParameters(); TODO
		else if (key == 'q')
			currentInterface.init();
		else if (key == 'Q')
			imgAnalyser.resetAllParameters(true);
		else if (key == 'p')
			imgAnalyser.playOrPause();
		else if (key=='i')
			imgAnalyser.changeInput();
		else if (key=='h')
			setToolWindow(!toolWindow);
		
		input.keyPressed();
		currentInterface.keyPressed();
	}

	public void mouseDragged() {
		input.mouseDragged();
		currentInterface.mouseDragged();
	}

	public void mouseWheel(MouseEvent event) {
		input.mouseWheel(event);
		currentInterface.mouseWheel(event);
	}

	public void keyReleased() {
		input.keyReleased();
		currentInterface.keyReleased();
	}

	public void mousePressed() {
		currentInterface.mousePressed();
	}
	
	public void mouseReleased() {
		currentInterface.mouseReleased();
	}
	
	public void mouseMoved() {
	}
	
	// --- private stuff -> KEEP OUT ---
	
	private void setInterface(Interface view) {
		if (currentInterface != null)
			currentInterface.onHide();
		currentInterface = view;
		view.onShow();
	}

}