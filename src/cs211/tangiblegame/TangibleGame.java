package cs211.tangiblegame;

import cs211.tangiblegame.calibration.Calibration;
import cs211.tangiblegame.imageprocessing.ImageAnalyser;
import cs211.tangiblegame.realgame.RealGame;
import cs211.tangiblegame.trivial.TrivialGame;
import processing.core.*;
import processing.event.MouseEvent;


public class TangibleGame extends PApplet {
	public static final String name = "Brabra";
	//private static final long serialVersionUID = 338280650599573653L;
	public enum View {Menu, Calibration, TrivialGame, RealGame}

	//--parametres
	//private static final int ratioSize = 4; //généralement de 2 (640x360) à 6 (1920x1080)
	public static final float inclinaisonMax = PApplet.PI/5; 
	public static final boolean imgAnalysis = false;
	
	//--interne
	public ImageAnalyser imgAnalyser;
	private Interface currentInterface;
	public RealGame intRealGame;
	public TrivialGame intTrivialGame;
	public Calibration intCalibration;
	public Menu intMenu;
	public boolean hasPopup = false;
	public boolean over = false;
	
	public static String dataPath;
	public static String inputPath;
	
	//----- setup et boucle d'update (draw)
	
	public static void main(String args[]) {
		PApplet.main(new String[] { cs211.tangiblegame.TangibleGame.class.getName() });
	}
	
	public void settings() {
		size(1280, 720, "processing.opengl.PGraphics3D");
	}
	
	public void setup() {
		surface.setTitle(name);
		setPaths();
		ProMaster.init(this);
		float camZ = height / (2*tan(PI*60/360.0f));
		perspective(PI/3, width/(float)height, camZ/10, camZ*1000);
		
		imgAnalyser = new ImageAnalyser(this);
		if (imgAnalysis)
			thread("imageProcessing");
		
		setView(View.RealGame);
	}
	
	private void setPaths() {
		String base = dataPath("").substring(0, dataPath("").lastIndexOf(name)+name.length())+"\\bin\\";
		dataPath = base+"data\\";
		inputPath = base+"input\\";
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
	}

	//-------- Gestion Evenements

	public void keyPressed() {
		//intercepte escape
		if (key == 27 && currentInterface != intMenu) {
			setView(View.Menu);
			key = 0;
		} 
		
		//l ou s -> load ou save parameters
		if (key == 'l') {
			imgAnalyser.imgProc.selectParameters();
		}
			
		//pour tous les jeux:
		if (currentInterface != intMenu) {
			if (key == 'q')
				currentInterface.init();
			if (key == 'Q') {
				imgAnalyser.resetAllParameters(true);
			}
			
			if (key == 'p')
				imgAnalyser.playOrPause();
			if (key=='i')
				imgAnalyser.changeInput();
		}
		
		currentInterface.keyPressed();
	}  
	
	public void dispose() {
		if (imgAnalyser.takeMovie && imgAnalyser.paused()) {
			imgAnalyser.play(true);
		}
		over = true;
		System.out.println("bye bye !");
	} 

	public void mouseDragged() {
		currentInterface.mouseDragged();
	}

	public void mouseWheel(MouseEvent event) {
		currentInterface.mouseWheel(event);
	}

	public void keyReleased() {
		currentInterface.keyReleased();
	}

	public void mousePressed() {
		currentInterface.mousePressed();
	}
	
	public void mouseReleased() {
		currentInterface.mouseReleased();
	}
}