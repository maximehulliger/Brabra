package brabra.calibration;

import brabra.Interface;
import brabra.Master;
import brabra.Brabra;
import brabra.calibration.HScrollbar;
import brabra.imageprocessing.ImageAnalyser;
import brabra.imageprocessing.ImageProcessing;
import processing.core.PApplet;
import processing.core.PFont;

public class Calibration extends Interface {
	private static final int selectionCursorSizeNormal = 10;
	private static final int selectionCursorSizeButton = 5;
	private static final int caraBarsHeight = ImageProcessing.paraCameraBase.length*20;
	private static final int displayWidth = Brabra.width/3 + 1;
	private static final int displayHeight = Brabra.height - caraBarsHeight - 75;
	private static final String[] infoInput = { "Hue min", "Hue max", "Brigh min", "Brigh max", "Satur min", "Satur max", 
			"r min", "r max", "g min", "g max", "b min", "b max", 
			"min vote", "neighbourhood", "nb lignes", "sobel threshold"};
	private static final String[] infoButton = { "Hue min", "Hue max", "Brigh min", "Brigh max", "Satur min", "Satur max", 
			"r min", "r max", "g min", "g max", "b min", "b max", 
			"min vote left", "max vote left", "min vote right", "max vote right"};
	
	private ImageAnalyser ia;
	private HScrollbar[] bar;
	private float[] currentPara;
	private PFont fontLabel;
	public PFont fontImages;
	
	private String[] currentInfo;
	public boolean buttonCalibrationMode = false;

	public void onShow() {
		ia = app.imgAnalyser;
		if (fontLabel == null) {
			fontLabel = app.createFont("Arial", 18, true);
			fontImages = app.createFont("Arial", (Brabra.height - 275)/17, true);
		}
		ia.play(true, true);
		updateCurrentPara();
		
		// create Bars
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
	
	public void onHide() {
		app.setImgAnalysis(false);
	}
	
	public void draw() {
		app.background(0);
		app.fill(255, 255);
		ia.imagesLock.lock();
		if (ia.inputImg != null) {
			
			ia.quadDetectionLock.lock();
			app.image(ia.quadDetection, 0, 0, displayWidth, displayHeight);
			ia.quadDetectionLock.unlock();
			
			app.textFont( ia.standardFont );
			app.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
			app.fill(200, 100, 0, 180);
			
			if (!ia.running())
				app.text("paused", displayWidth, displayHeight);
			
			if (buttonCalibrationMode) {
				if (ia.hasFoundQuad && ia.buttonDetection.threshold2Button != null)
					app.image(ia.buttonDetection.threshold2Button, displayWidth, 0, displayWidth, displayHeight);
				if (!ia.hasFoundQuad)
					app.text("button detection mode: need to detect the plate. ", displayWidth*2, displayHeight);
				else if (!ia.running())
					app.text("button detection mode  :)  ", displayWidth*2, displayHeight);
				else
					app.text("button detection mode (pause (p) to help yourself)  ", displayWidth*2, displayHeight);
				
			} else
				app.image(ia.threshold2g, displayWidth, 0, displayWidth, displayHeight);
			
			app.image(ia.inputImg, 2*displayWidth, 0, displayWidth, displayHeight);
			
			// draw the cursor for color selection
			if (app.mouseX > 2*displayWidth && app.mouseY < displayHeight) {
				final int selectionCursorSize = buttonCalibrationMode ? selectionCursorSizeButton : selectionCursorSizeNormal;
				app.rectMode(PApplet.CENTER);
				app.rect(app.mouseX, app.mouseY, selectionCursorSize, selectionCursorSize);
				app.rectMode(PApplet.CORNER);
			}
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
				ia.parametres = ImageProcessing.paraCameraBase.clone();
			else {
				ia.buttonDetection.inputLock.lock();
				ia.buttonDetection.paraBoutons = ImageProcessing.paraBoutonsBase.clone();
				ia.buttonDetection.inputLock.unlock();
			}
			ia.inputLock.unlock();
		} else if (app.key=='Q')		//all parameters reset -> update etat bars
			updateBars();
		else if (app.key=='b') {
			buttonCalibrationMode = !buttonCalibrationMode;
			updateCurrentPara();
			updateBars();
		} else if (app.key == 'l')
			updateBars();
		else if (app.key == 's')
			ImageProcessing.saveParameters();
		else if (app.key == 'r') {
			boolean even = true;
			for (int i=0; i<ImageProcessing.nbParaBase; i++) {
				currentPara[i] = even ? ImageProcessing.basicParaMaxValue : 0;
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

			for (int i=0; i<currentPara.length; i++)
				currentPara[i] = bar[i].getEtat();

			if (buttonCalibrationMode)
				ia.buttonDetection.inputLock.unlock();
			else
				ia.inputLock.unlock();
		} else {
			if (app.mouseX > 2*displayWidth && app.mouseY < displayHeight) {
				final int selectionCursorSize = buttonCalibrationMode ? selectionCursorSizeButton : selectionCursorSizeNormal;
				final float[] parametres = buttonCalibrationMode ? ia.buttonDetection.paraBoutons : ia.parametres;
				final int mouseOnImgX = (int)((app.mouseX - 2*displayWidth)*ia.inputImg.width/displayWidth);
				final int mouseOnImgY = (int)((app.mouseY)*ia.inputImg.height/displayHeight);
				final int startX = Master.max(0, mouseOnImgX-selectionCursorSize/2);
				final int startY = Master.max(0, mouseOnImgY-selectionCursorSize/2);
				final int endX = Master.min(displayWidth, mouseOnImgX+selectionCursorSize/2);
				final int endY = Master.min(displayHeight, mouseOnImgY+selectionCursorSize/2);
				ia.inputLock.lock();
				for (int x = startX; x<endX; x++)
					for (int y = startY; y<endY; y++) {
						final int p = ia.inputImg.get(x, y);
						float[] pVals = new float[] {
								app.hue(p), app.saturation(p), app.brightness(p),
								app.red(p), app.green(p), app.blue(p) };
						
						for (int i=0; i<6; i++) {
							final int paraMinIdx = i*2;
							final int paraMaxIdx = paraMinIdx+1;
							if (pVals[i] < parametres[paraMinIdx])
								parametres[paraMinIdx] = pVals[i]-1;
							if (pVals[i] > parametres[paraMaxIdx])
								parametres[paraMaxIdx] = pVals[i]+1;
						}
					}
				ia.inputLock.unlock();
				updateBars();
			}
		}
	}
}
