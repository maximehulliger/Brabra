package cs211.tangiblegame.imageprocessing;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import cs211.tangiblegame.ProMaster;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class ButtonDetection extends ProMaster {
	private static final float maxRayon = 60;
	private static final int overHeadVote = 2500;
	private static final boolean printButtonScore = false;
	
	// preloaded pixel int value (to avoid weird processing shit with multiple threads.) loaded by ImageAnalyser
	protected static int colorButtonOk, colorButtonRejected;
	
	
	public final ReentrantLock inputLock = new ReentrantLock();
	public float[] paraBoutons;
	private PImage inputImg;
	
	public final ReentrantLock outputLock = new ReentrantLock();
	private float leftScore = -1, rightScore = -1; //[0, 1], -1 si invisible, 0 si presque
	public final ReentrantLock jobOverLock = new ReentrantLock();
	public PImage threshold2Button;
	
	private PVector[] corners;
	private List<Integer[]> blobs = null;
	
	public ButtonDetection() {
		paraBoutons = app.imgAnalyser.imgProc.paraBoutonsBase.clone();
	}
	
	/** [0, 1], -1 if invisible, 0 if nearly visible */
	public float rightScore() {
		try {
			outputLock.lock();
			return rightScore;
		} finally {
			outputLock.unlock();
		}
	}
	
	public float leftScore() {
		try {
			outputLock.lock();
			return leftScore;
		} finally {
			outputLock.unlock();
		}
	}
	
	public void setInput(PImage input, PVector[] corners) {
		this.inputImg = input;
		this.corners = corners;
	}
	
	public void drawButtons(PGraphics input) {
		if (blobs != null) {
			for (Integer[] b : blobs) {
				int bx = b[0];
				int by = b[1];
				float rayon = b[2]*maxRayon/b[5];
				if (b[3] == 2) 
					input.stroke(0, 255, 0, 255);
				else
					input.noStroke();
				
				if (b[3] >= 1)
					input.fill(colorButtonOk);
				else if (b[3] == 0)
					input.fill(colorButtonRejected);
				
				
				input.ellipse(bx, by, rayon, rayon);
			}
		}
	}
	
	public void resetOutput() {
		outputLock.lock();
		leftScore = -1;
		rightScore = -1;
		blobs = null;
		outputLock.unlock();
	}
	
	/** detect blobs instance within the four corners of the Lego board from a thresholded image of the buttons. */
	public void detect() {
		jobOverLock.lock();
		Polygon quad = new Polygon();
		for (int i=0; i<corners.length; i++)
			quad.addPoint((int) corners[i].x, (int) corners[i].y);
		
		//1. threshold the image
		PImage quadFiltered = ImageProcessing.quadFilter(inputImg, quad);
		inputLock.lock();
		PImage threshold1r = ImageProcessing.colorThreshold(quadFiltered, paraBoutons[0], paraBoutons[1], paraBoutons[2], paraBoutons[3], paraBoutons[4], paraBoutons[5]);
		PImage bluredr = ImageProcessing.blur(threshold1r);
		threshold2Button = ImageProcessing.intensityThreshold(bluredr, paraBoutons[6], paraBoutons[7], paraBoutons[8], paraBoutons[9], paraBoutons[10], paraBoutons[11]);
		inputLock.unlock();
		
		//2. First pass: label the pixels and store labels' equivalences
		int [][] labels= new int [inputImg.width][inputImg.height];
		List<TreeSet<Integer>> labelsEquivalences= new ArrayList<TreeSet<Integer>>();
		int currentLabel=1;
		
		for(int x = 0; x < inputImg.width; x++) {
			for(int y = 0; y < inputImg.height; y++) {
				int pixel = ImageProcessing.pixel(threshold2Button, x, y);
				if (!quad.contains(x, y) || pixel == ImageProcessing.color0)
					continue;
				
				// on cherche le label minimum autour.
				int[][] vois = new int[][] {{x+1, y}, {x, y+1}, {x-1, y}, {x, y-1}, 
											{x+1, y+1}, {x-1, y+1}, {x-1, y-1}, {x+1, y-1}};
				int minLab = Integer.MAX_VALUE;
				for (int[] v : vois) {
					if(!ImageProcessing.isIn(v[0], 0, inputImg.width-1) || !ImageProcessing.isIn(v[1], 0, inputImg.height-1))
						continue;
					int pVois = ImageProcessing.pixel(threshold2Button, v[0], v[1]);
					if ( pVois != ImageProcessing.color0 ) {
						int labVois = labels[v[0]][v[1]];
						if ( labVois>0 && labVois<minLab )
							minLab = labVois;
					}
				}
				
				if (minLab == Integer.MAX_VALUE) {
					//aucun voisin connu -> nouvel id
					labels[x][y] = currentLabel;
					TreeSet<Integer> newEqu = new TreeSet<>();
					newEqu.add(currentLabel);
					labelsEquivalences.add(newEqu);
					currentLabel++;
				} else {
					//le pixel récupère l'id le plus petit de ses voisins
					labels[x][y] = minLab;
					//on lie autre -> min
					for (int[] v : vois) {
						if(!ImageProcessing.isIn(v[0], 0, inputImg.width-1) || !ImageProcessing.isIn(v[1], 0, inputImg.height-1))
							continue;
						int labVois = labels[v[0]][v[1]];
						if ( labVois!= 0 && labVois > minLab) {
							TreeSet<Integer> labTree = labelsEquivalences.get(labVois-1);
							TreeSet<Integer> minTree = labelsEquivalences.get(minLab-1);
							minTree.addAll(labTree);
							labTree.addAll(minTree);
						}
					}
				}
			}
		}
		TreeSet<Integer> allFinalLabel = new TreeSet<>();
		for(TreeSet<Integer> t : labelsEquivalences) {
			allFinalLabel.add( t.first() );
		}
		
		// 3. on regarde les blobs qui vont sortir
		Map<Integer, Integer[]> blobsAcc = new HashMap<>(); //[tot x, tot y, nbPixel]
		for (int finalLab : allFinalLabel) {
			blobsAcc.put(finalLab, new Integer[] { 0, 0, 0 });
		}

		// 4. Second pass: re-label the pixels by their equivalent class & update blob score
		for(int x = 0; x < inputImg.width; x++) {
			for(int y = 0; y < inputImg.height; y++) {
				if (!quad.contains(x, y))
					continue;
				int lab = labels[x][y];
				if (lab > 0) {
					int finLab = labelsEquivalences.get(lab-1).first();
					labels[x][y] = finLab;
					Integer[] blob = blobsAcc.get(finLab);
					blob[0] += x;
					blob[1] += y;
					blob[2] ++;
				}
			}
		}
		
		//5. compute blobs & button score/state
		blobs = new ArrayList<>( blobsAcc.size() );
		int middleX = 0;
		for (PVector v : corners)
			middleX += v.x;
		middleX /= 4;
		int rightScoreApp = 0, leftScoreApp = 0;
		
		outputLock.lock();
		for (Integer[] b : blobsAcc.values()) {
			int x = b[0] / b[2];
			int y = b[1] / b[2];
			int etat = 0;
			int minVote = 0;
			int maxVote = 0;
			if (b[2] < overHeadVote) {
				if (x > middleX) {
					minVote = (int)paraBoutons[14];
					maxVote = (int)paraBoutons[15];
					if (b[2] > paraBoutons[15]) {
						b[2] = (int)paraBoutons[15];
						etat = 2;
					} else if (b[2] > paraBoutons[14])
						etat = 1;
					if (b[2] > rightScoreApp)
						rightScoreApp = b[2];
				} else if (b[2] > leftScoreApp) {
					minVote = (int)paraBoutons[12];
					maxVote = (int)paraBoutons[13];
					if (b[2] > paraBoutons[13]) {
						b[2] = (int)paraBoutons[13];
						etat = 2;
					} else if (b[2] > paraBoutons[12])
						etat = 1;
					if (b[2] > rightScoreApp)
						leftScoreApp = b[2];
				}
			}
			if (b[2] > 2) //bruit <= 2
				blobs.add( new Integer[] { x, y, b[2], etat, minVote, maxVote } );
		}
		leftScore = PApplet.map(leftScoreApp, paraBoutons[12], paraBoutons[13], 0, 1);
		rightScore = PApplet.map(rightScoreApp, paraBoutons[14], paraBoutons[15], 0, 1);
		outputLock.unlock();
		jobOverLock.unlock();
		if (printButtonScore) {
			System.out.println("bout: gauche: "+leftScore+", droite: "+rightScore);
		}
	}
}
