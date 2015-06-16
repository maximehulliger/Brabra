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

public class ImageProcessing extends Thread {
	private Movie mov = null;
	private Capture cam = null;
	private int idxCamera = 3;
	
	public final ReentrantLock imagesLock;
	public PImage inputImg;
	public PImage threshold2g;
	public PImage sobel;
	public HoughLine hough;
	
	public final ReentrantLock quadDetectionLock;
	public PGraphics quadDetection;
	
	//--- input
	public final ReentrantLock inputLock;
	public boolean forced = false;	//force l'analyse de l'image même si l'input n'a pas changé.
	public boolean takeMovie = true; //prend la camera si false.
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
	
	private final int color0, color255;
	
	private final TangibleGame app;
	private final ReentrantLock rotationLock;
	public boolean pausedCam = false, pausedMov = false;
	private PVector rotation = new PVector(0,0,0);
	public boolean continueThread = true;
	private PFont fontPaused = null;
	
	public ImageProcessing(TangibleGame app) {
		this.app = app;
		color0 = app.color(0);
		color255 = app.color(255);
		
		imagesLock = new ReentrantLock();
		quadDetectionLock = new ReentrantLock();
		rotationLock = new ReentrantLock();
		inputLock = new ReentrantLock();
		
		paraMovie = ProMaster.copy(paraMovieBase);
		paraCamera = ProMaster.copy(paraCameraBase);
		if (takeMovie)
			takeVideoInput();
		else
			takeCameraInput(idxCamera);
		play(false); //pause
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
				if (play) {
					fontPaused = null;
					mov.play();
				} else {
					mov.pause();
				}
			}
			pausedMov = !play;
		} else {
			if (pausedCam == play) {
				if (play) {
					fontPaused = null;
					cam.start();
				} else {
					cam.stop();
				}
			}
			pausedCam = !play;
		}
		
		if (((pausedMov && takeMovie) || (pausedCam && !takeMovie)) && quadDetection != null) {
			inputLock.unlock();
			/*quadDetection.textFont( fontPaused );
			quadDetection.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
			quadDetection.fill(200, 100, 0, 255);
			quadDetection.text("paused", quadDetection.width, quadDetection.height);*/
		} else
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
		fontPaused = null;
		inputLock.unlock();
	}
	
	public void run() {
		while (continueThread) {
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
			
			if (inputImg != null && (newImage || forced)) {
				PImage threshold1g = colorThreshold(inputImg, parametres[0], parametres[1], parametres[2], parametres[3], parametres[4], parametres[5]);
				PImage bluredg = blur(threshold1g);
				threshold2g = intensityThreshold(bluredg, parametres[6], parametres[7], parametres[8], parametres[9], parametres[10], parametres[11]);
				sobel = sobel(threshold2g, parametres[15]);
				HoughLine.minVotes = PApplet.round(parametres[12] * 100);
				HoughLine.neighbourhood = PApplet.round(parametres[13] * 100);
				HoughLine.maxKeptLines = PApplet.round(parametres[14] * 10);
				hough = new HoughLine(sobel, app);
				
				//-- print control img (with image lock release)
				quadDetectionLock.lock();
				quadDetection = app.createGraphics(inputImg.width, inputImg.height);
				quadDetection.beginDraw();
				//app.applock.lock();
				quadDetection.fill(255, 255);
				quadDetection.image(sobel, 0, 0);
				hough.drawLines(quadDetection);
				hough.drawQuad(quadDetection);
				//app.applock.unlock();
				int w = inputImg.width, h = inputImg.height;
				imagesLock.unlock();
				if ((pausedMov && takeMovie) || (pausedCam && !takeMovie)) {
					inputLock.unlock();
					if (fontPaused == null)
						fontPaused = app.createFont("Arial", h/16);
					quadDetection.textFont( fontPaused );
					quadDetection.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
					quadDetection.fill(200, 100, 0, 255);
					quadDetection.text("paused", quadDetection.width, quadDetection.height);
				} else {
					inputLock.unlock();
				}
				quadDetection.endDraw();
				quadDetectionLock.unlock();
				imagesLock.lock();
				//-- if valid quad, update rotation & control img (rendu)
				if (hough.quad != null) {
					ArrayList<PVector> detectedQuad = hough.quad();
					imagesLock.unlock();
					//ButtonDetection blobDet = new ButtonDetection(this, hough.quad, threshold2);
	
					//get rotation from quad
					TwoDThreeD deathMasterLongSword = new TwoDThreeD(w, h);
					rotationLock.lock();
					rotation = deathMasterLongSword.get3DRotations(detectedQuad);
					float r = 360/PApplet.TWO_PI;
					System.out.printf("rot: x: %.1f y: %.1f z: %.1f (°)\n", rotation.x*r, rotation.y*r, rotation.z*r);
					rotationLock.unlock();
				} else {
					imagesLock.unlock();
				}
			} else {
				inputLock.unlock();
				imagesLock.unlock();
				
				try {
					sleep(50); //dors 50 ms
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private int rgbColor(int r, int g, int b) {
		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;
		return (0xFFFF_FFFF << 24) + (r << 16) + (g << 8) + b;
	}
	
	public PVector rotation() {
		try {
			rotationLock.lock();
			return rotation.get();
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
				int p = rgbColor((int)acc[0], (int)acc[1], (int)acc[2]);
				result.pixels[x+result.width*y] = p;
			}
		}
		return result;
	}
}


