package cs211.tangiblegame.calibration;

import processing.core.PApplet;
import processing.core.PFont;
import cs211.tangiblegame.Interface;
import cs211.tangiblegame.imageprocessing.ImageProcessing;
import cs211.tangiblegame.calibration.HScrollbar;

public class Calibration extends Interface {
	private final static boolean displayParameters = false; //sortie console
	
	private ImageProcessing ip;
	private int nbCara = 16;
	private int caraBarsHeight = nbCara*20;
	private HScrollbar[] bar = new HScrollbar[nbCara];
	private PFont fontLabel;
	private String[] info = { "Hue min", "Hue max", "Brigh min", "Brigh max", "Satur min", "Satur max", 
			"r min", "r max", "g min", "g max", "b min", "b max", 
			"min vote", "neighbourhood", "nb lignes", "sobel threshold"};

	public Calibration() {
		ip = app.imgProcessing;
		fontLabel = app.createFont("Arial", 18, true);
	}

	public void init() {
		ip.restartMovie();
		ip.inputLock.lock();
		ImageProcessing.displayQuadRejectionCause = true;
		app.textFont(fontLabel) ;
		app.textAlign(PApplet.BASELINE);
		ip.play(true);
		ip.forced = true;
		for (int i=0; i<nbCara; i++) {
			bar[i] = new HScrollbar(app, 0, app.height-caraBarsHeight+20*i, app.width, 20, app.imgProcessing.parametres[i]);
		}
		ip.inputLock.unlock();
		
	}

	public void draw() {
		app.background(0);
		app.fill(255, 255);
		ip.imagesLock.lock();
		if (ip.inputImg != null) {
			int displayWid = app.width/3 + 1;
			int displayHei = app.height - caraBarsHeight - 75;
			
			ip.quadDetectionLock.lock();
			app.image(ip.quadDetection, 0, 0, displayWid, displayHei);
			ip.quadDetectionLock.unlock();
				
			app.image(ip.threshold2g, displayWid, 0, displayWid, displayHei);
			app.image(ip.inputImg, 2*displayWid, 0, displayWid, displayHei);
		}
		ip.imagesLock.unlock();

		// update GUI
		app.fill(0);            
		for (int i=0; i<nbCara; i++) {
			bar[i].update();
			ip.parametres[i] = bar[i].getPos();
			bar[i].display();
			app.text(info[i], 30,app.height-caraBarsHeight+17+20*i);
		}
	}
	
	public void keyPressed() {
		//pour tous les jeux:
		if (app.key=='i' || app.key == 'Q') {
			ip.inputLock.lock();
			for (int i=0; i<nbCara; i++) {
				bar[i].setEtat(ip.parametres[i]);
			}
			ip.inputLock.unlock();
		}
	}
	
	public void mouseDragged() {
		if (HScrollbar.oneLocked) {
			ip.inputLock.lock();
			for (int i=0; i<nbCara; i++) {
				ip.parametres[i] = bar[i].getPos();
			}

			if (displayParameters) {
				System.out.println("----------------");
				for (int i=0; i<nbCara/2; i++)
					System.out.printf(" %d: [%.2f, %.2f]\n", i, ip.parametres[2*i], ip.parametres[2*i+1]);
				System.out.println("=> "+ip.hough.lines.size()+" lignes");
			}
			ip.inputLock.unlock();
		}
	}
}
