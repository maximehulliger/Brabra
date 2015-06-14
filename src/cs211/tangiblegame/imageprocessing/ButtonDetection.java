package cs211.tangiblegame.imageprocessing;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class ButtonDetection {
	PApplet app;
	Polygon quad = new Polygon();
	public PImage coco = null;
	public boolean leftVisible = false;
	public boolean rightVisible = false;
	
	/** Create a blob detection instance with the four corners of the Lego board. */
	public ButtonDetection(PApplet applet, PVector[] corners, PImage input) {
		
		this.app = applet;
		
		//TODO 1. threshold ?
		
		for (int i=0; i<corners.length; i++)
			quad.addPoint((int) corners[i].x, (int) corners[i].y);
		
		//2. First pass: label the pixels and store labels' equivalences
		int [][] labels= new int [input.width][input.height];
		List<TreeSet<Integer>> labelsEquivalences= new ArrayList<TreeSet<Integer>>();
		int currentLabel=1;
		
		for(int x = 0; x < input.width; x++) {
			for(int y = 0; y < input.height; y++) {
				int pixel = ImageProcessing.pixel(input, x, y);
				if (!isInQuad(x, y) || pixel == applet.color(0))
					continue;
				
				// on cherche le label minimum autour.
				int[][] vois = new int[][] {{x+1, y}, {x, y+1}, {x-1, y}, {x, y-1}, 
											{x+1, y+1}, {x-1, y+1}, {x-1, y-1}, {x+1, y-1}};
				int minLab = Integer.MAX_VALUE;
				for (int[] v : vois) {
					int pVois = ImageProcessing.pixel(input, v[0], v[1]);
					if ( pVois != applet.color(0) ) {
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
		Map<Integer, Integer[]> blobs = new HashMap<>(); //[tot x, tot y, nbPixel, color]
		Random rand = new Random();
		for (int finalLab : allFinalLabel) {
			Integer[] blob = { 0, 0, 0, applet.color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)) };
			blobs.put(finalLab, blob);
		}

		// a4. Second pass: re-label the pixels by their equivalent class
		for(int x = 0; x < input.width; x++) {
			for(int y = 0; y < input.height; y++) {
				if (!isInQuad(x, y))
					continue;
				int lab = labels[x][y];
				if (lab > 0) {
					int finLab = labelsEquivalences.get(lab-1).first();
					labels[x][y] = finLab;
					Integer[] blob = blobs.get(finLab);
					blob[0] += x;
					blob[1] += y;
					blob[2] ++;
				}
			}
		}
		
		// a5. output an image with each blob colored in one uniform color.
		coco = new PImage(input.width, input.height);
		for(int x = 0; x < input.width; x++) {
			for(int y = 0; y < input.height; y++) {
				int lab = labels[x][y];
				if (lab > 0)
					coco.pixels[input.width*y + x] = blobs.get(lab)[3];
			}
		}
		
		//b4. compute blob
		/*List<Integer[]> finalBlobs = new ArrayList<>();
		for (Integer[] b : blobs.values()) {
			finalBlobs.add( new Integer[] { b[0]/b[2], b[1]/b[2] } );
			coco.set(b[0]/b[2], b[1]/b[2], 0);
		}*/
		
		//6. set button state
		int middleX = 0;
		for (PVector v : corners)
			middleX += v.x;
		middleX /= 4;
		
		for (Integer[] b : blobs.values()) {
			int weight = b[2];
			if (weight < 40)
				continue;
			int x = b[0]/weight;
			if (x < middleX)
				leftVisible = true;
			else
				rightVisible = true;
		}
		return;
	}
	
	/** Returns true if a (x,y) point lies inside the quad */
	public boolean isInQuad(int x, int y) {return quad.contains(x, y);}
}
