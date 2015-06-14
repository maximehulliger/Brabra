package cs211.tangiblegame;

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
	
	public boolean paused = false; 	//pour la vidéo
	
	//----- setup et boucle d'update (draw)

	public void setup() {
		ProMaster.init(this);
		size(16*20*ratioSize, 9*20*ratioSize, P3D);
		imgProcessing = new ImageProcessing();
		intRealGame = new RealGame();
		intTrivialGame = new TrivialGame();
		intCalibration = new Calibration();
		intMenu = new Menu();
		
		setInterface(intTrivialGame);
		
		//Quaternion.test();
	}
	
	public void setInterface(Interface i) {
		currentInterface = i;
		i.init();
	}
	
	public void draw() {
		currentInterface.draw();
	}

	//-------- Gestion Evenements

	public void mouseDragged() {
		currentInterface.mouseDragged();
	}

	public void mouseWheel(MouseEvent event) {
		currentInterface.mouseWheel(event);
	}

	public void keyReleased() {
		currentInterface.keyReleased();
	}

	public void keyPressed() {
		if (key == 27 && currentInterface != intMenu) {
			setInterface(intMenu);
			key = 0;
		}
			
		//pour tous les jeux:
		if (currentInterface != null) //TODO pas menu !
		{
			//q: recommence la partie
			if (key == 'q')
				currentInterface.init();
			//p: met en pause la vidéo
			if (imgProcessing.takeMovie && key == 'p') {
				if (paused)
					imgProcessing.mov.play();
				else 
					imgProcessing.mov.pause();
				paused = !paused;
			}
			if (key=='i')
				imgProcessing.changeInput();
		}

		currentInterface.keyPressed();
	}  

	public void mouseReleased() {
		currentInterface.mouseReleased();
	}
}