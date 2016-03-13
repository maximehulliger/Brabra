package cs211.tangiblegame.geo;

import cs211.tangiblegame.geo.Line.Projection;
import cs211.tangiblegame.physic.PseudoPolyedre;
import processing.core.*;

public class Cube extends PseudoPolyedre {
	
	public final PVector size;
	protected final float[] dim;
	public final Plane[] faces;

	/** Create a cube with arretes of lenght dim. */
	public Cube(PVector location, Quaternion rotation, PVector size) {
	    super(location, rotation, size.mag()/2);
	    this.size = size;
	    this.dim = mult(size, 0.5f).array();
		this.faces = getFaces(size);
	    setName("Cube");
	}

	public void display() {
		pushLocal();
		color.fill();
		app.box(size.x, size.y, size.z);
		popLocal();
	}

	public void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0) {
			float fact = mass/12;
			super.inertiaMom = new PVector(
					fact*(sq(size.y) + sq(size.z)), 
					fact*(sq(size.x) + sq(size.z)), 
					fact*(sq(size.x) + sq(size.y)));
			super.inverseInertiaMom = new PVector(
					1/inertiaMom.x,
					1/inertiaMom.y,
					1/inertiaMom.z );
		}
	}
	
	public boolean isIn(PVector abs) {
		float[] loc = local(abs).array();
		for (int i=0; i<3; i++)
			if (PApplet.abs(loc[i]) >= dim[0])
				return false;
		return true;
	}
	
	//retourne le point qui est le plus contre cette normale (par rapport au centre)
	public PVector pointContre(PVector normale) {
		PVector cNorm = PVector.mult(normale, -1);
		PVector proj = zero.copy();
		for (int i=0; i<3; i++)
			proj.add( PVector.mult( faces[i*2].normale().norm, dim[i] * sgn(faces[i*2].normale().norm.dot(cNorm))) );
		proj.add(locationAbs);
		return proj;
	}
	
	public float projetteSur(PVector normale) {
		float proj = 0;
		for (int i=0; i<3; i++) {
			proj += dim[i] * faces[i*2].normale().norm.dot(normale);
		}
		return proj;
	}
	
	public Plane[] plansSeparationFor(PVector colliderLocation) {
		PVector rel = PVector.sub(colliderLocation, locationAbs);
		Plane[] ret = new Plane[3];
		for (int i=0; i<3; i++) {
			if (rel.dot(faces[i*2].normale().norm) > 0)
				ret[i] = faces[i*2];
			else
				ret[i] = faces[i*2+1];
		}
		return ret;
	}

	public Line collisionLineFor(PVector p) {
		float bestSqDist = Float.MAX_VALUE;
		Line bestNormale = null;
		for (Plane plane : faces) {
			Line normale = plane.collisionLineFor(p);
			float distSq = distSq(normale.base, p);
			if (distSq < bestSqDist) {
				bestSqDist = distSq;
				bestNormale = normale;
			}
		}
		assert(bestNormale != null);
		return bestNormale;
	}
	
	public Projection projetteSur(Line ligne) {
	  float mid = ligne.projectionFactor(locationAbs);
	  float proj = PApplet.abs(projetteSur(ligne.norm));
	  return new Projection(mid-proj, mid+proj);
	}
	
	public PVector projette(PVector point) {
		float bestSqDist = Float.MAX_VALUE;
		PVector bestProj = null;
		for (Plane p : faces) {
			PVector proj = p.projette(point);
			float distSq = distSq(proj, point);
			if (distSq < bestSqDist) {
				bestSqDist = distSq;
				bestProj = proj;
			}
		}
		assert(bestProj != null);
		return bestProj;
	}
	
	//update les coordonnées absolue. (à chaque transform change du parent)
	public boolean updateAbs() {
		boolean sUpdated = super.updateAbs();
		if (sUpdated) {
			//1. update les plans
			pushLocal();
			for (Plane p : faces)
				p.updateAbs();
			popLocal();
		  	//2. les sommets
		  	//super.sommets = absolute(natSommets);
		  	//3. les axes
		  	/*axes = new PVector[] {
					absolute(left, zero, rotation),
					absolute(up, zero, rotation),
					absolute(front, zero, rotation) };*/
			return true;
		} else
			return false;
	}
	
	//-- static:
	
	private static Plane[] getFaces(PVector size) {
	    Plane[] faces =  new Plane[6];
	    PVector[] facesLoc = getFacesLoc(size);
	    Quaternion[] facesRot = getFacesRot();
	    PVector[] facesSize = getFacesSize(size);
	    for (int i=0; i<6; i++)
	      faces[i] = new Plane(facesLoc[i], facesRot[i], -1, facesSize[i]);
	    return faces;
	}

	private static PVector[] getFacesLoc(PVector size) {
	    return new PVector[] {
	    	new PVector(size.x/2, 0, 0), new PVector(-size.x/2, 0, 0), 	//gauche,  droite  (x)
	    	new PVector(0, size.y/2, 0), new PVector(0, -size.y/2, 0), 	//dessus, dessous  (y)
	  	    new PVector(0, 0, size.z/2), new PVector(0, 0, -size.z/2)};	//devant, derriere (z)
	}
	
	private static Quaternion[] getFacesRot() {
		return new Quaternion[] {
				Quaternion.fromDirection(left), Quaternion.fromDirection(right),
				Quaternion.fromDirection(up), Quaternion.fromDirection(down),
				Quaternion.fromDirection(front), Quaternion.fromDirection(behind),
				};
	    /*
	    float hpi = PApplet.HALF_PI;
		float pi = PApplet.PI;
		return new PVector[] {
	    	new PVector(0, 0, -hpi), new PVector(0, 0, hpi), 	//gauche,  droite  (x)
	    	new PVector(0, 0, 0), new PVector(0, 0, pi), 		//dessus, dessous  (y)
	  	    new PVector(hpi, 0, 0), new PVector(-hpi, 0, 0)};	//devant, derriere (z)
		*/
		
	}
	
	private static PVector[] getFacesSize(PVector size) {
		return new PVector[] {
	    	new PVector(size.y, 0, size.z), new PVector(size.y, 0, size.z), 	//gauche,  droite  (x)
	    	new PVector(size.x, 0, size.z), new PVector(size.x, 0, size.z), 	//dessus, dessous  (y)
	  	    new PVector(size.x, 0, size.y), new PVector(size.x, 0, size.y)};	//devant, derriere (z)
	}
}
