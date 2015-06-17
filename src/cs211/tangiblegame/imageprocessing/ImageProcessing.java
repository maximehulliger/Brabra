package cs211.tangiblegame.imageprocessing;

import java.awt.Polygon;

import cs211.tangiblegame.ProMaster;
import processing.core.PApplet;
import processing.core.PImage;

public final class ImageProcessing extends ProMaster {

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
		0.6f, 0.13f };	//nb line kept, sobel threshold
	public static final float[] paraBoutonsBase = { 
		0.25f, 0.53f, 	//hue
		0.0f, 1f, 		//bright
		0.15f, 1f,		//satur
		0.0f, 1f, 		//r (3)
		0f, 1f, 		//g
		0f, 0.88f};		//b

	static public int rgbColor(int r, int g, int b, int a) {
		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;
		return /*(0xFFFF_FFFF << 32) +*/ (a << 24) + (r << 16) + (g << 8) + b;
	}

	static public int rgbColor(int rgb) {
		return rgbColor(rgb, rgb, rgb, 255);
	}
	
	public static PImage quadFilter(PImage input, Polygon quad) {
		PImage ret = app.createImage(input.width, input.height, PApplet.RGB);
		for(int x = 0; x < input.width; x++) {
			for(int y = 0; y < input.height; y++) {
				int i = y * input.width + x;
				if (quad.contains(x, y))
						ret.pixels[i] = input.pixels[i];
					else
						ret.pixels[i] = color0;
			}
		}
		return ret;
	}

	public static PImage intensityThreshold(PImage img, 
			float minR, float maxR,
			float minG, float maxG, 
			float minB, float maxB) {

		boolean invR = minR>maxR;
		boolean invG = minG>maxG;
		boolean invB = minB>maxB;

		PImage ret = app.createImage(img.width, img.height, PApplet.RGB);
		for(int i = 0; i < img.width * img.height; i++) {
			int p = img.pixels[i];
			if ( ((!invR && isIn(app.red(p), minR*255, maxR*255)) || (invR && !isIn(app.hue(p), maxR*255, minR*255)))  &&
					((!invG && isIn(app.green(p), minG*255, maxG*255)) || (invG && !isIn(app.brightness(p), maxG*255, minG*255))) &&
					((!invB && isIn(app.blue(p), minB*255, maxB*255)) || (invB && !isIn(app.saturation(p), maxB*255, minB*255))) )
				ret.pixels[i] = p;
			else
				ret.pixels[i] = color0;
		}
		return ret;
	}

	public static PImage colorThreshold(PImage img, 
			float minHue, float maxHue,			
			float minBright, float maxBright,	
			float minSatur, float maxSatur ) { 	

		boolean invHue = minHue>maxHue;
		boolean invBright = minBright>maxBright;
		boolean invSatur = minSatur>maxSatur;

		PImage ret = app.createImage(img.width, img.height, PApplet.RGB);
		for(int i = 0; i < img.width * img.height; i++) {
			int p = img.pixels[i];

			if ( ((!invHue && isIn(app.hue(p), minHue*255, maxHue*255)) || (invHue && !isIn(app.hue(p), maxHue*255, minHue*255)))  &&
					((!invBright && isIn(app.brightness(p), minBright*255, maxBright*255)) || (invBright && !isIn(app.brightness(p), maxBright*255, minBright*255))) &&
					((!invSatur && isIn(app.saturation(p), minSatur*255, maxSatur*255)) || (invSatur && !isIn(app.saturation(p), maxSatur*255, minSatur*255))) )
				ret.pixels[i] = p;
			else
				ret.pixels[i] = color0;
		}
		return ret;
	}

	public static PImage blur(PImage img) {
		return convolute(img, gaussianKernel, 99);
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

	static public PImage sobel(PImage img, float threshold) {
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

	static private PImage convolute(PImage img, float[][] kernel, float weight) {
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
