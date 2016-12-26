package brabra.imageprocessing;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import brabra.ProMaster;
import brabra.game.physic.geo.Vector;
import brabra.Brabra;
import brabra.Master;
import brabra.imageprocessing.HoughLine;
import brabra.imageprocessing.TwoDThreeD;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Capture;
import processing.core.PApplet;

/** Maestro Class of the image processing part of the project. */
public class ImageAnalyser extends ProMaster {
	
	///--- parameters
	public static final float maxAcceptedAngle = 65f/360*PApplet.TWO_PI; //65°
	private static final int sleepTime = 200;
	private int idxCamera = 3;

	//--- input
	public final ReentrantLock inputLock = new ReentrantLock();
	private boolean detectButtons = false;
	public float[] parametres;
	private boolean forced = false;	//force l'analyse de l'image même si l'input n'a pas changé.
	private boolean paused = true;
	
	//--- images & quad detection (control img)
	public final ReentrantLock imagesLock = new ReentrantLock();
	public PImage inputImg;
	public PImage threshold2g;
	private PImage sobel;
	public final ReentrantLock quadDetectionLock = new ReentrantLock();
	public boolean hasFoundQuad = false;
	public PGraphics quadDetection = null;
	
	//--- button detection
	public final ButtonDetection buttonDetection;
	
	//--- rotation
	private final ReentrantLock rotationLock = new ReentrantLock();
	public boolean hasFoundRotation = false;
	private int rotationAge = 0;
	private Vector rotation = new Vector();
	private Vector lastRotation = new Vector();
	private Vector gameRotation = new Vector();
	
	//--- "interne" (img / calibration)
	public PFont standardFont;
	/*pkg*/ int inWidth, inHeight;
	private boolean newInput = false;
	private Capture cam = null;
	
	public ImageAnalyser() {
		app.imgAnalyser = this;
		buttonDetection = new ButtonDetection();
	}
	
