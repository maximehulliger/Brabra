package brabra.imageprocessing;

import java.util.List;
import java.util.ArrayList;

import processing.core.PVector;

public class QuadGraph {

    List<int[]> cycles = new ArrayList<int[]>();
    int[][] graph;

    public void build(List<HoughLine.Line> lines, int width, int height) {
        
        int n = lines.size();
        
        // The maximum possible number of edges is sum(0..n) = n * (n + 1)/2
        graph = new int[n * (n + 1)/2][2];
                
        int idx = 0;
        
        for (int i = 0; i < lines.size(); i++) {
            for (int j = i + 1; j < lines.size(); j++) {
                if (intersect(lines.get(i), lines.get(j), width, height)) {
                	graph[idx++] = new int[] {i, j};
                }
            }
        }
    }

    /** Returns true if polar lines 1 and 2 intersect 
     * inside an area of size (width, height)
     */
    public static boolean intersect(HoughLine.Line line1, HoughLine.Line line2, int width, int height) {
    	
    	float d = line2.cosPhi*line1.sinPhi - line1.cosPhi*line2.sinPhi;
		if (d == 0)
			return false;
		
		int x = Math.round((line2.r*line1.sinPhi - line1.r*line2.sinPhi)/d);
		int y = Math.round(-(line2.r*line1.cosPhi - line1.r*line2.cosPhi)/d);
		
        if (0 <= x && 0 <= y && width >= x && height >= y)
            return true;
        else
            return false;

    }
    
    List<int[]> findCycles() {
        cycles.clear();
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph[i].length; j++) {
                findNewCycles(new int[] {graph[i][j]});
            }
        }
        return cycles;
    }

    void findNewCycles(int[] path)
    {
            int n = path[0];
            int x;
            int[] sub = new int[path.length + 1];

            for (int i = 0; i < graph.length; i++)
                for (int y = 0; y <= 1; y++)
                    if (graph[i][y] == n)
                    //  edge refers to our current node
                    {
                        x = graph[i][(y + 1) % 2];
                        if (!visited(x, path))
                        //  neighbor node not on path yet
                        {
                            sub[0] = x;
                            System.arraycopy(path, 0, sub, 1, path.length);
                            //  explore extended path
                            findNewCycles(sub);
                        }
                        else if ((path.length > 2) && (x == path[path.length - 1]))
                        //  cycle found
                        {
                            int[] p = normalize(path);
                            int[] inv = invert(p);
                            if (isNew(p) && isNew(inv) && p.length == 4)
                            {
                                cycles.add(p);
                            }
                        }
                    }
    }

    //  check of both arrays have same lengths and contents
    static Boolean equals(int[] a, int[] b)
    {
        Boolean ret = (a[0] == b[0]) && (a.length == b.length);

        for (int i = 1; ret && (i < a.length); i++)
        {
            if (a[i] != b[i])
            {
                ret = false;
            }
        }

        return ret;
    }

    //  create a path array with reversed order
    static int[] invert(int[] path)
    {
        int[] p = new int[path.length];

        for (int i = 0; i < path.length; i++)
        {
            p[i] = path[path.length - 1 - i];
        }

        return normalize(p);
    }

    //  rotate cycle path such that it begins with the smallest node
    static int[] normalize(int[] path)
    {
        int[] p = new int[path.length];
        int x = smallest(path);
        int n;

        System.arraycopy(path, 0, p, 0, path.length);

        while (p[0] != x)
        {
            n = p[0];
            System.arraycopy(p, 1, p, 0, p.length - 1);
            p[p.length - 1] = n;
        }

        return p;
    }

    //  compare path against known cycles
    //  return true, iff path is not a known cycle
    Boolean isNew(int[] path)
    {
        Boolean ret = true;

        for(int[] p : cycles)
        {
            if (equals(p, path))
            {
                ret = false;
                break;
            }
        }

        return ret;
    }

    //  return the int of the array which is the smallest
    static int smallest(int[] path)
    {
        int min = path[0];

        for (int p : path)
        {
            if (p < min)
            {
                min = p;
            }
        }

        return min;
    }

    //  check if vertex n is contained in path
    static Boolean visited(int n, int[] path)
    {
        Boolean ret = false;

        for (int p : path)
        {
            if (p == n)
            {
                ret = true;
                break;
            }
        }

        return ret;
    }
    
    /** Check if a quad is convex or not.
     * 
     * Algo: take two adjacent edges and compute their cross-product. 
     * The sign of the z-component of all the cross-products is the 
     * same for a convex polygon.
     * 
     * See http://debian.fmi.uni-sofia.bg/~sergei/cgsr/docs/clockwise.htm
     * for justification.
     * 
     * @param c1
     */
    public static boolean isConvex(PVector c1,PVector c2,PVector c3,PVector c4){
        
        PVector v21= PVector.sub(c1, c2);
        PVector v32= PVector.sub(c2, c3);
        PVector v43= PVector.sub(c3, c4);
        PVector v14= PVector.sub(c4, c1);
  
        float i1=v21.cross(v32).z;
        float i2=v32.cross(v43).z;
        float i3=v43.cross(v14).z;
        float i4=v14.cross(v21).z;
        
        if(   (i1>0 && i2>0 && i3>0 && i4>0) 
           || (i1<0 && i2<0 && i3<0 && i4<0))
            return true;
        else {
        	if (ImageAnalyser.displayQuadRejectionCause)
        		System.out.println("Eliminating non-convex quad");
            return false;
        }
   }

    /** Compute the area of a quad, and check it lays within a specific range
     */
    public static boolean validArea(PVector c1,PVector c2,PVector c3,PVector c4, float max_area, float min_area){
        
        PVector v21= PVector.sub(c1, c2);
        PVector v32= PVector.sub(c2, c3);
        PVector v43= PVector.sub(c3, c4);
        PVector v14= PVector.sub(c4, c1);
  
        float i1=v21.cross(v32).z;
        float i2=v32.cross(v43).z;
        float i3=v43.cross(v14).z;
        float i4=v14.cross(v21).z;
        
        float area = Math.abs(0.5f * (i1 + i2 + i3 + i4));
        
        boolean valid = (area < max_area && area > min_area);
   
        if (!valid)
        	if (ImageAnalyser.displayQuadRejectionCause)
        		System.out.println("Area out of range");
        
        return valid;
   }
  
    /** Compute the (cosine) of the four angles of the quad, and check they are all large enough
     * (the quad representing our board should be close to a rectangle)
     */
    public static boolean nonFlatQuad(PVector c1,PVector c2,PVector c3,PVector c4){
        
        float min_cos = 0.3f; // cos(70deg) ~= 0.3
        
        PVector v21= PVector.sub(c1, c2);
        PVector v32= PVector.sub(c2, c3);
        PVector v43= PVector.sub(c3, c4);
        PVector v14= PVector.sub(c4, c1);
  
        float cos1=Math.abs(v21.dot(v32) / (v21.mag() * v32.mag()));
        float cos2=Math.abs(v32.dot(v43) / (v32.mag() * v43.mag()));
        float cos3=Math.abs(v43.dot(v14) / (v43.mag() * v14.mag()));
        float cos4=Math.abs(v14.dot(v21) / (v14.mag() * v21.mag()));
    
        if (cos1 < min_cos && cos2 < min_cos && cos3 < min_cos && cos4 < min_cos)
            return true;
        else {
        	if (ImageAnalyser.displayQuadRejectionCause)
        		System.out.println("Flat quad");
            return false;
        }
   }
    

}

