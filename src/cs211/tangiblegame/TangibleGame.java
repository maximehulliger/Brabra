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
	//private static final int ratioSize = 4; //généralement de 2 (640x360) à 6 (1920x1080)
	public static final float inclinaisonMax = PApplet.PI/5; 
	
	//--interne
	public ImageAnalyser imgAnalyser;
	private Interface currentInterface;
	public RealGame intRealGame;
	public TrivialGame intTrivialGame;
	public Calibration intCalibration;
	public Menu intMenu;
	public boolean hasPopup = false;
	public boolean ready = false;
	public boolean over = false;
	
	//----- setup et boucle d'update (draw)
	
	public static void main(String args[])
	{
		PApplet.main(new String[] { cs211.tangiblegame.TangibleGame.class.getName() });
	}
	
	public void setup() {
		ProMaster.init(this);
		size(1280, 720, P3D);
		float camZ = height / (2*tan(PI*60/360.0f));
		perspective(PI/3, width/(float)height, camZ/10, camZ*1000);
		imgAnalyser = new ImageAnalyser(this);
		thread("loadInterfaces");
		thread("imageProcessing");
		intMenu = new Menu();
		
		setInterface(intMenu);
		//Quaternion.test();
	}
	
	public void imageProcessing() {
		imgAnalyser.run();
	}
	
	public void loadInterfaces() {
		intRealGame = new RealGame();
		intTrivialGame = new TrivialGame();
		intCalibration = new Calibration();
		intRealGame.init();
		intTrivialGame.init();
		intCalibration.init();
		ready = true;
		System.out.println("ready !");
	}
	
	public void setInterface(Interface i) {
		currentInterface = i;
		i.wakeUp();
	}
	
	public void draw() {
		currentInterface.draw();
	}

	//-------- Gestion Evenements

	public void keyPressed() {
		//intercepte escape
		if (key == 27 && currentInterface != intMenu) {
			setInterface(intMenu);
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