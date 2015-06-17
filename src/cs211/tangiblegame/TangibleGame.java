package cs211.tangiblegame;

import cs211.tangiblegame.calibration.Calibration;
import cs211.tangiblegame.imageprocessing.ImageAnalyser;
import cs211.tangiblegame.realgame.RealGame;
import cs211.tangiblegame.trivial.TrivialGame;
import processing.core.*;
import processing.event.MouseEvent;


public class TangibleGame extends PApplet {
	private static final long serialVersionUID = 338280650599573653L;

	//--parametres
	private static final int ratioSize = 4; //généralement de 2 (640x360) à 5 (1920x1080)
	public static final float inclinaisonMax = PApplet.PI/5; 
	
	//--interne
	public ImageAnalyser imgAnalyser;
	private Interface currentInterface;
	public RealGame intRealGame;
	public TrivialGame intTrivialGame;
	public Calibration intCalibration;
	public Menu intMenu;
	public boolean over = false;
	
	//----- setup et boucle d'update (draw)
	
	public void setup() {
		ProMaster.init(this);
		size(16*20*ratioSize, 9*20*ratioSize, P3D);
		imgAnalyser = new ImageAnalyser(this);
		//imgProcessing.start();
		thread("imageProcessing");
		intRealGame = new RealGame();
		intTrivialGame = new TrivialGame();
		intCalibration = new Calibration();
		intMenu = new Menu();
		
		setInterface(intMenu);
		//Quaternion.test();
	}
	
	public void imageProcessing() {
		imgAnalyser.run();
	}
	
	public void setInterface(Interface i) {
		currentInterface = i;
		i.init();
	}
	
	public void draw() {
		currentInterface.draw();
	}

	//-------- Gestion Evenements

	public void keyPressed() {
		//intercepte escape
		if (key == 27 && currentInterface != intMenu) {
			imgAnalyser.play(false);
			imgAnalyser.forced = false;
			imgAnalyser.detectButtons = false;
			
			ImageAnalyser.displayQuadRejectionCause = false; 
			setInterface(intMenu);
			key = 0;
		} 
			
		//pour tous les jeux:
		if (currentInterface != intMenu) {
			if (key == 'q')
				currentInterface.init();
			if (key == 'Q') {
				if (intCalibration.buttonCalibrationMode)
					imgAnalyser.buttonDetection.resetParameters();
				else
					imgAnalyser.resetParameters();
			}
			if (key == 'p')
				imgAnalyser.playOrPause();
			if (key=='i')
				imgAnalyser.changeInput();
		}
		
		currentInterface.keyPressed();
	}  
	
	public void dispose() {
		if (imgAnalyser.takeMovie && imgAnalyser.pausedMov) {
			imgAnalyser.play(true);
		}
		over = true;
		System.out.println("\n\nbye bye !");
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