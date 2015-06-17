package cs211.tangiblegame.calibration;

import processing.core.PApplet;
import processing.core.PFont;
import cs211.tangiblegame.Interface;
import cs211.tangiblegame.imageprocessing.ImageAnalyser;
import cs211.tangiblegame.calibration.HScrollbar;

public class Calibration extends Interface {
	private final static boolean displayParameters = true; //sortie console
	
	private ImageAnalyser ia;
	private int nbCaraThreshold = 12;
	private int nbCara = nbCaraThreshold + 4;
	private int caraBarsHeight = nbCara*20;
	private HScrollbar[] bar = new HScrollbar[nbCara];
	private PFont fontLabel;
	public PFont fontButtonMode;
	private String[] info = { "Hue min", "Hue max", "Brigh min", "Brigh max", "Satur min", "Satur max", 
			"r min", "r max", "g min", "g max", "b min", "b max", 
			"min vote", "neighbourhood", "nb lignes", "sobel threshold"};
	public boolean buttonCalibrationMode = false;
	
	public Calibration() {
		ia = app.imgAnalyser;
		fontLabel = app.createFont("Arial", 18, true);
		fontButtonMode = app.createFont("Arial", (app.height - caraBarsHeight - 75)/17, true);
	}

	public void init() {
		ia.restartMovie();
		ia.inputLock.lock();
		ImageAnalyser.displayQuadRejectionCause = true;
		ia.play(true);
		ia.forced = true;
		app.textFont(fontLabel) ;
		app.textAlign(PApplet.BASELINE);
		for (int i=0; i<nbCara; i++) {
			bar[i] = new HScrollbar(app, 0, app.height-caraBarsHeight+20*i, app.width, 20, app.imgAnalyser.parametres[i]);
		}
		ia.inputLock.unlock();
		
	}

	public void draw() {
		app.background(0);
		app.fill(255, 255);
		ia.imagesLock.lock();
		if (ia.inputImg != null) {
			int displayWid = app.width/3 + 1;
			int displayHei = app.height - caraBarsHeight - 75;
			
			ia.quadDetectionLock.lock();
			app.image(ia.quadDetection, 0, 0, displayWid, displayHei);
			ia.quadDetectionLock.unlock();
			
			if (buttonCalibrationMode) {
				if (ia.hasFoundQuad && ia.threshold2Button != null)
					app.image(ia.threshold2Button, displayWid, 0, displayWid, displayHei);
				app.textFont(fontButtonMode);
				app.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
				app.fill(200, 100, 0, 180);
				if (!ia.hasFoundQuad)
					app.text("button detection mode: need to detect the plate. ", displayWid*2, displayHei);
				else if ((ia.pausedMov && ia.takeMovie) || (ia.pausedCam && !ia.takeMovie))
					app.text("button detection mode  :)  ", displayWid*2, displayHei);
				else
					app.text("button detection mode (pause (p) to help yourself)  ", displayWid*2, displayHei);
				
			}
			else
				app.image(ia.threshold2g, displayWid, 0, displayWid, displayHei);
			
			app.image(ia.inputImg, 2*displayWid, 0, displayWid, displayHei);
		}
		ia.imagesLock.unlock();

		// update GUI
		app.fill(0);            
		for (int i=0; i<nbCara; i++) {
			bar[i].update();
			bar[i].display();
			app.textAlign(PApplet.BASELINE);
			app.text(info[i], 30,app.height-caraBarsHeight+17+20*i);
		}
	}
	
	public void keyPressed() {
		if (buttonCalibrationMode && app.key=='Q') {
			ia.buttonDetection.lock();
			for (int i=nbCaraThreshold; i<nbCara; i++) {
				bar[i].setEtat(ia.buttonDetection.paraBoutons[i]);
			}
			ia.buttonDetection.unlock();
			for (int i=0; i<nbCaraThreshold; i++) {
				bar[i].setEtat(ia.parametres[i]);
			}
		} else if ((app.key=='i' || app.key == 'Q')) {
			ia.inputLock.lock();
			for (int i=0; i<nbCara; i++) {
				bar[i].setEtat(ia.parametres[i]);
			}
			ia.inputLock.unlock();
			buttonCalibrationMode = false;
		}
		
		if (app.key=='b') {
			if (buttonCalibrationMode) {
				ia.inputLock.lock();
				for (int i=0; i<nbCara; i++) {
					bar[i].setEtat(ia.parametres[i]);
				}
				ia.inputLock.unlock();
			} else {
				ia.buttonDetection.lock();
				for (int i=0; i<nbCaraThreshold; i++) {
					bar[i].setEtat(ia.buttonDetection.paraBoutons[i]);
				}
				ia.buttonDetection.unlock();
				for (int i=nbCaraThreshold; i<nbCara; i++) {
					bar[i].setEtat(ia.parametres[i]);
				}
			}
			buttonCalibrationMode = !buttonCalibrationMode;
		}
	}
	
	public void mouseDragged() {
		if (HScrollbar.oneLocked) {
			ia.inputLock.lock();
			if (buttonCalibrationMode) {
				ia.buttonDetection.lock();
				for (int i=0; i<nbCaraThreshold; i++) {
					ia.buttonDetection.paraBoutons[i] = bar[i].getPos();
				}
				ia.buttonDetection.unlock();
				for (int i=nbCaraThreshold; i<nbCara; i++) {
					ia.parametres[i] = bar[i].getPos();
				}
			} else {
				for (int i=0; i<nbCara; i++) {
					ia.parametres[i] = bar[i].getPos();
				}
			}

			if (displayParameters) {
				System.out.println("----------------");
				if (buttonCalibrationMode) {
					for (int i=0; i<nbCaraThreshold/2; i++)
						System.out.printf(" %d: [%.2f, %.2f]\n", i, ia.buttonDetection.paraBoutons[2*i], ia.buttonDetection.paraBoutons[2*i+1]);
					for (int i=nbCaraThreshold/2; i<nbCara/2; i++)
						System.out.printf(" %d: [%.2f, %.2f]\n", i, ia.parametres[2*i], ia.parametres[2*i+1]);
				} else {
					for (int i=0; i<nbCara/2; i++)
						System.out.printf(" %d: [%.2f, %.2f]\n", i, ia.parametres[2*i], ia.parametres[2*i+1]);
				}
				System.out.println("=> "+ia.hough.lines.size()+" lignes");
			}
			ia.inputLock.unlock();
		}
	}
}
