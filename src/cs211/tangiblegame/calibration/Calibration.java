package cs211.tangiblegame.calibration;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import cs211.tangiblegame.Interface;
import cs211.tangiblegame.imageprocessing.ImageProcessing;
import cs211.tangiblegame.imageprocessing.TwoDThreeD;
import cs211.tangiblegame.calibration.HScrollbar;

public class Calibration extends Interface {

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
		app.textFont(fontLabel) ;
		app.textAlign(PApplet.BASELINE);
		for (int i=0; i<nbCara; i++) {
			bar[i] = new HScrollbar(app, 0, app.height-caraBarsHeight+20*i, app.width, 20, app.imgProcessing.parametres[i]);
		}
	}

	public void draw() {
		app.imgProcessing.update(true);
		app.fill(255, 255);
		if (ip.inputImg != null) {
			int displayWid = app.width/3;
			int displayHei = app.height - caraBarsHeight - 75;
			app.image(ip.quadDetection, 0, 0, displayWid, displayHei);
			app.image(ip.threshold2g, displayWid, 0, displayWid, displayHei);
			app.image(ip.inputImg, 2*displayWid, 0, displayWid, displayHei);
			
			// print values
			System.out.println("----------------");
			for (int i=0; i<nbCara/2; i++)
				System.out.printf(" %d: [%.2f, %.2f]\n", i, ip.parametres[2*i], ip.parametres[2*i+1]);
			System.out.println("=> "+ip.hough.lines.size()+" lignes");
			if (ip.hough.quad != null) {
				TwoDThreeD deathMasterLongSword = new TwoDThreeD(ip.inputImg.width, ip.inputImg.height);
				PVector rotation = deathMasterLongSword.get3DRotations(ip.hough.quad());
				float r = 360/PApplet.TWO_PI;
				System.out.printf("rot: x: %.1f y: %.1f z: %.1f (°)\n", rotation.x*r, rotation.y*r, rotation.z*r);
			}
		}

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
			for (int i=0; i<nbCara; i++) {
				bar[i].setEtat(app.imgProcessing.parametres[i]);
			}
		}
	}
	
	public void mouseDragged() {
		if (HScrollbar.oneLocked) {
			for (int i=0; i<nbCara; i++) {
				ip.parametres[i] = bar[i].getPos();
			}
		}
	}
}
