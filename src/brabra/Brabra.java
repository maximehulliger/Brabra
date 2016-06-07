package brabra;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.ConcurrentLinkedQueue;

import brabra.calibration.Calibration;
import brabra.game.RealGame;
import brabra.game.physic.geo.ProTransform;
import brabra.gui.ToolWindow;
import brabra.imageprocessing.ImageAnalyser;
import brabra.trivial.TrivialGame;
import javafx.application.Platform;
import processing.core.*;
import processing.event.MouseEvent;


/** Main class of the Brabra project. */
public class Brabra extends PApplet {
	public enum View {Menu, Calibration, TrivialGame, RealGame, None}
	public static final String name = "Brabra";
	
	//--- Parameters
	/** [1-6]: user[1-3], dev[4-verbMax]. atm: verbMax. */
	public int verbosity = 6;
	/** Verbosity for one object debug. */
	public static final int verbMax = 6;
	/** Max tilt in radians that will be taken in account for the plate (detection) */
	public static final float inclinaisonMax = PApplet.PI/5;
	/** Main window size. */
	public static final int width = 1080, height = 720;
	/** Frame per seconds wished by Brabra. */
	public static final float frameRate = 30;
	/** Indicates if this should be activated on start. */
	public boolean imgAnalysis = false, toolWindow = true, runWithoutFocus = false;
	
	//--- Public
	/** Static reference to the app. valid once initLock is released. */
	public static Brabra app;
	public final Parameters para = new Parameters();
	public final PVector stageLoc;
	public ImageAnalyser imgAnalyser = null;
	public ToolWindow fxApp = null;
		
	//--- Intern
	private final String basePath;
	private final ConcurrentLinkedQueue<Runnable> toExecuteInPro = new ConcurrentLinkedQueue<>();
	private boolean imgAnalysisStarted = false, fxAppStarted = false;
	private Interface currentInterface;
	public final Interface intMenu, intTrivialGame, intCalibration;
	public final RealGame game;
	private boolean over = false;
	private boolean focusGainedEventWaiting = false;
	
	// --- setup and life cycle (draw, dispose) ---
	
	public static void main(String args[]) {
		new Brabra().launch();
	}
	
	public Brabra() {
		ProTransform.app = ProMaster.app = Brabra.app = this;
		
		// set window location
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		stageLoc = new PVector((gd.getDisplayMode().getWidth() - width) / 2,
       			(gd.getDisplayMode().getHeight() - height) / 3);
		if (toolWindow)
			 stageLoc.x += ToolWindow.width/2;
		
		// set base path
		final String rawDataPath = dataPath(""); // from processing
		final String toData = Master.inEclipse() ? "src/" : "bin/";
		basePath = (rawDataPath.substring(0, rawDataPath.lastIndexOf(name)+name.length())+"/"+toData).replace('\\', '/');
		Debug.info(2, "base path: "+basePath+" in "+System.getProperty("os.name"));
		
		// init static & app interfaces.
		ProMaster.game = this.game = new RealGame();
		intCalibration = new Calibration();
		intTrivialGame = new TrivialGame();
		intMenu = new Menu();
	}
	
	/** Launch the whole software. */
	public void launch() {
		// start the other threads if needed.
		setImgAnalysis(imgAnalysis);
		setToolWindow(toolWindow);
					
		// start the processing thread.
		run();
	}
	
	/** Run the processing thread. */
	public void run() {
		String[] sketchArgs = {"--location="+(int)stageLoc.x+","+(int)stageLoc.y, this.getClass().getName()};
		PApplet.runSketch( sketchArgs, this );
	}
	
	/** Execute this Runnable later in the processing (main) thread. */
	public void runLater(Runnable r) {
		toExecuteInPro.add(r);
	}
	
	public void settings() {
		//1080, 720
		size(width, height, "processing.opengl.PGraphics3D");
	}

	public void setup() {
		try {
			// > Processing stuff.
			frameRate(frameRate);
			//float camZ = height / (2*tan(PI*60/360.0f));
			perspective(PI/3, width/(float)height, 1, ProMaster.far);
			surface.setTitle(name);
			// Enable the frame and correct windowLoc.
			frame.pack();
			
			// > Correct window lock
	        //Insets insets = frame.getInsets();
	        //windowLoc.sub(insets.left, insets.top);
			
	        // > App (main thread) is now fully ready (at least the private stuff). we wait for other components.

			// And finally show the view (init content).
			setView(View.RealGame);
			
		} catch (Exception e) {
			e.printStackTrace();
			this.dispose();
		}
	}
	
	public void draw() {
		try {
			// execute the code to execute in this thread.
			Runnable toRun = toExecuteInPro.poll();
			while (toRun != null) {
				toRun.run();
				toRun = toExecuteInPro.poll();
			}
			
			// draw interface
			currentInterface.draw();
			
			// gui
			hint(PApplet.DISABLE_DEPTH_TEST);
			camera();
			if (imgAnalysis && currentInterface!=intCalibration)
				imgAnalyser.gui();
			currentInterface.gui();
			hint(PApplet.ENABLE_DEPTH_TEST);
		} catch (Exception e) {
			e.printStackTrace();
			this.dispose();
		}
	}

