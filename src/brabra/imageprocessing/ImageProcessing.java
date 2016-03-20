package brabra.imageprocessing;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import brabra.ProMaster;
import processing.core.PApplet;
import processing.core.PImage;

public final class ImageProcessing extends ProMaster {
	public static final int nbParaBase = 12;
	public static final int nbParaInput = 4;
	public static final int nbParaBouton = 4;
	public static final float basicParaMaxValue = 255;

	// preloaded pixel int value (to avoid weird processing shit with multiple threads.) loaded by ImageAnalyser
	protected static int color0, color255;
	
	public static final float[] inputParaMaxValue = {
		200, //MaxMinVote
		200, //MaxNeighBour
		10,  //MaxLineKept
		1, 	 //MaxSobelThreshold
	};
	
	public static final float[] buttonParaMaxValue = {
		200, //MinVoteLeft
		200, //MaxVoteLeft -> #button pixel needed to have score 1
		200, //MinVoteRight 
		200, //MaxVoteTight
	};
	
	public static final float[] paraMovieBase = { 
		101.6f, 127.1f, //hue
		0, 209f, 		//bright
		37.8f, 255f,	//satur
		0, 255f, 		//r
		81.8f, 202f, 	//g
		51f, 236,		//b
		35f, 25.4f,		//min vote, neighbour
		6f, 0.3f };		//nb line kept, sobel threshold
	
	public float[] paraCameraBase = { 
		67.6f, 107.5f, 	//hue
		54.3f, 255f, 	//bright
		109f, 255f,		//satur
		0f, 125f, 		//r
		47.8f, 255f, 	//g
		0f, 175f,		//b
		50f, 38f,		//min vote, neighbour
		6f, 0.13f };	//nb line kept, sobel threshold
	public float[] paraBoutonsBase = { 
		243.7f, 6f, 	//hue
		0.0f,255f, 		//bright
		114f, 255f,		//satur
		0.0f, 255f, 	//r (3)
		0f, 255f, 		//g
		0.0f, 224.4f,	//b
		50f, 100f,		//vote map left button
		50f, 100f };	//vote map right button

	static public int rgbColor(int r, int g, int b, int a) {
		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;
		return (a << 24) + (r << 16) + (g << 8) + b;
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

	/** from 0 to 255 */
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
			if ( ((!invR && isIn(app.red(p), minR, maxR)) || (invR && !isIn(app.hue(p), maxR, minR)))  &&
					((!invG && isIn(app.green(p), minG, maxG)) || (invG && !isIn(app.brightness(p), maxG, minG))) &&
					((!invB && isIn(app.blue(p), minB, maxB)) || (invB && !isIn(app.saturation(p), maxB, minB))) )
				ret.pixels[i] = p;
			else
				ret.pixels[i] = color0;
		}
		return ret;
	}

	/** from 0 to 255 */
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

			if ( ((!invHue && isIn(app.hue(p), minHue, maxHue)) || (invHue && !isIn(app.hue(p), maxHue, minHue)))  &&
					((!invBright && isIn(app.brightness(p), minBright, maxBright)) || (invBright && !isIn(app.brightness(p), maxBright, minBright))) &&
					((!invSatur && isIn(app.saturation(p), minSatur, maxSatur)) || (invSatur && !isIn(app.saturation(p), maxSatur, minSatur))) )
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

	/** Open the parameter file selection dialogue. */
	public void selectParameters() {
		try {
			JFileChooser dialogue = new JFileChooser(new File("."));
			
			if (dialogue.showOpenDialog(null)== JFileChooser.APPROVE_OPTION) {
				File fichier = dialogue.getSelectedFile();
			    FileReader fileReader = new FileReader(new File(fichier.getPath()));
			    BufferedReader buffer = new BufferedReader(fileReader);
			    String s;
			    if ((s = buffer.readLine()) != null) {
			    	String[] sf = s.split(" ");
			    	float[] f = new float[sf.length];
			    	int nbParaBoutTot = nbParaBouton + nbParaBase, nbParaInTot = nbParaBase + nbParaInput;
			    	if (sf.length != nbParaInTot + nbParaBoutTot) { //! valid file
			    		JOptionPane.showMessageDialog(null, "can't understand that file", "sorry :'(", JOptionPane.INFORMATION_MESSAGE);
			    	} else {
			    		for (int i=0; i<sf.length; i++) {
				    		f[i] = Float.parseFloat(sf[i]);
				    	}
			    		float[] pCam = Arrays.copyOfRange(f, 0, nbParaInTot);
			    		float[] pBout = Arrays.copyOfRange(f, nbParaInTot, nbParaInTot + nbParaBoutTot);
			    		paraCameraBase = pCam;
			    		paraBoutonsBase = pBout;
			    		app.imgAnalyser.resetAllParameters(false);
			    	}
			    }
			    buffer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveParameters() {
		try {
			JFileChooser dialogue = new JFileChooser(new File("."));
			if (dialogue.showOpenDialog(null)== 
			    JFileChooser.APPROVE_OPTION) {
				File fichier = dialogue.getSelectedFile();
				PrintWriter sortie = new PrintWriter(new FileWriter(fichier.getPath(), false));
				for (int i=0; i<app.imgAnalyser.paraCamera.length; i++) {
					sortie.print(app.imgAnalyser.paraCamera[i] + " ");
				}
				for (int i=0; i<app.imgAnalyser.buttonDetection.paraBoutons.length; i++) {
					sortie.print(app.imgAnalyser.buttonDetection.paraBoutons[i] + " ");
				}
				sortie.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
