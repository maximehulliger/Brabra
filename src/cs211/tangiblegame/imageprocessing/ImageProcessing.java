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

public class ImageProcessing {
	public static final float maxAcceptedAngle = 65f/360*PApplet.TWO_PI; //65°
	public static boolean displayQuadRejectionCause = false;
	private int idxCamera = 3;
	
	public final ReentrantLock imagesLock;
	public PImage inputImg;
	public PImage threshold2g;
	public PImage sobel;
	public HoughLine hough;
	
	public final ReentrantLock quadDetectionLock;
	public boolean hasFoundQuad = false;
	public PGraphics quadDetection;
	
	public final ReentrantLock buttonStateLock;
	public boolean leftButtonVisible = false;
	public boolean rightButtonVisible = false;
	private final ButtonDetection buttonDetection;
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
	public boolean pausedCam = false, pausedMov = false;
	
	public float[] parametres, paraMovie, paraCamera;
	public static final float[] paraMovieBase = { 
			0.37f, 0.5f, 	//hue
			0, 0.65f, 		//bright
			0.31f, 1,		//satur
			0, 0.4f, 		//r
			0.1f, 0.6f, 	//g
			0.2f, 1,		//b
			0.5f, 0.38f,	//min vote, neighbour
			0.6f, 0.2f };	//nb line kept, sobel threshold
	public static final float[] paraCameraBase = { 
			0.25f, 0.47f, 	//hue
			0.0f, 1f, 		//bright
			0.15f, 1f,		//satur
			0f, 1f, 		//r
			0f, 1f, 		//g
			0f, 0.88f,		//b
			0.5f, 0.38f,	//min vote, neighbour
			0.6f, 0.2f };	//nb line kept, sobel threshold
	
	//rotation
	private final ReentrantLock rotationLock;
	public boolean lostRotation = true;
	private PVector rotation = new PVector(0,0,0);
	private PVector lastRotation = new PVector(0,0,0);
	private PVector gameRotation = new PVector(0,0,0);
	
	/*pkg*/ final TangibleGame app;
	/*pkg*/ int inWidth = 0, inHeight = 0;
	/*pkg*/ final int color0, color255;
	/*pkg*/ static int colorQuad, colorLines;
	/*pkg*/ final int colorButtonOk, colorButtonRejected;
	private boolean mustDrawPauseFrame = false;
	private PFont fontPaused = null;
	private boolean readyForInput = false;
	private Movie mov = null;
	private Capture cam = null;
	private float strockWeight;
	
	public ImageProcessing(TangibleGame app) {
		this.app = app;
		color0 = app.color(0);
		color255 = app.color(255);
		colorButtonOk = app.color(0, 255, 0, 150);
		colorButtonRejected = app.color(255, 0, 0, 150);
		colorQuad = app.color(200, 100, 0, 120);
		
		buttonDetection = new ButtonDetection(this);
		imagesLock = new ReentrantLock();
		quadDetectionLock = new ReentrantLock();
		rotationLock = new ReentrantLock();
		inputLock = new ReentrantLock();
		buttonStateLock = new ReentrantLock();
		
		paraMovie = ProMaster.copy(paraMovieBase);
		paraCamera = ProMaster.copy(paraCameraBase);
		if (takeMovie)
			takeVideoInput();
		else
			takeCameraInput(idxCamera);
		play(false); //pause
	}
	
	public void updateForInput() {
		if (!readyForInput && (inWidth != inputImg.width || inHeight != inputImg.height)) {
			inWidth = inputImg.width;
			inHeight = inputImg.height;
			quadDetection = app.createGraphics(inWidth, inHeight);
			fontPaused = app.createFont("Arial", inHeight/16);
			strockWeight = PApplet.max(inWidth * 3f / app.width, 1f);
			quadDetection.strokeWeight(strockWeight);
			readyForInput = true;
		}
	}
	
	public void restartMovie() {
		inputLock.lock();
		if (mov != null)
			mov.jump(0);
		inputLock.unlock();
	}
	
	public void resetParametres() {
		inputLock.lock();
		if (takeMovie) {
			paraMovie = ProMaster.copy(paraMovieBase);
			parametres = paraMovie;
		} else {
			paraCamera = ProMaster.copy(paraCameraBase);
			parametres = paraCamera;
		}
		inputLock.unlock();
	}
	