	public void dispose() {
		if (!over) {
			Debug.setCurrentWork("quiting");
			over = true;
			if (fxApp != null && !fxApp.isClosing())
				Platform.exit();
			super.dispose();
			System.out.println("bye bye !");
		}
	}
	
	// --- Getters ---
	
	/** Return a path to a ressource from the root folder (probably 'Brabra/'). */
	public String pathTo(String ressource) {
		return basePath+ressource;
	}
	
	/** Return a path to a folder with this name from the root folder (probably 'Brabra/'). */
	public String pathToFolder(String folder) {
		return pathTo(folder)+"/";
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
	
	public boolean useDrag() { 
		return currentInterface.useDrag();
	}
	
	// --- File loading ---
	
	public PShape loadShape(String filename) {
		return super.loadShape(pathToFolder("resource")+filename);
	}
	
	public PImage loadImage(String file) {
		boolean abs = file.startsWith("C:") || file.startsWith("/");
		return super.loadImage(abs ? file : pathToFolder("resource")+file);
	}
	
	// --- Setters ---

	public void setVisible(boolean visible) {
		//surface.setVisible(visible);
		if (toolWindow)
			fxApp.setVisible(visible);
		if (imgAnalysis && !imgAnalyser.running()) {
			imgAnalyser.play(false);
		}
	}
	
	/** Activate or deactivate (hide) the tool window. Initialize it (fxApp) if needed. */
	public void setToolWindow(boolean hasToolWindow) {
		if (toolWindow != hasToolWindow || !fxAppStarted) {
			if (hasToolWindow) {
				toolWindow = true;
				fxAppStarted = true;
				Master.launch(() -> ToolWindow.launch());
				Debug.info(3, "Tool Window thread started.");
	
				// we wait for the javaFX thread to init (beacause of m).
//				try {
//					while (fxApp == null)
//						Thread.sleep(3_000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			} else if (fxAppStarted) {
				Platform.runLater(() -> fxApp.setVisible(hasToolWindow));
			}
		}
	}
	
	/** Activate or deactivate the image analysis. Initialize it (imgAnalyser) if needed. */
	public void setImgAnalysis(boolean hasImgAnalysis) {
		imgAnalysis = hasImgAnalysis;
		if (imgAnalyser == null)
			imgAnalyser = new ImageAnalyser();
		if (imgAnalysisStarted && hasImgAnalysis) {
			Debug.info(3, "starting img analysis thread.");
			Master.launch(() -> imgAnalyser.launch());
		}
	}
	
	/** Change the current view of the app. */
	public void setView(View view) {
		switch (view) {
		case Menu:
			setInterface(intMenu);
			return;
		case Calibration:
			setInterface(intCalibration);
			return;
		case RealGame:
			setInterface(game);
			return;
		case TrivialGame:
			setInterface(intTrivialGame);
			return;
		case None:
			setInterface(new Interface() {
				public void onShow() {background(0);}
				public void draw() {}
			});
			return;
		}
	}

	// --- Event Management ---

	public void keyPressed() {
		if (key == 27) {
			key = 0; // we intercept escape
			if (currentInterface != intMenu)
				setView(View.Menu);
			else
				exit();
		} else if (key == 'l')
			Debug.log("parameters loading still to implement");//imgAnalyser.imgProc.selectParameters(); TODO
		else if (key == 'r') {
			currentInterface.onHide();
			currentInterface.onShow();
			para.setRunning(false);
		} else if (key == 'Q')
			imgAnalyser.resetAllParameters(true);
		else if (key == 'p')
			imgAnalyser.playOrPause();
		else if (key=='i')
			imgAnalyser.changeInput();
		else if (key=='h')
			setToolWindow(!toolWindow);
		
		currentInterface.keyPressed();
	}

	public void keyReleased() {
		if (key == 'r')
			para.setRunning(true);
		currentInterface.keyReleased();
	}
	
	public void mouseDragged() {
		currentInterface.mouseDragged();
	}

	public void mouseWheel(MouseEvent event) {
		currentInterface.mouseWheel(event);
	}

	public void mousePressed() {
		currentInterface.mousePressed();
	}
	
	public void mouseReleased() {
		currentInterface.mouseReleased();
	}
	
	public void mouseMoved() {}
	
	public void focusLost() {
		if (currentInterface != null)
			currentInterface.onFocusChange(false);
	}
	
	public void focusGained() {
		if (currentInterface != null)
			currentInterface.onFocusChange(true);
		else
			focusGainedEventWaiting = true;
	}
	
	// --- Private stuff -> KEEP OUT ---
	
	private void setInterface(Interface view) {
		if (currentInterface != null)
			currentInterface.onHide();
		currentInterface = view;
		currentInterface.onShow();
		if (focusGainedEventWaiting)
			currentInterface.onFocusChange(true);
	}
}