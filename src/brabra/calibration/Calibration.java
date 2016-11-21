package brabra.calibration;

import brabra.Interface;
import brabra.Brabra;
import brabra.calibration.HScrollbar;
import brabra.imageprocessing.ImageAnalyser;
import brabra.imageprocessing.ImageProcessing;
import processing.core.PApplet;
import processing.core.PFont;

public class Calibration extends Interface {
	private ImageAnalyser ia;
	private HScrollbar[] bar;
	private float[] currentPara;
	private PFont fontLabel;
	public PFont fontImages;
	private int caraBarsHeight;
	private String[] infoInput = { "Hue min", "Hue max", "Brigh min", "Brigh max", "Satur min", "Satur max", 
			"r min", "r max", "g min", "g max", "b min", "b max", 
			"min vote", "neighbourhood", "nb lignes", "sobel threshold"};
	private String[] infoButton = { "Hue min", "Hue max", "Brigh min", "Brigh max", "Satur min", "Satur max", 
			"r min", "r max", "g min", "g max", "b min", "b max", 
			"min vote left", "max vote left", "min vote right", "max vote right"};
	private String[] currentInfo;
	public boolean buttonCalibrationMode = false;
	
	public Calibration() {
		
	}

	public void onShow() {
		ia = app.imgAnalyser;
		if (fontLabel == null) {
			fontLabel = app.createFont("Arial", 18, true);
			fontImages = app.createFont("Arial", (Brabra.height - 275)/17, true);
		}
		ia.play(true, true);
		updateCurrentPara();
		createBars();
	}
	
	public void onHide() {
		app.setImgAnalysis(false);
	}
	
	/** create bars from current state and currentPara */
	public void createBars() {
		caraBarsHeight = currentPara.length*20;
		bar = new HScrollbar[currentPara.length];
		
		float[] specialParaEtatMax;
		if (buttonCalibrationMode)
			specialParaEtatMax = ImageProcessing.buttonParaMaxValue;
		else
			specialParaEtatMax = ImageProcessing.inputParaMaxValue;
		
		int i = 0;
		for (; i<ImageProcessing.nbParaBase; i++) {
			bar[i] = new HScrollbar(app, 0, Brabra.height-caraBarsHeight+20*i, Brabra.width, 20, 
					currentPara[i], ImageProcessing.basicParaMaxValue);
		}
		for (; i<ImageProcessing.nbParaBase+specialParaEtatMax.length; i++) {
			bar[i] = new HScrollbar(app, 0, Brabra.height-caraBarsHeight+20*i, Brabra.width, 20, 
					currentPara[i], specialParaEtatMax[i-ImageProcessing.nbParaBase]);
		}
	}

	public void draw() {
		app.background(0);
		app.fill(255, 255);
		ia.imagesLock.lock();
		if (ia.inputImg != null) {
			int displayWid = Brabra.width/3 + 1;
			int displayHei = Brabra.height - caraBarsHeight - 75;
			
			ia.quadDetectionLock.lock();
			app.image(ia.quadDetection, 0, 0, displayWid, displayHei);
			ia.quadDetectionLock.unlock();
			
			app.textFont( ia.standardFont );
			app.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
			app.fill(200, 100, 0, 180);
			
			if (!ia.running())
				app.text("paused", displayWid, displayHei);
			
			if (buttonCalibrationMode) {
				if (ia.hasFoundQuad && ia.buttonDetection.threshold2Button != null)
					app.image(ia.buttonDetection.threshold2Button, displayWid, 0, displayWid, displayHei);
				if (!ia.hasFoundQuad)
					app.text("button detection mode: need to detect the plate. ", displayWid*2, displayHei);
				else if (!ia.running())
					app.text("button detection mode  :)  ", displayWid*2, displayHei);
				else
					app.text("button detection mode (pause (p) to help yourself)  ", displayWid*2, displayHei);
				
			} else
				app.image(ia.threshold2g, displayWid, 0, displayWid, displayHei);
			
			app.image(ia.inputImg, 2*displayWid, 0, displayWid, displayHei);
		}
		ia.imagesLock.unlock();

		// update GUI
		app.fill(0);            
		app.textAlign(PApplet.BASELINE);
		app.textFont(fontLabel) ;
		for (int i=0; i<currentPara.length; i++) {
			bar[i].update();
			bar[i].display();
			app.text(currentInfo[i], 30, Brabra.height-caraBarsHeight+17+20*i);
		}
	}
	
	public void keyPressed() {
		if (app.key == 'q') {
			ia.inputLock.lock();
			if (buttonCalibrationMode)
				ia.parametres = ia.imgProc.paraCameraBase.clone();
			else {
				ia.buttonDetection.inputLock.lock();
				ia.buttonDetection.paraBoutons = ia.imgProc.paraBoutonsBase.clone();
				ia.buttonDetection.inputLock.unlock();
			}
			ia.inputLock.unlock();
		} else if (app.key=='Q')		//all parameters reset -> update etat bars
			updateBars();
		else if (app.key=='b') {
			buttonCalibrationMode = !buttonCalibrationMode;
			updateCurrentPara();
			createBars();
		} else if (app.key == 'l')
			updateBars();
		else if (app.key == 's')
			app.imgAnalyser.imgProc.saveParameters();
		else if (app.key == 'r') {
			boolean even = true;
			for (int i=0; i<ImageProcessing.nbParaBase; i++) {
				currentPara[i] = even ? 0 : ImageProcessing.basicParaMaxValue;
				even = !even;
			}
			updateBars();
		}
	}
	
	/** update bars & currentPara from img analyser */
	public void updateBars() {
		updateCurrentPara();
		//update base & special bars
		for (int i=0; i<currentPara.length; i++)
			bar[i].setEtat(currentPara[i]);
	}
	
	private void updateCurrentPara() {
		if (buttonCalibrationMode) {
			currentPara = ia.buttonDetection.paraBoutons;
			currentInfo = infoButton;
		} else {
			currentPara = ia.parametres;
			currentInfo = infoInput;
		}
	}
	
	public void mouseDragged() {
		if (HScrollbar.oneLocked) { //write bars values
			if (buttonCalibrationMode)
				ia.buttonDetection.inputLock.lock();
			else
				ia.inputLock.lock();

			for (int i=0; i<currentPara.length; i++) {
				currentPara[i] = bar[i].getEtat();
			}

			if (buttonCalibrationMode)
				ia.buttonDetection.inputLock.unlock();
			else
				ia.inputLock.unlock();
		}
	}
}