	public void play(boolean play) {
		inputLock.lock();
		if (takeMovie) {
			if (pausedMov == play) {
				pausedMov = !play;
				if (play) {
					readyForInput = false;
					mov.play();
				} else {
					mov.pause();
					mustDrawPauseFrame = true;
				}
			}
		} else {
			if (pausedCam == play) {
				pausedCam = !play;
				if (play) {
					readyForInput = false;
					cam.start();
				} else {
					cam.stop();
					mustDrawPauseFrame = true;
				}
			}
		}
		inputLock.unlock();
	}

	public void playOrPause() {
		if (takeMovie)
			play(pausedMov);
		else
			play(pausedCam);
	}

	public void takeVideoInput() {
		inputLock.lock();
		if (!takeMovie || mov == null) {
			parametres = paraMovie;
			if (mov == null) {
				mov = new Movie(app, "testvideo.mp4");
				mov.loop();
			}
		}
		this.takeMovie = true;
		inputLock.unlock();
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
			cam.start();
		}
		this.idxCamera = idxCam;
		this.takeMovie = false;
		inputLock.unlock();
	}

	public void changeInput() {
		inputLock.lock();
		boolean tm = takeMovie;
		if (!tm)
			takeVideoInput();
		else
			takeCameraInput(idxCamera);
		play(true);
		readyForInput = false;
		inputLock.unlock();
	}

	public void run() {
		while (!app.over) {
			try {
				boolean newImage = false;
				inputLock.lock();
				imagesLock.lock();
				if (takeMovie && !pausedMov && mov.available()) {
					newImage = true;
					mov.read();
					inputImg = mov.get();

				} else if (!takeMovie && !pausedCam && cam.available()) {
					newImage = true;
					cam.read();
					inputImg = cam.get();
				}

				if (inputImg != null && (newImage || forced || mustDrawPauseFrame)) {
					mustDrawPauseFrame = false;
					updateForInput();
					
					// analyse input to find a green quad
					PImage threshold1g = colorThreshold(inputImg, parametres[0], parametres[1], parametres[2], parametres[3], parametres[4], parametres[5]);
					PImage bluredg = blur(threshold1g);
					threshold2g = intensityThreshold(bluredg, parametres[6], parametres[7], parametres[8], parametres[9], parametres[10], parametres[11]);
					sobel = sobel(threshold2g, parametres[15]);
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
						butDetJob = new ButtonDetectionJob(inputImg, hough.quad);
						detectedQuad = hough.quad();
						imagesLock.unlock();
						butDetJob.start();

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
								gameRotation = new PVector(-rotMoyenne.x * ratioXZ, rotMoyenne.z, -rotMoyenne.y* ratioXZ);
								float r = 360/PApplet.TWO_PI;
								System.out.printf("rot: x: %.1f y: %.1f z: %.1f (°)\n", rotation.x*r, rotation.y*r, rotation.z*r);
							}
							rotationLock.unlock();
						} else {
							if (ImageProcessing.displayQuadRejectionCause)
								System.out.println("angle pas accepté !");
						}
						
						//-- get & set button state
						butDetJob.join();
						butDetJob = null;
						buttonStateLock.lock();
						leftButtonVisible = buttonDetection.leftVisible;
						rightButtonVisible = buttonDetection.rightVisible;
						buttonStateLock.unlock();
						imagesLock.lock();
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
					// paused
					inputLock.lock();
					if ((pausedMov && takeMovie) || (pausedCam && !takeMovie)) {
						quadDetection.textFont( fontPaused );
						inputLock.unlock();
						quadDetection.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
						quadDetection.fill(200, 100, 0, 255);
						quadDetection.text("paused", quadDetection.width, quadDetection.height);
					} else {
						inputLock.unlock();
					}
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
	
	static public int rgbColor(int r, int g, int b, int a) {
		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;
		return /*(0xFFFF_FFFF << 32) +*/ (a << 24) + (r << 16) + (g << 8) + b;
	}
	
	static public int rgbColor(int rgb) {
		return rgbColor(rgb, rgb, rgb, 255);
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
		quadDetectionLock.lock();
		if (quadDetection != null) {
			app.fill(255);
			app.image(quadDetection, 20, 20, app.width/6, app.height/6);
		}
		quadDetectionLock.unlock();
	}

	private PImage intensityThreshold(PImage img, 
			float minR, float maxR,
			float minG, float maxG, 
			float minB, float maxB) {
		if (minR>maxR || minG>maxG || minB>maxB)
			return img;

		PImage ret = app.createImage(img.width, img.height, PApplet.RGB);
		for(int i = 0; i < img.width * img.height; i++) {
			int p = img.pixels[i];
			if (isIn(app.red(p), minR*255, maxR*255) &&
					isIn(app.green(p), minG*255, maxG*255) &&
					isIn(app.blue(p), minB*255, maxB*255))
				ret.pixels[i] = p;
			else
				ret.pixels[i] = color0;
		}
		return ret;
	}

	private PImage colorThreshold(PImage img, 
			float minHue, float maxHue,			//0.4f, 0.55f
			float minBright, float maxBright,	//0.25f, 0.55f
			float minSatur, float maxSatur ) { 	//0.5f, 1
		if (minHue>maxHue || minBright>maxBright || minSatur>maxSatur)
			return img;

		PImage ret = app.createImage(img.width, img.height, PApplet.RGB);
		for(int i = 0; i < img.width * img.height; i++) {
			int p = img.pixels[i];
			if (isIn(app.hue(p), minHue*255, maxHue*255) &&
					isIn(app.brightness(p), minBright*255, maxBright*255) &&
					isIn(app.saturation(p), minSatur*255, maxSatur*255))
				ret.pixels[i] = p;
			else
				ret.pixels[i] = color0;
		}
		return ret;
	}

	public static int pixel(PImage img, int x, int y) {
		if (isIn(x, 0, img.width-1) && isIn(y, 0, img.height-1))
			return img.pixels[x + img.width*y];
		else
			return 0;
	}

	public static boolean isIn(int x, int min, int max) { //x E [min;max]
		return x>=min && x<=max;
	}

	public static boolean isIn(float x, float min, float max) { //x E [min;max]
		return x>=min && x<=max;
	}

	public final static int kernelSize = 3;

	public static float[][] gaussianKernel  = { //w:99
		{ 9,  12, 9  },
		{ 12, 15, 12 },
		{ 9,  12, 9  } };

	private PImage sobel(PImage img, float threshold) {
		float[][] hKernel = { 
				{ 0, 1, 0 },
				{ 0, 0, 0 },
				{ 0, -1, 0 } };
		float[][] vKernel = { 
				{ 0, 0, 0 },
				{ 1, 0, -1 },
				{ 0, 0, 0 } };

		PImage result = app.createImage(img.width, img.height, PApplet.RGB);
		float[] buffer = new float[img.width * img.height];
		float max = 0;

		for(int x = 0; x < img.width; x++) {
			for(int y = 0; y < img.height; y++) {
				float accH = 0;
				float accV = 0;
				for (int dx = 0; dx < kernelSize; dx++){
					for (int dy = 0; dy < kernelSize; dy++) {
						float kValueH = hKernel[dx][dy];
						float kValueV = vKernel[dx][dy];
						float intensity = app.brightness(pixel(img, x+dx-kernelSize/2, y+dy-kernelSize/2));
						accH += intensity*kValueH;
						accV += intensity*kValueV;
					}
				}
				float sum= PApplet.sqrt(PApplet.pow(accH, 2) + PApplet.pow(accV, 2));
				buffer[y * img.width + x] = sum;
				if (sum > max) max = sum;
			}
		}

		for (int y = 0; y < img.height; y++) { 
			for (int x = 0; x < img.width; x++) { 
				if (buffer[y * img.width + x] > (int)(max * threshold)) {
					result.pixels[y * img.width + x] = color255;
				} else {
					result.pixels[y * img.width + x] = color0;
				}
			}
		}
		return result;
	}

	private PImage blur(PImage img) {
		return convolute(img, gaussianKernel, 99);
	}

	private PImage convolute(PImage img, float[][] kernel, float weight) {
		// create a greyscale image (type: ALPHA) for output
		PImage result = app.createImage(img.width, img.height, PApplet.RGB);

		for(int x = 0; x < img.width; x++) {
			for(int y = 0; y < img.height; y++) {
				float[] acc = {0,0,0};
				for(int dx = 0; dx < kernelSize; dx++){
					for(int dy = 0; dy < kernelSize; dy++) {
						float kValue = kernel[dx][dy];
						int p = pixel(img, x+dx-kernelSize/2, y+dy-kernelSize/2);
						acc[0] += app.red(p)*kValue;
						acc[1] += app.green(p)*kValue;
						acc[2] += app.blue(p)*kValue;
					}
				}
				for (int i=0; i<3; i++)
					acc[i] /= weight;
				int p = rgbColor((int)acc[0], (int)acc[1], (int)acc[2], 255);
				result.pixels[x+result.width*y] = p;
			}
		}
		return result;
	}
}


