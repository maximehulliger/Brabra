package brabra.imageprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import brabra.ProMaster;
import brabra.game.physic.geo.Vector;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
 
public class HoughLine extends ProMaster {
	private static final float discretizationStepsPhi = 0.06f;
	private static final float discretizationStepsR = 2.5f;
	public static int minVotes = 50;
	public static int neighbourhood = 38; // size of the region we search for a local maximum
	public static int maxKeptLines = 6;

	// preloaded pixel int value (to avoid weird processing shit with multiple threads.) loaded by ImageAnalyser
	protected static int colorQuad;
		
	
	private PApplet app;
	private PImage edgeImg;
	private int phiDim;
	private int rDim;
	private static float[] sinTab = null;
	private static float[] cosTab = null;
	private int[] accumulator;
	public List<Line> lines = new ArrayList<Line>();;
	public Vector[] quad;
	
	public HoughLine(PImage edgeImg, PApplet app) {
		this.app = app;
		this.edgeImg = edgeImg;
		float phiMax = PApplet.PI;
		this.phiDim = (int) (phiMax / discretizationStepsPhi);
		float rMax = edgeImg.width + edgeImg.height; //r can be pos or neg
		this.rDim = (int) ( ((rMax*2)+1) / discretizationStepsR );
		
		
		//1. pre-compute the sin and cos values if needed
		if (sinTab == null) {
			float ang = 0;
			sinTab = new float[phiDim];
			cosTab = new float[phiDim];
			for (int accPhi = 0; accPhi < phiDim; ang += discretizationStepsPhi, accPhi++) {
				sinTab[accPhi] = PApplet.sin(ang);
				cosTab[accPhi] = PApplet.cos(ang);
			}
		}
		
		//2. create accumulator
		this.accumulator = new int[(phiDim + 2) * (rDim + 2)]; //1 pixel margin
		//on edge points (white), store all possible (r, phi) pairs describing lines going through the point.
		for (int y = 0; y < edgeImg.height; y++) {
			for (int x = 0; x < edgeImg.width; x++) {
				if (app.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					for(int iPhi = 0; iPhi < phiDim; iPhi ++) {
						float phi = iPhi*discretizationStepsPhi;
						float r = x*PApplet.cos(phi) + y*PApplet.sin(phi);
						if (PApplet.abs(r) < rMax) {
							int accR = Math.round( r/discretizationStepsR + (rDim - 1)*0.5f );
							int idx = accR + (iPhi + 1) * (rDim + 2) + 1;
							accumulator[ idx ]++;
						}
					}
				}
			}
		}
		
		//3. get reduced lines
		// only search around lines with more that this amount of votes
		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > minVotes) {
					boolean bestCandidate=true;
					// iterate over the neighbourhood that are in the image
					for(int dPhi=-neighbourhood/2; dPhi < neighbourhood/2+1; dPhi++) {
						if( accPhi+dPhi < 0 || accPhi+dPhi >= phiDim) continue;
						for(int dR=-neighbourhood/2; dR < neighbourhood/2 +1; dR++) {
							if(accR+dR < 0 || accR+dR >= rDim) continue;
							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;
							if(accumulator[idx] < accumulator[neighbourIdx]) {
								bestCandidate=false;
								break;
							}
						}
						if(!bestCandidate) break;
					}
					if (bestCandidate)
						lines.add(new Line(idx));
				}
			}
		}
		Collections.sort(lines, new HoughComparator());
		//System.out.println("["+lines.size()+"] lignes.");
		lines = lines.subList(0, PApplet.min(lines.size(), maxKeptLines));
		
		//4. get quad
		quad = getQuad();
	}
	
	public ArrayList<Vector> quad() {
		ArrayList<Vector> ret = new ArrayList<Vector>();
		ret.add(quad[0]); ret.add(quad[1]); ret.add(quad[2]); ret.add(quad[3]);
		return ret;
	}
	
	public PImage accumulatorImg() {
		PImage houghImg = app.createImage(rDim + 2, phiDim + 2, PApplet.ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			houghImg.pixels[i] = ImageProcessing.rgbColor( PApplet.min(255, accumulator[i]) );
		}
		return houghImg;
	}
		
	public void drawLines(PGraphics pg) {
		for (Line line : lines) {
			//compute the intersection of this line with the 4 borders of the image
			int x0 = 0;
			int y0 = (int) (line.r / line.sinPhi);
			int x1 = (int) (line.r / line.cosPhi);
			int y1 = 0;
			int x2 = edgeImg.width;
			int y2 = (int) (-line.cosPhi / line.sinPhi * x2 + line.r / line.sinPhi);
			int y3 = edgeImg.width;
			int x3 = (int) (-(y3 - line.r / line.sinPhi) * (line.sinPhi / line.cosPhi));
			
			// Finally, plot the lines
			pg.stroke(204,102,0);
			if (y0 > 0) {
				if (x1 > 0)
					pg.line(x0, y0, x1, y1);
				else if (y2 > 0)
					pg.line(x0, y0, x2, y2);
				else
					pg.line(x0, y0, x3, y3);
			} else {
				if (x1 > 0) {
					if (y2 > 0)
						pg.line(x1, y1, x2, y2);
					else
						pg.line(x1, y1, x3, y3);
				} else
					pg.line(x2, y2, x3, y3);
			}
		}
	}
	
	
	public void drawQuad(PGraphics pg) {
		if (quad != null) {
			// Choose a random, semi-transparent colour
			pg.fill(colorQuad);
			pg.quad(quad[0].x, quad[0].y,
					quad[1].x, quad[1].y,
					quad[2].x, quad[2].y,
					quad[3].x, quad[3].y);
		}
	}
	
	//retourne les 4 coins d'un quad valide à partir des lignes, ou null si pas de quad valide détecté.
	private Vector[] getQuad() {
		if (lines.size() < 4)
			return null;	
		
		QuadGraph qgraph = new QuadGraph();
		qgraph.build(lines, edgeImg.width, edgeImg.height);
		
		for (int[] quad : qgraph.findCycles()) {
			Line l1 = lines.get(quad[0]);
			Line l2 = lines.get(quad[1]);
			Line l3 = lines.get(quad[2]);
			Line l4 = lines.get(quad[3]);
			Vector c12 = l1.intersection(l2);
			Vector c23 = l2.intersection(l3);
			Vector c34 = l3.intersection(l4);
			Vector c41 = l4.intersection(l1);
			
			//filter quads
			if (QuadGraph.validArea(c12, c23, c34, c41, 170_000, 6_000) &&
					QuadGraph.isConvex(c12, c23, c34, c41)){ //&&
					//QuadGraph.nonFlatQuad(c12, c23, c34, c41)) {
						return new Vector[] {c12, c23, c34, c41};
			}
		}
		return null;
	}
	
	public class Line {
		public final int idx;
		public final int votes;
		public final float cosPhi;
		public final float sinPhi;
		public final float r;
		public Line(int idx) {
			this.idx = idx;
			int iPhi = (int) (idx / (rDim + 2)) - 1;
			int iR = idx - (iPhi + 1) * (rDim + 2) - 1;
			this.r = (iR - (rDim - 1) * 0.5f) * discretizationStepsR;
			this.votes = accumulator[idx];
			this.cosPhi = cosTab[iPhi];
			this.sinPhi = sinTab[iPhi];
		}
		
		public Vector intersection(Line other) {
			float d = other.cosPhi*this.sinPhi - this.cosPhi*other.sinPhi;
			int x = Math.round((other.r*this.sinPhi - this.r*other.sinPhi)/d);
			int y = Math.round(-(other.r*this.cosPhi - this.r*other.cosPhi)/d);
			return new Vector(x, y);
		}
	}

	private class HoughComparator implements Comparator<Line> {
		public int compare(Line l1, Line l2) {
			if (l1.votes > l2.votes
					|| (l1.votes == l2.votes && l1.idx < l2.idx)) return -1;
			return 1;
		}
	}
}
