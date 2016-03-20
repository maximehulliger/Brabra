package brabra.game.physic.geo;

import brabra.game.physic.PseudoPolyedre;
import brabra.game.physic.geo.Line.Projection;
import brabra.game.scene.Object;
import processing.core.*;

public class Cube extends PseudoPolyedre {
	
	/** Total size (local). */
	public final PVector size;
	/** Size / 2. */
	public final PVector dim;
	public final Plane[] faces;
	/** Vertices relative to the object. */
	private final PVector[] verticesRel;
	/** Edges relative to the object. */
	private final Line[] edgesRel;

	/** Create a cube with arretes of lenght dim. */
	public Cube(PVector location, Quaternion rotation, PVector size) {
	    super(location, rotation, size.mag()/2);
	    super.setName("Cube");
	    this.size = size;
	    this.dim = mult(size, 0.5f);
		this.faces = getFaces(size, this);
		this.verticesRel = verticesRel(dim);
	    this.edgesRel = edgesRel(verticesRel);
	}

	public void display() {
		pushLocal();
		if (!displayColliderMaybe()) {
			color.fill();
			displayCollider();
		}
		popLocal();
	}
	
	public void displayCollider() {
		app.box(size.x, size.y, size.z);
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

	public boolean updateAbs() {
		if (super.updateAbs()) {
			// for Cube
			for (Plane p : faces)
				p.updateAbs();
			// for polyhedron
		  	super.setAbs(absolute(verticesRel), absolute(edgesRel));
			return true;
		} else
			return false;
	}
	
	// --- from PseudoPolyedre ---
	
	public boolean isIn(PVector abs) {
		float[] loc = local(abs).array();
		for (int i=0; i<3; i++)
			if (PApplet.abs(loc[i]) >= dim.array()[0])
				return false;
		return true;
	}
	
	public PVector pointContre(PVector normale) {
		PVector cNorm = PVector.mult(normale, -1);
		PVector proj = zero.copy();
		for (int i=0; i<3; i++)
			proj.add( PVector.mult( faces[i*2].normale().norm, dim.array()[i] * sgn(faces[i*2].normale().norm.dot(cNorm))) );
		proj.add(locationAbs);
		return proj;
	}
	
	public float projetteSur(PVector normale) {
		float proj = 0;
		for (int i=0; i<3; i++) {
			proj += dim.array()[i] * faces[i*2].normale().norm.dot(normale);
		}
		return proj;
	}
	
	public Plane[] plansSeparationFor(PVector colliderLocation) {
		PVector rel = PVector.sub(colliderLocation, locationAbs);
		Plane[] ret = new Plane[3];
		for (int i=0; i<3; i++) {
			ret[i] = rel.dot(faces[i*2].normale().norm) > 0
				? faces[i*2] : faces[i*2+1];
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
		updateAbs();
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
		//assert(bestProj != null);
		if (bestProj == null) {
			bestSqDist = Float.MAX_VALUE;
			bestProj = null;
			for (Plane p : faces) {
				PVector proj = p.projette(point);
				float distSq = distSq(proj, point);
				if (distSq < bestSqDist) {
					bestSqDist = distSq;
					bestProj = proj;
				}
			}
		}
		return bestProj;
	}
	
	// --- private stuff ---
	
	private static PVector[] verticesRel(PVector dim) {
		return new PVector[] {
				new PVector(dim.x, dim.y, dim.z), new PVector(-dim.x, -dim.y, -dim.z), 	//+++
				new PVector(dim.x, dim.y, -dim.z), new PVector(-dim.x, -dim.y, dim.z), 	//++-
				new PVector(dim.x, -dim.y, dim.z), new PVector(-dim.x, dim.y, -dim.z),	//+-+
				new PVector(dim.x, -dim.y, -dim.z), new PVector(-dim.x, dim.y, dim.z)};	//+--
	}
	
	private static Line[] edgesRel(PVector[] verRel) {
		return new Line[] {
				// -x -> x
				new Line(verRel[1], verRel[6], true), new Line(verRel[3], verRel[4], true),
				new Line(verRel[5], verRel[2], true), new Line(verRel[7], verRel[0], true),
				// -y -> y
				new Line(verRel[1], verRel[5], true), new Line(verRel[3], verRel[7], true),
				new Line(verRel[4], verRel[0], true), new Line(verRel[6], verRel[2], true),
				// -z -> z
				new Line(verRel[1], verRel[3], true), new Line(verRel[2], verRel[0], true),
				new Line(verRel[5], verRel[7], true), new Line(verRel[6], verRel[4], true)};
	}
	
	private static Plane[] getFaces(PVector size, Object forMe) {
	    Plane[] faces =  new Plane[6];
	    PVector[] facesLoc = new PVector[] {
		    	new PVector(size.x/2, 0, 0), new PVector(-size.x/2, 0, 0), 	//gauche,  droite  (x)
		    	new PVector(0, size.y/2, 0), new PVector(0, -size.y/2, 0), 	//dessus, dessous  (y)
		  	    new PVector(0, 0, size.z/2), new PVector(0, 0, -size.z/2)};	//devant, derriere (z)
	    Quaternion[] facesRot = new Quaternion[] {
				Quaternion.fromDirection(left), Quaternion.fromDirection(right),
				Quaternion.fromDirection(up), Quaternion.fromDirection(down),
				Quaternion.fromDirection(front), Quaternion.fromDirection(behind)};
	    PVector[] facesSize = new PVector[] {
		    	new PVector(size.y, 0, size.z), new PVector(size.y, 0, size.z), 	//gauche,  droite  (x)
		    	new PVector(size.x, 0, size.z), new PVector(size.x, 0, size.z), 	//dessus, dessous  (y)
		  	    new PVector(size.x, 0, size.y), new PVector(size.x, 0, size.y)};	//devant, derriere (z)
	    for (int i=0; i<6; i++) {
	    	faces[i] = new Plane(facesLoc[i], facesRot[i], facesSize[i]);
	    	faces[i].setParent(forMe);
	    }
	    return faces;
	}
}
