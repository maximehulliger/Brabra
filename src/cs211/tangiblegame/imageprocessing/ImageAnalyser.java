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
	public final boolean detectButtons = true;
	private int idxCamera = 3;
	
	//--- images & quad detection (control img)
	public final ReentrantLock imagesLock;
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
	public final ButtonDetection buttonDetection;
	private class ButtonDetectionJob extends Thread {
		public ButtonDetectionJob(PImage input, PVector[] corners) {
			buttonDetection.setInput(input, corners);
		}
		public void run() {
			buttonDetection.detect();
		}
	}
	
	//--- input
	public final ReentrantLock inputLock;
	public boolean forced = false;	//force l'analyse de l'image même si l'input n'a pas changé.
	public boolean takeMovie = false; //prend la camera si false.
	public boolean pausedCam = true, pausedMov = true;
	public float[] parametres, paraMovie, paraCamera;
	
	//rotation
	private final ReentrantLock rotationLock;
	public boolean hasFoundRotation = true;
	private int rotationAge = 0;
	private PVector rotation = new PVector(0,0,0);
	private PVector lastRotation = new PVector(0,0,0);
	private PVector gameRotation = new PVector(0,0,0);
	
	public PFont standardFont;
	public float strockWeight;
	public boolean ready = true;
	/*pkg*/ final TangibleGame app;
	/*pkg*/ int inWidth = 0, inHeight = 0;
	private boolean newInput = true;
	private Movie mov = null;
	private Capture cam = null;
	
	public ImageAnalyser(TangibleGame app) {
		this.app = app;
		
		buttonDetection = new ButtonDetection(this);
		imagesLock = new ReentrantLock();
		quadDetectionLock = new ReentrantLock();
		rotationLock = new ReentrantLock();
		inputLock = new ReentrantLock();
		buttonStateLock = new ReentrantLock();
		standardFont = app.createFont("Arial", app.height/40f);
		
		paraMovie = ProMaster.copy(ImageProcessing.paraMovieBase);
		paraCamera = ProMaster.copy(ImageProcessing.paraCameraBase);
	}
	
	public void run() {
		while (!app.over) {
			try {
				boolean newImage = false;
				boolean once = false;
				inputLock.lock();
				
				//video input initialisation (at first run)
				if (newInput) {
					if (takeMovie && mov == null) {
						mov = new Movie(app, "testvideo.mp4");
						mov.loop();
						parametres = paraMovie;
					} else if (!takeMovie && cam == null) {
						takeCameraInput(idxCamera);
						parametres = paraCamera;
					}
				}
				
				imagesLock.lock();
				if (takeMovie && (!pausedMov || newInput) && mov.available()) {
					newImage = true;
					mov.read();
					inputImg = mov.get();

				} else if (!takeMovie && (!pausedCam || newInput) && cam.available()) {
					newImage = true;
					cam.read();
					inputImg = cam.get();
				}
				
				if ( newImage && newInput ) {
					updateForInput();
					once = true;
				}

				if ( newImage || (forced && inputImg != null) || once ) {
					
					// analyse input to find a green quad
					PImage threshold1g = ImageProcessing.colorThreshold(inputImg, parametres[0], parametres[1], parametres[2], parametres[3], parametres[4], parametres[5]);
					PImage bluredg = ImageProcessing.blur(threshold1g);
					threshold2g = ImageProcessing.intensityThreshold(bluredg, parametres[6], parametres[7], parametres[8], parametres[9], parametres[10], parametres[11]);
					sobel = ImageProcessing.sobel(threshold2g, parametres[15]);
					HoughLine.minVotes = PApplet.round(parametres[12] * 100);
					HoughLine.neighbourhood = PApplet.round(parametres[13] * 100);
					HoughLine.maxKeptLines = PApplet.round(parametres[14] * 10);
					inputLock.unlock();
					hough = new HoughLine(sobel, app);
					
					hasFoundQuad = hough.quad != null;
					
					// if quad is found, analyse buttons & rotation (with image lock release)
					ButtonDetectionJob butDetJob = null;
					ArrayList<PVector> detectedQuad = null;
					if (hasFoundQuad) {
						//-- finish with images & start button det.
						detectedQuad = hough.quad();
						if (detectButtons) {
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
								PVector rotMoyenne = PVector.div( PVector.add(lastRotation, rotation), 2); //moyenne des 2 dernières entrées
								float ratioXZ = TangibleGame.inclinaisonMax / maxAcceptedAngle;
								gameRotation = new PVector(-rotMoyenne.x * ratioXZ, rotMoyenne.z, rotMoyenne.y* ratioXZ);
								//on adoucis les angles d'entrée (pour + de contrôle proche de 0)
								float r = 360/PApplet.TWO_PI;
								System.out.printf("rot: x: %.1f y: %.1f z: %.1f (°)\n", rotation.x*r, rotation.y*r, rotation.z*r);	
							}
							rotationAge = 0;
							hasFoundRotation = true;
							rotationLock.unlock();
						} else {
							if (displayQuadRejectionCause)
								System.out.println("angle trop grand !");
						}
						
						//-- get & set button state
						if (detectButtons) {
							butDetJob.join();
							butDetJob = null;
							buttonStateLock.lock();
							leftButtonVisible = buttonDetection.leftVisible;
							rightButtonVisible = buttonDetection.rightVisible;
							buttonStateLock.unlock();
						}
						imagesLock.lock();
					} else {
						rotationLock.lock();
						hasFoundRotation = false;
						if (rotationAge++ == 6) {
							gameRotation = ProMaster.zero.get();
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
					app.delay(50); //dors 50 ms
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// ----- interaction response

	public void updateForInput() {
		if (newInput) {
			//image size dependant parameters update
			if (inWidth != inputImg.width || inHeight != inputImg.height) {
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
	
	public void resetParameters() {
		inputLock.lock();
		if (takeMovie) {
			paraMovie = ProMaster.copy(ImageProcessing.paraMovieBase);
			parametres = paraMovie;
		} else {
			paraCamera = ProMaster.copy(ImageProcessing.paraCameraBase);
			parametres = paraCamera;
		}
		inputLock.unlock();
	}
	
	public void play(boolean play) {
		inputLock.lock();
		if (takeMovie) {
			if (pausedMov == play && mov != null) {
				if (play) 	mov.play();
				else 		mov.pause();
			}
			pausedMov = !play;
		} else {
			if (pausedCam == play && cam != null) {
				if (play) 	cam.start();
				else		cam.stop();
			}
			pausedCam = !play;
		}
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
		
		if (!pausedCam) {
			if (takeMovie)
				cam.stop();
			else
				cam.start();
		}
		
		newInput = true;
		
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
			if ((pausedMov && takeMovie) || (pausedCam && !takeMovie)) {
				app.fill(200, 100, 0, 180);
				app.text("paused", 20 + app.width/6f, 20 + app.height/6f);
			}
		}
	}
}


