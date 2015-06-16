package cs211.tangiblegame;

import java.util.concurrent.locks.ReentrantLock;

import cs211.tangiblegame.calibration.Calibration;
import cs211.tangiblegame.imageprocessing.ImageProcessing;
import cs211.tangiblegame.realgame.RealGame;
import cs211.tangiblegame.trivial.TrivialGame;
import processing.core.*;
import processing.event.MouseEvent;


public class TangibleGame extends PApplet {
	private static final long serialVersionUID = 338280650599573653L;

	//--parametres
	private static final int ratioSize = 4; //généralement de 2 (640x360) à 5 (1920x1080)
	
	//--interne
	public ImageProcessing imgProcessing;
	private Interface currentInterface;
	public RealGame intRealGame;
	public TrivialGame intTrivialGame;
	public Calibration intCalibration;
	public Menu intMenu;
	public ReentrantLock applock;
	public boolean over = false;
	
	//----- setup et boucle d'update (draw)
	
	public void setup() {
		applock = new ReentrantLock();
		ProMaster.init(this);
		size(16*20*ratioSize, 9*20*ratioSize, P3D);
		imgProcessing = new ImageProcessing(this);
		//imgProcessing.start();
		thread("imageProcessing");
		applock.lock();
		intRealGame = new RealGame();
		intTrivialGame = new TrivialGame();
		intCalibration = new Calibration();
		intMenu = new Menu();
		
		setInterface(intMenu);
		//Quaternion.test();
		applock.unlock();
		
	}
	
	public void imageProcessing() {
		imgProcessing.run();
	}
	
	public void setInterface(Interface i) {
		currentInterface = i;
		i.init();
	}
	
	public void draw() {
		applock.lock();
		currentInterface.draw();
		applock.unlock();
	}

	//-------- Gestion Evenements

	public void keyPressed() {
		//intercepte escape
		if (key == 27 && currentInterface != intMenu) {
			imgProcessing.play(false);
			imgProcessing.forced = false;
			ImageProcessing.displayQuadRejectionCause = false; 
			setInterface(intMenu);
			key = 0;
		} 
			
		//pour tous les jeux:
		if (currentInterface != intMenu) {
			if (key == 'q')
				currentInterface.init();
			if (key == 'Q')
				imgProcessing.resetParametres();
			if (key == 'p')
				imgProcessing.playOrPause();
			if (key=='i')
				imgProcessing.changeInput();
		}
		
		currentInterface.keyPressed();
	}  
	
	public void dispose() {
		if (imgProcessing.takeMovie && imgProcessing.pausedMov) {
			imgProcessing.play(true);
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