	public void launch() {
		// preloaded pixel int value (to avoid weird processing shit with multiple threads.) loaded by ImageAnalyser
		ImageProcessing.color0 = app.color(0);
		ImageProcessing.color255 = app.color(255);
		ButtonDetection.colorButtonOk = app.color(0, 255, 0, 150);
		ButtonDetection.colorButtonRejected = app.color(255, 0, 0, 150);
		HoughLine.colorQuad = app.color(200, 100, 0, 120);
		
		// font & parametres
		standardFont = app.createFont("Arial", Brabra.height/40f);
		parametres = ImageProcessing.paraCameraBase.clone();
		
		// main loop
		while (!app.isOver()) {
			boolean newImage = false;
			inputLock.lock();
			imagesLock.lock();
			if (cam!=null && cam.available()) {
				cam.read();
				inputImg = cam.get();
				newImage = true;
			}
			
			if (newInput) {
				//video input initialisation (at first run)
				Capture newCam = null;
				final String[] cameras = Capture.list();
				final int idx = PApplet.min(idxCamera, cameras.length);
				final String camStr = cameras[idx];
				newCam = new Capture(app, camStr);
				cam = newCam;
				cam.start();
				while (!cam.available())
					app.delay(100);
				cam.read();
				inputImg = cam.get();
				newInput = false;
			}

			if ( (newImage && !paused) || (forced && inputImg != null)) {
				
				// analyse input to find a green quad
				PImage bluredg = ImageProcessing.blur(inputImg);
				PImage threshold1g = ImageProcessing.colorThreshold(bluredg, parametres[0], parametres[1], parametres[2], parametres[3], parametres[4], parametres[5]);
				threshold2g = ImageProcessing.intensityThreshold(threshold1g, parametres[6], parametres[7], parametres[8], parametres[9], parametres[10], parametres[11]);
				sobel = ImageProcessing.sobel(threshold2g, parametres[15]);
				HoughLine.minVotes = PApplet.round(parametres[12]);
				HoughLine.neighbourhood = PApplet.round(parametres[13]);
				HoughLine.maxKeptLines = PApplet.round(parametres[14]);
				inputLock.unlock();
				HoughLine hough = new HoughLine(sobel, app);
				
				hasFoundQuad = hough.quad != null;
				
				// if quad is found, analyse buttons & rotation (with image lock release)
				ArrayList<Vector> detectedQuad = null;
				if (hasFoundQuad) {
					//-- finish with images & start button det.
					detectedQuad = hough.quad();
					Thread buttonDetectionThread = null;
					if (detectButtons)
						buttonDetectionThread = Master.launch(() -> buttonDetection.detect(inputImg, hough.quad));
					imagesLock.unlock();

					//-- compute & set rotation
					TwoDThreeD deathMasterLongSword = new TwoDThreeD(inWidth, inHeight);
					Vector newRot = deathMasterLongSword.get3DRotations(detectedQuad);
					if (isConstrained(newRot.x, -maxAcceptedAngle, maxAcceptedAngle) &&
							isConstrained(newRot.x, -maxAcceptedAngle, maxAcceptedAngle) &&
							isConstrained(newRot.x, -maxAcceptedAngle, maxAcceptedAngle)) {
						rotationLock.lock();
						if (!lastRotation.equals(newRot) ) {
							lastRotation = rotation;
							rotation = newRot;
							gameRotation = lastRotation.plus(rotation).div(2); //moyenne des 2 dernières entrÃ©es
							gameRotation.mult(-Brabra.inclinaisonMax / maxAcceptedAngle);
						}
						rotationAge = 0;
						hasFoundRotation = true;
						rotationLock.unlock();
					} else {
						hasFoundRotation = false;
					}
					
					if (detectButtons) {
						try {
							buttonDetectionThread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					imagesLock.lock();
				} 
				if (!hasFoundQuad || !hasFoundRotation) { // no good input -> old the whole !
					rotationLock.lock();
					if (rotationAge++ == 6) {
						gameRotation = zero.copy();
					}
					rotationLock.unlock();

					buttonDetection.resetOutput();
				}

				//-- print control img
				quadDetectionLock.lock();
				if (quadDetection == null) {
					inWidth = inputImg.width;
					inHeight = inputImg.height;
					quadDetection = app.createGraphics(inWidth, inHeight);
				}
				quadDetection.beginDraw();
				quadDetection.fill(255, 255);
				quadDetection.image(sobel, 0, 0);
				hough.drawLines(quadDetection);
				if (hasFoundQuad) {
					hough.drawQuad(quadDetection);
					imagesLock.unlock();
					buttonDetection.drawButtons(quadDetection);
				} else
					imagesLock.unlock();
				quadDetection.endDraw();
				quadDetectionLock.unlock();
			} else {
				inputLock.unlock();
				imagesLock.unlock();
				app.delay(sleepTime); //dort sleepTime
			}
		}
	}
	
	// --- getters ---
	
	public boolean running() {
		return !paused;
	}

	public Vector rotation() {
		rotationLock.lock();
		Vector rotation = gameRotation.copy();
		rotationLock.unlock();
		return rotation;
	}

	// ----- interaction response
	
	public void resetAllParameters(boolean movieToo) {
		inputLock.lock();
		parametres = ImageProcessing.paraCameraBase.clone();
		buttonDetection.paraBoutons = ImageProcessing.paraBoutonsBase.clone();
		newInput = true;
		inputLock.unlock();
	}
	
	public void play(boolean detectButtons, boolean forced) {
		assert paused;
		
		if (!app.hasImgAnalysis())
			app.setImgAnalysis(true);
		
		inputLock.lock();
		
		this.detectButtons = detectButtons;
		this.forced = forced;
		this.paused = false;
		
		if (cam == null)
			newInput = true;
		else
			cam.start();
		
		inputLock.unlock();
	}
	
	public void stop() {
		assert !paused;
		
		paused = true;
		
		if (cam != null)
			cam.stop();
		
		// resetOutput
		gameRotation = zero.copy();
		hasFoundQuad = false;
		buttonDetection.resetOutput();
	}

	public void playOrPause() {
		if (paused)
			play(detectButtons, forced);
		else
			stop();
	}
	
	/** Display the state of the image analysis on the top left of the screen. */
	public void gui() {
		app.textFont(standardFont);
		app.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
		quadDetectionLock.lock();
		if (quadDetection == null) {
			quadDetectionLock.unlock();
			app.fill(0, 0, 0);
			app.rect(20, 20, Brabra.width/6f, Brabra.height/6f);
			app.fill(200, 100, 0, 180);
			app.text("coming...", 20 + Brabra.width/6f, 20 + Brabra.height/6f);
		} else {
			app.fill(255);
			if (hasFoundQuad && !hasFoundRotation)		//rotation is to big
				app.tint(255, 0, 0);
			app.image(quadDetection, 20, 20, Brabra.width/6f, Brabra.height/6f);
			quadDetectionLock.unlock();
			app.tint(255, 255);
			if (paused) {
				app.fill(200, 100, 0, 180);
				app.text("paused", 20 + Brabra.width/6f, 20 + Brabra.height/6f);
			}
		}
	}
}


