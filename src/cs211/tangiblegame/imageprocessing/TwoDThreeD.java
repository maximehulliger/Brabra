package cs211.tangiblegame.imageprocessing;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import processing.core.PVector;
import papaya.*;

public class TwoDThreeD {
	
	// default focal length, well suited for most webcams
	static float f = 700;
	
	// intrisic camera matrix
	static float [][] K = {{f,0,0},
			        	   {0,f,0},
			        	   {0,0,1}};
	
	// Real physical coordinates of the Lego board in mm
	static float boardSize = 380.f; // large Duplo board
	//static float boardSize = 255.f; // smaller Lego board
	
	// the 3D coordinates of the physical board corners, clockwise
	static float [][] physicalCorners = {
		{-128, -128, 0, 1}, {128, -128, 0, 1}, {128, 128, 0, 1}, {-128, 128, 0, 1}
							// Store here the 3D coordinates of the corners of
							// the real Lego board, in homogenous coordinates
							// and clockwise.
							};
	
	public TwoDThreeD(int width, int height) {
		
		// set the offset to the center of the webcam image
		K[0][2] = 0.5f * width;
		K[1][2] = 0.5f * height;
	}
	
	static class CWComparator implements Comparator<PVector> {
		PVector center;
		public CWComparator(PVector center) {
			this.center = center;
		}
		public int compare(PVector b, PVector d) {
			if(Math.atan2(b.y-center.y,b.x-center.x)<Math.atan2(d.y-center.y,d.x-center.x))
				return -1;
			else return 1;
		}
	}
	public static List<PVector> sortCorners(List<PVector> quad){
		// Sort corners so that they are ordered clockwise
		PVector a = quad.get(0);
		PVector b = quad.get(2);
		PVector center = new PVector((a.x+b.x)/2,(a.y+b.y)/2);
		Collections.sort(quad,new CWComparator(center));

		int idxBest = -1;
		float distBest = Float.MAX_VALUE;
		for (int i=0; i<quad.size(); i++) {
			float dist = quad.get(i).magSq();
			if (dist < distBest) {
				idxBest = i;
				distBest = dist;
			}
		}
		Collections.rotate(quad, quad.size() - idxBest);
		return quad;
	}


