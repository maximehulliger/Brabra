package cs211.tangiblegame;

import cs211.tangiblegame.calibration.Calibration;
import cs211.tangiblegame.imageprocessing.ImageAnalyser;
import cs211.tangiblegame.realgame.RealGame;
import cs211.tangiblegame.trivial.TrivialGame;
import processing.core.*;
import processing.event.MouseEvent;


public class TangibleGame extends PApplet {
	public static final String name = "Brabra";
	public enum View {Menu, Calibration, TrivialGame, RealGame}

	//--- parametres
	private static final int windowSize = 4; //generaly from 2 (640x360) to  6 (1920x1080)
	public static final float inclinaisonMax = PApplet.PI/5; 
	public boolean imgAnalysis = false;
	
	//--- interne
	public String dataPath;
	public String inputPath;
	public ImageAnalyser imgAnalyser;
	private Interface currentInterface;
	public RealGame intRealGame;
	public TrivialGame intTrivialGame;
	public Calibration intCalibration;
	public Menu intMenu;
	public Input input = new Input();
	public boolean hasPopup = false;
	public boolean over = false;
	
	
	//----- setup et boucle d'update (draw)
	
	public static void main(String args[]) {
		PApplet.main(new String[] { cs211.tangiblegame.TangibleGame.class.getName() });
	}
	
	public void settings() {
		size(windowSize*320, windowSize*160, "processing.opengl.PGraphics3D");
		//surface.setResizable(true);
	}
	
	public void setup() {
		surface.setTitle(name);
		setPaths();
		ProMaster.init(this);
		
		float camZ = height / (2*tan(PI*60/360.0f));
		perspective(PI/3, width/(float)height, camZ/10, camZ*1000);
		
		imgAnalyser = new ImageAnalyser();
		if (imgAnalysis) {
			System.out.println("starting img analysis thread.");
			thread("imageProcessing");
		}
		
		setView(View.RealGame);
	}

	private void setPaths() {
		String base = dataPath("").substring(0, dataPath("").lastIndexOf(name)+name.length())+"/bin/";
		dataPath = base+"data/";
		inputPath = base+"input/";
		System.out.println("base path: "+base);
	}
	
	public void imageProcessing() {
		imgAnalyser.run();
	}
	
	public void setView(View view) {
		switch (view) {
		case Menu:
			if (intMenu == null) {
				intMenu = new Menu();
			}
			currentInterface = intMenu;
			break;
		case Calibration:
			if (intCalibration == null) {
				intCalibration = new Calibration();
				intCalibration.init();
			}
			currentInterface = intCalibration;
			break;
		case RealGame:
			if (intRealGame == null) {
				intRealGame = new RealGame();
				intRealGame.init();
			}
			currentInterface = intRealGame;
			break;
		case TrivialGame:
			if (intTrivialGame == null) {
				intTrivialGame = new TrivialGame();
				intTrivialGame.init();
			}
			currentInterface = intTrivialGame;
			break;
		}
		currentInterface.wakeUp();
	}
	
	public void draw() {
		currentInterface.draw();
		
		//gui
		camera();
		hint(PApplet.DISABLE_DEPTH_TEST);
		if (imgAnalysis && currentInterface!=intCalibration)
			imgAnalyser.gui();
		currentInterface.gui();
		hint(PApplet.ENABLE_DEPTH_TEST);
	}

	//-------- Gestion Evenements

	public void keyPressed() {
		//intercepte escape
		if (key == 27 && currentInterface != intMenu) {
			setView(View.Menu);
			key = 0;
			return;
		} else if (key == 'l')	//l -> load parameters
			imgAnalyser.imgProc.selectParameters();
		else if (key == 'q')
			currentInterface.init();
		else if (key == 'Q')
			imgAnalyser.resetAllParameters(true);
		else if (key == 'p')
			imgAnalyser.playOrPause();
		else if (key=='i')
			imgAnalyser.changeInput();
		
		input.keyPressed();
		currentInterface.keyPressed();
	}  
	
	public void dispose() {
		if (imgAnalyser.takeMovie && imgAnalyser.paused()) {
			imgAnalyser.play(true);
		}
		over = true;
		System.out.println("bye bye !");
		//exit();
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
}