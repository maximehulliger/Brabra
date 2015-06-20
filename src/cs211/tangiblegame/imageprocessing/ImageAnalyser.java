package cs211.tangiblegame.imageprocessing;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import cs211.tangiblegame.imageprocessing.HoughLine;
import cs211.tangiblegame.imageprocessing.TwoDThreeD;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.TangibleGame;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PApplet;
import processing.core.PVector;
import processing.video.*;

public class ImageAnalyser {
	public static final float maxAcceptedAngle = 65f/360*PApplet.TWO_PI; //65°
	public static boolean displayQuadRejectionCause = false;
	private int idxCamera = 3;

	//--- input
	public final ReentrantLock inputLock;
	public boolean detectButtons = false;
	public boolean forced = false;	//force l'analyse de l'image même si l'input n'a pas changé.
	public boolean takeMovie = false; //prend la camera si false.
	private boolean pausedCam = true, pausedMov = true;
	public float[] parametres, paraMovie, paraCamera;
	
	//--- images & quad detection (control img)
	public final ReentrantLock imagesLock;
	public final ImageProcessing imgProc = new ImageProcessing();
	public PImage inputImg;
	public PImage threshold2g;
	public PImage sobel;
	public HoughLine hough;
	public PImage threshold2Button;
	public final ReentrantLock quadDetectionLock;
	public boolean hasFoundQuad = false;
	public PGraphics quadDetection;
	
	//--- button detection
	public final ReentrantLock buttonStateLock;
	public boolean displayButtonsState = true;
	public boolean leftButtonVisible = false;
	public boolean rightButtonVisible = false;
	public float leftButtonScore = 0, rightButtonScore = 0;
	public final ButtonDetection buttonDetection;
	private class ButtonDetectionJob extends Thread {
		public ButtonDetectionJob(PImage input, PVector[] corners) {
			buttonDetection.setInput(input, corners);
		}
		public void run() {
			buttonDetection.detect();
		}
	}
	
	//rotation
	private final ReentrantLock rotationLock;
	public boolean hasFoundRotation = false;
	private int rotationAge = 0;
	private PVector rotation = new PVector(0,0,0);
	private PVector lastRotation = new PVector(0,0,0);
	private PVector gameRotation = new PVector(0,0,0);
	
	//"interne" (lulz)
	public PFont standardFont;
	public float strockWeight;
	/*pkg*/ final TangibleGame app;
	/*pkg*/ int inWidth = 0, inHeight = 0;
	private boolean newInput = false;
	private Movie mov = null;
	private Capture cam = null;
	
	public ImageAnalyser(TangibleGame app) {
		this.app = app;
		
		//safeStartLock = new ReentrantLock();
		//safeStartLock.lock();
		buttonDetection = new ButtonDetection(this);
		imagesLock = new ReentrantLock();
		quadDetectionLock = new ReentrantLock();
		rotationLock = new ReentrantLock();
		inputLock = new ReentrantLock();
		buttonStateLock = new ReentrantLock();
		standardFont = app.createFont("Arial", app.height/40f);
		quadDetection = app.createGraphics(1920, 1080);
		
		paraMovie = ProMaster.copy(ImageProcessing.paraMovieBase);
		paraCamera = ProMaster.copy(imgProc.paraCameraBase);
		if (takeMovie)
			parametres = paraMovie;
		else
			parametres = paraCamera;
	}
	
