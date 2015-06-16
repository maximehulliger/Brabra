package cs211.tangiblegame.imageprocessing;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class ButtonDetection {
	private final ImageProcessing imgPro;
	private PImage input;
	private PVector[] corners;
	public int minVote = 30;
	public boolean leftVisible = false;
	public boolean rightVisible = false;
	private Collection<Integer[]> blobs;
	
	public ButtonDetection(ImageProcessing imgPro) {
		this.imgPro = imgPro;
	}
	
	public void setInput(PImage input, PVector[] corners) {
		this.input = input;
		this.corners = corners;
	}
	
	public void drawButtons(PGraphics input) {
		for (Integer[] b : this.blobs) {
			int bx = b[0];
			int by = b[1];
			float rayon = b[2];
			if (b[2] >= minVote)
				input.fill(imgPro.colorButtonOk);
			else {
				input.fill(imgPro.colorButtonRejected);
			}
			input.ellipse(bx - rayon/2, by - rayon/2, rayon, rayon);
		}
	}
	
	/** detect blobs instance within the four corners of the Lego board from a thresholded image of the buttons. */
	public void detect() {
		Polygon quad = new Polygon();
		for (int i=0; i<corners.length; i++)
			quad.addPoint((int) corners[i].x, (int) corners[i].y);
		
		//2. First pass: label the pixels and store labels' equivalences
		int [][] labels= new int [input.width][input.height];
		List<TreeSet<Integer>> labelsEquivalences= new ArrayList<TreeSet<Integer>>();
		int currentLabel=1;
		
		for(int x = 0; x < input.width; x++) {
			for(int y = 0; y < input.height; y++) {
				int pixel = ImageProcessing.pixel(input, x, y);
				if (!quad.contains(x, y) || pixel == imgPro.color0)
					continue;
				
				// on cherche le label minimum autour.
				int[][] vois = new int[][] {{x+1, y}, {x, y+1}, {x-1, y}, {x, y-1}, 
											{x+1, y+1}, {x-1, y+1}, {x-1, y-1}, {x+1, y-1}};
				int minLab = Integer.MAX_VALUE;
				for (int[] v : vois) {
					if(!ImageProcessing.isIn(v[0], 0, input.width-1) || !ImageProcessing.isIn(v[1], 0, input.height-1))
						continue;
					int pVois = ImageProcessing.pixel(input, v[0], v[1]);
					if ( pVois != imgPro.color0 ) {
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
						if(!ImageProcessing.isIn(v[0], 0, input.width-1) || !ImageProcessing.isIn(v[1], 0, input.height-1))
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
		Map<Integer, Integer[]> blobs = new HashMap<>(); //[tot x, tot y, nbPixel]
		for (int finalLab : allFinalLabel) {
			Integer[] blob = { 0, 0, 0 };
			blobs.put(finalLab, blob);
		}

		// 4. Second pass: re-label the pixels by their equivalent class & update blob score
		for(int x = 0; x < input.width; x++) {
			for(int y = 0; y < input.height; y++) {
				if (!quad.contains(x, y))
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
		
		//5. compute blob & state
		this.blobs = blobs.values();
		for (Integer[] b : this.blobs) {
			b[0] /= b[2];
			b[1] /= b[2];
		}
		
		//6. set button state
		int middleX = 0;
		for (PVector v : corners)
			middleX += v.x;
		middleX /= 4;
		
		for (Integer[] b : this.blobs) {
			if (b[2] >= minVote) {
				if (b[0] < middleX)
					leftVisible = true;
				else
					rightVisible = true;
			}
		}
	}
}