	public PVector get3DRotations(List<PVector> points2Dunordered) {
		List<PVector> points2Dordered = sortCorners(points2Dunordered);
		for (PVector p : points2Dordered)
			p.z = 1;
		
		// 1- Solve the extrinsic matrix from the projected 2D points
		double[][] E = solveExtrinsicMatrix(points2Dordered);
		
		
        // 2 - Re-build a proper 3x3 rotation matrix from the camera's 
		//     extrinsic matrix E
        float[] firstColumn = {(float)E[0][0],
        					   (float)E[1][0],
        					   (float)E[2][0]};
        firstColumn = Mat.multiply(firstColumn, 1/Mat.norm2(firstColumn)); // normalize
        
        float[] secondColumn={(float)E[0][1],
        					  (float)E[1][1],
        					  (float)E[2][1]};
        secondColumn = Mat.multiply(secondColumn, 1/Mat.norm2(secondColumn)); // normalize
        
        float[] thirdColumn = Mat.cross(firstColumn, secondColumn);
        
        float[][] rotationMatrix = {
        		{firstColumn[0], secondColumn[0], thirdColumn[0]},
                {firstColumn[1], secondColumn[1], thirdColumn[1]},
                {firstColumn[2], secondColumn[2], thirdColumn[2]}
               };
        
        // 3 - Computes and returns Euler angles (rx, ry, rz) from this matrix
        return rotationFromMatrix(rotationMatrix);
	}
		
		
	private double[][] solveExtrinsicMatrix(List<PVector> points2D) {
	
		// p ~= K · [R|t] · P
		// with P the (3D) corners of the physical board, p the (2D) 
		// projected points onto the webcam image, K the intrinsic 
		// matrix and R and t the rotation and translation we want to 
		// compute.
		//
		// => We want to solve: (K^(-1) · p) X ([R|t] · P) = 0
		
		float [][] invK=Mat.inverse(K);

		float[][] projectedCorners = new float[4][3];
		
		for(int i=0;i<4;i++){
			projectedCorners[i] = Mat.multiply(invK, points2D.get(i).array());
		}
		
		// 'A' contains the cross-product (K^(-1) · p) X P
	    float[][] A= new float[12][9];
	    
	    for(int i=0;i<4;i++){
	      A[i*3][0]=0;
	      A[i*3][1]=0;
	      A[i*3][2]=0;
	      
	      // note that we take physicalCorners[0,1,*3*]: we drop the Z
	      // coordinate and use the 2D homogenous coordinates of the physical
	      // corners
	      A[i*3][3]=-projectedCorners[i][2] * physicalCorners[i][0];
	      A[i*3][4]=-projectedCorners[i][2] * physicalCorners[i][1];
	      A[i*3][5]=-projectedCorners[i][2] * physicalCorners[i][3];

	      A[i*3][6]= projectedCorners[i][1] * physicalCorners[i][0];
	      A[i*3][7]= projectedCorners[i][1] * physicalCorners[i][1];
	      A[i*3][8]= projectedCorners[i][1] * physicalCorners[i][3];

	      A[i*3+1][0]= projectedCorners[i][2] * physicalCorners[i][0];
	      A[i*3+1][1]= projectedCorners[i][2] * physicalCorners[i][1];
	      A[i*3+1][2]= projectedCorners[i][2] * physicalCorners[i][3];
	      
	      A[i*3+1][3]=0;
	      A[i*3+1][4]=0;
	      A[i*3+1][5]=0;
	      
	      A[i*3+1][6]=-projectedCorners[i][0] * physicalCorners[i][0];
	      A[i*3+1][7]=-projectedCorners[i][0] * physicalCorners[i][1];
	      A[i*3+1][8]=-projectedCorners[i][0] * physicalCorners[i][3];

	      A[i*3+2][0]=-projectedCorners[i][1] * physicalCorners[i][0];
	      A[i*3+2][1]=-projectedCorners[i][1] * physicalCorners[i][1];
	      A[i*3+2][2]=-projectedCorners[i][1] * physicalCorners[i][3];
	      
	      A[i*3+2][3]= projectedCorners[i][0] * physicalCorners[i][0];
	      A[i*3+2][4]= projectedCorners[i][0] * physicalCorners[i][1];
	      A[i*3+2][5]= projectedCorners[i][0] * physicalCorners[i][3];
	      
	      A[i*3+2][6]=0;
	      A[i*3+2][7]=0;
	      A[i*3+2][8]=0;
	    }

	    SVD svd=new SVD(A);
	    
	    double[][] V = svd.getV();
	    
	    double[][] E = new double[3][3];
	    
	    //E is the last column of V
	    for(int i=0;i<9;i++){
	    	E[i/3][i%3] = V[i][V.length-1] / V[8][V.length-1];
	    }
	    
	    return E;

	}
	  
	private PVector rotationFromMatrix(float[][]  mat) {

		// Assuming rotation order is around x,y,z
		PVector rot = new PVector();
		
		if(mat[1][0] > 0.998) { // singularity at north pole
			rot.z = 0;
			float delta = (float) Math.atan2(mat[0][1],mat[0][2]);
			rot.y = -(float) Math.PI/2;
			rot.x = -rot.z + delta;
			return rot;
		}

		if(mat[1][0] < -0.998) { // singularity at south pole
			rot.z = 0;
			float delta = (float) Math.atan2(mat[0][1],mat[0][2]);
			rot.y = (float) Math.PI/2;
			rot.x = rot.z + delta;
			return rot;
		}

		rot.y =-(float)Math.asin(mat[2][0]);
		rot.x = (float)Math.atan2(mat[2][1]/Math.cos(rot.y), mat[2][2]/Math.cos(rot.y));
		rot.z = (float)Math.atan2(mat[1][0]/Math.cos(rot.y), mat[0][0]/Math.cos(rot.y));

		return rot;
	}
}