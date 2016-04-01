package brabra.imageprocessing;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import brabra.ProMaster;
import brabra.game.physic.geo.Vector;
import brabra.Brabra;
import brabra.imageprocessing.HoughLine;
import brabra.imageprocessing.TwoDThreeD;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Capture;
import processing.video.Movie;
import processing.core.PApplet;

/** Maestro Class of the image processing part of the project. */
public class ImageAnalyser extends ProMaster {
	///--- parameters
	public static final float maxAcceptedAngle = 65f/360*PApplet.TWO_PI; //65°
	public static boolean displayQuadRejectionCause = false;
	private static final int sleepTime = 200;
	private int idxCamera = 3;

	//--- input
	public final ReentrantLock inputLock = new ReentrantLock();
	public boolean detectButtons = false;
	public boolean takeMovie = false; //prend la camera si false.
	public float[] parametres, paraMovie, paraCamera;
	public boolean forced = false;	//force l'analyse de l'image même si l'input n'a pas changé.
	private boolean pausedCam = true, pausedMov = true;
	
	//--- images & quad detection (control img)
	public final ReentrantLock imagesLock = new ReentrantLock();
	public final ImageProcessing imgProc = new ImageProcessing();
	public PImage inputImg;
	public PImage threshold2g;
	public PImage sobel;
	public HoughLine hough;
	public final ReentrantLock quadDetectionLock = new ReentrantLock();
	public boolean hasFoundQuad = false;
	public PGraphics quadDetection = null;
	
	//--- button detection
	public final ButtonDetection buttonDetection;
	public void runButtonDetection() {
		buttonDetection.detect();
	}
	
	//--- rotation
	private final ReentrantLock rotationLock = new ReentrantLock();
	public boolean hasFoundRotation = false;
	private int rotationAge = 0;
	private Vector rotation = new Vector();
	private Vector lastRotation = new Vector();
	private Vector gameRotation = new Vector();
	
	//--- "interne" (img / calibration)
	public PFont standardFont;
	/*pkg*/ float strockWeight;
	/*pkg*/ int inWidth, inHeight;
	private boolean newInput = false;
	private Movie mov = null;
	private Capture cam = null;
	
	public ImageAnalyser() {
		app.imgAnalyser = this;
		
		// preloaded pixel int value (to avoid weird processing shit with multiple threads.) loaded by ImageAnalyser
		ImageProcessing.color0 = app.color(0);
		ImageProcessing.color255 = app.color(255);
		ButtonDetection.colorButtonOk = app.color(0, 255, 0, 150);
		ButtonDetection.colorButtonRejected = app.color(255, 0, 0, 150);
		HoughLine.colorQuad = app.color(200, 100, 0, 120);
		
		// font & parametres
		standardFont = app.createFont("Arial", Brabra.height/40f);
		paraMovie = ImageProcessing.paraMovieBase.clone();
		paraCamera = imgProc.paraCameraBase.clone();
		if (takeMovie)
			parametres = paraMovie;
		else
			parametres = paraCamera;
		
		buttonDetection = new ButtonDetection();
	}
	
	// --- getters ---

	public boolean paused() {
		return (pausedMov && takeMovie) || (pausedCam && !takeMovie);
	}
	
	public boolean running() {
		return app.hasImgAnalysis() && !paused();
	}

	public Vector rotation() {
		try {
			rotationLock.lock();
			return gameRotation.copy();
		} finally {
			rotationLock.unlock();
		}
	}