	public void run() {
		while (!app.over) {
			try {
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
					ButtonDetectionJob butDetJob = null;
					ArrayList<PVector> detectedQuad = null;
					if (hasFoundQuad) {
						//-- finish with images & start button det.
						detectedQuad = hough.quad();
						if (detectButtonsInt) {
							butDetJob = new ButtonDetectionJob(inputImg, hough.quad);
							butDetJob.start();
						}
						imagesLock.unlock();

						//-- compute & set rotation
						TwoDThreeD deathMasterLongSword = new TwoDThreeD(inWidth, inHeight);
						PVector newRot = deathMasterLongSword.get3DRotations(detectedQuad);
						if (ProMaster.isConstrained(newRot.x, -maxAcceptedAngle, maxAcceptedAngle) &&
								ProMaster.isConstrained(newRot.x, -maxAcceptedAngle, maxAcceptedAngle) &&
								ProMaster.isConstrained(newRot.x, -maxAcceptedAngle, maxAcceptedAngle)) {
							rotationLock.lock();
							if (!lastRotation.equals(newRot) ) {
								lastRotation = rotation;
								rotation = newRot;
								gameRotation = PVector.div( PVector.add(lastRotation, rotation), 2); //moyenne des 2 dernières entrées
								gameRotation.mult(-TangibleGame.inclinaisonMax / maxAcceptedAngle);
								//on adoucis les angles d'entrée (pour + de contrôle proche de 0) TODO
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
							butDetJob.join();
							butDetJob = null;
							if (hasFoundRotation) {
								buttonStateLock.lock();
								leftButtonVisible = buttonDetection.leftVisible;
								rightButtonVisible = buttonDetection.rightVisible;
								leftButtonScore = buttonDetection.leftScore;
								rightButtonScore = buttonDetection.rightScore;
								buttonStateLock.unlock();
							}
						}
						imagesLock.lock();
					} 
					if (!hasFoundQuad || !hasFoundRotation) { // no good input -> old the whole !
						rotationLock.lock();
						if (rotationAge++ == 6) {
							gameRotation = ProMaster.zero.get();
						}
						rotationLock.unlock();
						buttonStateLock.lock();
						leftButtonVisible = false;
						rightButtonVisible = false;
						leftButtonScore = 0;
						rightButtonScore = 0;
						buttonStateLock.unlock();
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
					app.delay(50); //dors 50 ms
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// ----- interaction response

	private void updateForInput() {
		if (newInput) {
			//image size dependant parameters update
			if (quadDetection.width != inputImg.width || quadDetection.height != inputImg.height) {
				inWidth = inputImg.width;
				inHeight = inputImg.height;
				quadDetection = app.createGraphics(inWidth, inHeight);
				strockWeight = PApplet.max(inWidth * 3f / app.width, 1f);
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
			paraMovie = ProMaster.copy(ImageProcessing.paraMovieBase);
		paraCamera = ProMaster.copy(imgProc.paraCameraBase);
		buttonDetection.paraBoutons = ProMaster.copy( imgProc.paraBoutonsBase );
		if (takeMovie && movieToo)
			parametres = paraMovie;
		else if (!takeMovie)
			parametres = paraCamera;
		newInput = true;
		inputLock.unlock();
	}
	
	public boolean paused() {
		return (pausedMov && takeMovie) || (pausedCam && !takeMovie);
	}
	
	public void play(boolean play) {
		inputLock.lock();
		
		//video input initialisation (at first run)
		if (takeMovie && mov == null) {
			mov = new Movie(app, "testvideo.mp4");
			mov.loop();
			parametres = paraMovie;
		} else if (!takeMovie && cam == null) {
			takeCameraInput(idxCamera);
			parametres = paraCamera;
		}
		
		if (takeMovie) {
			if (pausedMov == play && mov != null) {
				if (play) 	mov.play();
				else 		mov.pause();
				pausedMov = !play;
			}
		} else {
			if (pausedCam == play && cam != null) {
				if (play) 	cam.start();
				else		cam.stop();
				pausedCam = !play;
			}
		}
		if (play)
			newInput = true;
		
		inputLock.unlock();
	}

	public void playOrPause() {
		if (takeMovie)
			play(pausedMov);
		else
			play(pausedCam);
	}

	public void takeCameraInput(int idxCam) {
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
		} else {
			parametres = paraCamera;
		}
		
		if (takeMovie) {
			if (!pausedCam) cam.stop();
		} else {
			if (!pausedCam) cam.start();
		}
		newInput = true;
		play(!paused());
		inputLock.unlock();
	}

	public PVector rotation() {
		try {
			rotationLock.lock();
			return gameRotation.get();
		} finally {
			rotationLock.unlock();
		}
	}

	public void displayCtrImg() {
		app.textFont( standardFont );
		app.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
		
		if (quadDetectionLock.isLocked() && newInput) {
			app.fill(0, 0, 0, 180);
			app.rect(20, 20, app.width/6f, app.height/6f);
			app.fill(200, 100, 0, 180);
			app.text("coming...", 20 + app.width/6f, 20 + app.height/6f);
		} else if (quadDetection != null) {
			quadDetectionLock.lock();
			app.fill(255);
			if (hasFoundRotation)
				app.tint(255, 255, 255, 150); 
			else if (hasFoundQuad)		//rotation is to big
				app.tint(255, 0, 0, 210);
			app.image(quadDetection, 20, 20, app.width/6f, app.height/6f);
			quadDetectionLock.unlock();
			app.tint(255, 255);
			if (paused()) {
				app.fill(200, 100, 0, 180);
				app.text("paused", 20 + app.width/6f, 20 + app.height/6f);
			}
		}
	}
}


