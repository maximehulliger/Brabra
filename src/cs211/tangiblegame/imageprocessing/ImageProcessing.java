package cs211.tangiblegame.imageprocessing;

import cs211.tangiblegame.imageprocessing.HoughLine;
import cs211.tangiblegame.imageprocessing.TwoDThreeD;
import cs211.tangiblegame.ProMaster;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PApplet;
import processing.core.PVector;
import processing.video.*;

public class ImageProcessing extends ProMaster {
	public Movie mov;
	public Capture cam;

	public PImage inputImg;
	public PImage threshold2g;
	public PImage sobel;
	public HoughLine hough;
	public PGraphics quadDetection;

	public PVector rotation = zero.get();
	public float[] paraMovie = { 
			0.37f, 0.5f, 	//hue
			0, 0.65f, 		//bright
			0.31f, 1,		//satur
			0, 0.4f, 		//r
			0.1f, 0.6f, 	//g
			0.2f, 1,		//b
			0.5f, 0.38f,	//min vote, neighbour
			0.6f, 0.2f };	//nb line kept, sobel threshold
	public float[] paraCamera = { 
			0.16f, 0.4f, 	//hue
			0.0f, 1f, 		//bright
			0.15f, 1f,		//satur
			0f, 1f, 		//r
			0f, 1f, 		//g
			0f, 0.88f,		//b
			0.5f, 0.38f,	//min vote, neighbour
			0.6f, 0.2f };	//nb line kept, sobel threshold

	public boolean takeMovie = true; //prend la camera si false.
	public float[] parametres = paraMovie;
	
	public ImageProcessing() {
		setTakeMovie(takeMovie);
	}
	
	private void setTakeMovie(boolean takeMovie) {
		if (takeMovie) {
			parametres = paraMovie;
			mov = new Movie(app, "testvideo.mp4");
			mov.loop();
		} else {
			parametres = paraCamera;
			String[] cameras = Capture.list();
			/*for (int i = 0; i < cameras.length; i++)
				println(cameras[i]);*/
			cam = new Capture(app, cameras[3]);
			cam.start();
		}
		this.takeMovie = takeMovie;
	}

	public void changeInput() {
		setTakeMovie(!takeMovie);
	}

	public void update() {
		if (takeMovie && mov.available()) {
			mov.read();
			inputImg = mov.get();
			
		} else if (!takeMovie && cam.available()) {
			cam.read();
			inputImg = cam.get();
		}
		
		if (inputImg != null) {
			app.background(0);
			this.quadDetection = app.createGraphics(inputImg.width, inputImg.height);

			PImage threshold1g = colorThreshold(inputImg, parametres[0], parametres[1], parametres[2], parametres[3], parametres[4], parametres[5]);
			PImage bluredg = blur(threshold1g);
			threshold2g = intensityThreshold(bluredg, parametres[6], parametres[7], parametres[8], parametres[9], parametres[10], parametres[11]);
			sobel = sobel(threshold2g, parametres[15]);
			HoughLine.minVotes = PApplet.round(parametres[12] * 100);
			HoughLine.neighbourhood = PApplet.round(parametres[13] * 100);
			HoughLine.maxKeptLines = PApplet.round(parametres[14] * 10);
			hough = new HoughLine(sobel, app);
			
			//-- print control img
			quadDetection.beginDraw();
			quadDetection.image(sobel, 0, 0);
			hough.drawLines(quadDetection);
			hough.drawQuad(quadDetection);
			
			//-- if valid quad, update rotation & control img (rendu)
			if (hough.quad != null) {
				//ButtonDetection blobDet = new ButtonDetection(this, hough.quad, threshold2);

				//get rotation from quad
				if (hough.quad != null) {
					TwoDThreeD deathMasterLongSword = new TwoDThreeD(inputImg.width, inputImg.height);
					rotation = deathMasterLongSword.get3DRotations(hough.quad());
					float r = 360/PApplet.TWO_PI;
					System.out.printf("rot: x: %.1f y: %.1f z: %.1f (°)\n", rotation.x*r, rotation.y*r, rotation.z*r);
				}
			}
			quadDetection.endDraw();
		}
	}

	public void displayCtrImg() {
		//-- display
		//app.image(sobel, 0, 0, dWidth, dHeight);
		//image(blobDet.coco, dWidth, 0, dWidth, dHeight);
		if (inputImg != null) {
			app.fill(255, 255, 255);
			app.image(quadDetection, 20, 20, app.width/6, app.height/6);
		}
	}

	public PImage intensityThreshold(PImage img, 
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
				ret.pixels[i] = app.color(0);
		}
		return ret;
	}

	public PImage colorThreshold(PImage img, 
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
				ret.pixels[i] = app.color(0);
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

	public PImage sobel(PImage img, float threshold) {
		float[][] hKernel = { 
				{ 0, 1, 0 },
				{ 0, 0, 0 },
				{ 0, -1, 0 } };
		float[][] vKernel = { 
				{ 0, 0, 0 },
				{ 1, 0, -1 },
				{ 0, 0, 0 } };

		PImage result = app.createImage(img.width, img.height, PApplet.HSB);
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
					result.pixels[y * img.width + x] = app.color(255);
				} else {
					result.pixels[y * img.width + x] = app.color(0);
				}
			}
		}
		return result;
	}

	public PImage blur(PImage img) {
		return convolute(img, gaussianKernel, 99);
	}

	public PImage convolute(PImage img, float[][] kernel, float weight) {
		// create a greyscale image (type: ALPHA) for output
		PImage result = app.createImage(img.width, img.height, PApplet.ALPHA);

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
				int p = app.color(acc[0], acc[1], acc[2]);
				result.pixels[x+result.width*y] = p;
			}
		}
		return result;
	}
}