	// --- main loop ---
	public void run() {
		while (!app.isOver()) {
			boolean newImage = false;
			boolean once = false;
			inputLock.lock();
			imagesLock.lock();
			if (takeMovie && (!pausedMov || newInput) && (mov!=null) ) {
				newImage = true;
				mov.read();
				inputImg = mov.get();

			} else if (!takeMovie && (!pausedCam || newInput) && (cam!=null && cam.available()) ) {
				newImage = true;
				cam.read();
				inputImg = cam.get();
			}
			
			if ( newImage && newInput ) {
				updateForInput();
				once = true;
			}

			if ( (newImage && !paused()) || (forced && inputImg != null) || once ) {
				
				// analyse input to find a green quad
				PImage threshold1g = ImageProcessing.colorThreshold(inputImg, parametres[0], parametres[1], parametres[2], parametres[3], parametres[4], parametres[5]);
				PImage bluredg = ImageProcessing.blur(threshold1g);
				threshold2g = ImageProcessing.intensityThreshold(bluredg, parametres[6], parametres[7], parametres[8], parametres[9], parametres[10], parametres[11]);
				sobel = ImageProcessing.sobel(threshold2g, parametres[15]);
				HoughLine.minVotes = PApplet.round(parametres[12]);
				HoughLine.neighbourhood = PApplet.round(parametres[13]);
				HoughLine.maxKeptLines = PApplet.round(parametres[14]);
				boolean detectButtonsInt = detectButtons && !takeMovie;
				inputLock.unlock();
				hough = new HoughLine(sobel, app);
				
				hasFoundQuad = hough.quad != null;
				
				// if quad is found, analyse buttons & rotation (with image lock release)
				ArrayList<Vector> detectedQuad = null;
				if (hasFoundQuad) {
					//-- finish with images & start button det.
					detectedQuad = hough.quad();
					if (detectButtonsInt) {
						System.out.println("new button det job");
						app.thread("runButtonDetection");
					}
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
							gameRotation = lastRotation.plus(rotation).div(2); //moyenne des 2 dernières entrées
							gameRotation.mult(-Brabra.inclinaisonMax / maxAcceptedAngle);
							//on adoucis les angles d'entr�e (pour + de contr�le proche de 0)
							float r = 360/PApplet.TWO_PI;
							System.out.printf("rot: x: %.1f y: %.1f z: %.1f (°)\n", rotation.x*r, rotation.y*r, rotation.z*r);	
						}
						rotationAge = 0;
						hasFoundRotation = true;
						rotationLock.unlock();
					} else {
						hasFoundRotation = false;
						if (displayQuadRejectionCause)
							System.out.println("angle trop grand !");
					}
					
					//-- get & set button state
					if (detectButtonsInt) {
						buttonDetection.jobOverLock.lock();
						buttonDetection.jobOverLock.unlock();
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
	
	// ----- interaction response

	private void updateForInput() {
		if (newInput) {
			//image size dependant parameters update
			if (quadDetection == null || quadDetection.width != inputImg.width || quadDetection.height != inputImg.height) {
				inWidth = inputImg.width;
				inHeight = inputImg.height;
				quadDetection = app.createGraphics(inWidth, inHeight);
				strockWeight = PApplet.max(inWidth * 3f / Brabra.width, 1f);
				quadDetection.strokeWeight(strockWeight);
			}
			newInput = false;
		}
	}
	
	public void restartMovie() {
		inputLock.lock();
		if (mov != null)
			mov.jump(0);
		inputLock.unlock();
	}
	
	public void resetAllParameters(boolean movieToo) {
		inputLock.lock();
		if (movieToo)
			paraMovie = ImageProcessing.paraMovieBase.clone();
		paraCamera = imgProc.paraCameraBase.clone();
		buttonDetection.paraBoutons = imgProc.paraBoutonsBase.clone();
		if (takeMovie && movieToo)
			parametres = paraMovie;
		else if (!takeMovie)
			parametres = paraCamera;
		newInput = true;
		inputLock.unlock();
	}
	
	public void play(boolean play) {
		if (!app.hasImgAnalysis() || (!play && pausedCam && pausedMov))
			return;
		
		inputLock.lock();
		
		//video input initialisation (at first run)
		if (takeMovie) { 
			if (mov == null) {
				mov = new Movie(app, "testvideo.mp4");
				mov.loop();
				parametres = paraMovie;
			}
			if (pausedMov == play) {
				if (play) 	mov.play();
				else 		mov.pause();
				pausedMov = !play;
			}
		} else {
			if (cam == null) {
				takeCameraInput(idxCamera);
				parametres = paraCamera;
			}
			if (pausedCam == play) {
				if (play) 	cam.start();
				else		cam.stop();
				pausedCam = !play;
			}
		}
		
		if (play)
			newInput = true;
		resetOutput();
		inputLock.unlock();
	}
	
	private void resetOutput() {
		gameRotation = zero.copy();
		hasFoundQuad = false;
		quadDetection = null;
		buttonDetection.resetOutput();
	}

	public void playOrPause() {
		if (takeMovie)
			play(pausedMov);
		else
			play(pausedCam);
	}

	private void takeCameraInput(int idxCam) {
		inputLock.lock();
		if (takeMovie || idxCam != this.idxCamera || cam == null) {
			parametres = paraCamera;
			if (cam != null) {
				cam.dispose();
			}
			String[] cameras = Capture.list();
			System.out.println("----------------\navailable cam:");
			String toPrint;
			for (int i = 0; i < cameras.length; i++) {
				toPrint = cameras[i];
				if (i == idxCam) {
					toPrint = "** "+toPrint+ " **";
				}
				System.out.println(toPrint);
			}
			System.out.println("current: "+idxCam);
			cam = new Capture(app, cameras[PApplet.min(idxCam, cameras.length)]);
		}
		this.idxCamera = idxCam;
		takeMovie = false;
		inputLock.unlock();
	}

	public void changeInput() {
		inputLock.lock();
		
		takeMovie = !takeMovie;
		if (takeMovie) {
			parametres = paraMovie;
			if (!pausedCam) cam.stop();
			else 			cam.start();
		} else {
			parametres = paraCamera;
		}
		newInput = true;
		play(!paused());
		inputLock.unlock();
	}
	
	public void gui() {
		if (running()) {
			app.textFont( standardFont );
			app.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
			
			if (quadDetectionLock.isLocked() && newInput) {
				app.fill(0, 0, 0, 180);
				app.rect(20, 20, Brabra.width/6f, Brabra.height/6f);
				app.fill(200, 100, 0, 180);
				app.text("coming...", 20 + Brabra.width/6f, 20 + Brabra.height/6f);
			} else if (quadDetection != null) {
				quadDetectionLock.lock();
				app.fill(255);
				if (hasFoundRotation)
					app.tint(255, 255, 255, 150); 
				else if (hasFoundQuad)		//rotation is to big
					app.tint(255, 0, 0, 210);
				app.image(quadDetection, 20, 20, Brabra.width/6f, Brabra.height/6f);
				quadDetectionLock.unlock();
				app.tint(255, 255);
				if (paused()) {
					app.fill(200, 100, 0, 180);
					app.text("paused", 20 + Brabra.width/6f, 20 + Brabra.height/6f);
				}
			}
		}
	}
}


