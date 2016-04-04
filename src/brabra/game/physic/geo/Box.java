package brabra.game.physic.geo;

import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.PseudoPolyedre;
import brabra.game.physic.geo.Line.Projection;
import brabra.game.scene.Object;

public class Box extends PseudoPolyedre {
	
	/** Total size (local). */
	private Vector size;
	/** Size / 2. */
	private Vector dim;
	private Plane[] faces;
	/** Vertices relative to the object. */
	private Vector[] verticesRel;
	/** Edges relative to the object. */
	private Line[] edgesRel;

	/** Create a cube with arretes of lenght dim. */
	public Box(Vector location, Quaternion rotation, Vector size) {
	    super(location, rotation, size.mag()/2);
	    super.setName("Cube");
	    setSize(size);
	}
	
	// --- Getters ---

	/** Return the size of the box (edges length). */
	public Vector size() {
		return size;
	}

	/** Return half of the size of the box. */
	public Vector dim() {
		return dim;
	}

	/** Return the 6 faces of this box. */
	public Plane[] faces() {
		return faces;
	}
	
	// --- Setters ---
	
	public void setSize(Vector size) {
		this.size = size;
	    this.dim = size.multBy(0.5f);
		this.faces = getFaces(size, this);
		this.verticesRel = verticesRel(dim);
	    this.edgesRel = edgesRel(verticesRel);
	}
	
	public void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0) {
			float fact = mass/12;
			super.inertiaMom = new Vector(
					fact*(sq(size.y) + sq(size.z)), 
					fact*(sq(size.x) + sq(size.z)), 
					fact*(sq(size.x) + sq(size.y)));
			super.inverseInertiaMom = new Vector(
					1/inertiaMom.x,
					1/inertiaMom.y,
					1/inertiaMom.z );
		}
	}
	
	// --- life cycle ---

	public void display() {
		pushLocal();
		if (!displayColliderMaybe()) {
			color.fill();
			displayShape();
		}
		popLocal();
	}
	
	public void displayShape() {
		app.box(size.x, size.y, size.z);
	}

	// --- life cycle ---

	public boolean validate(Attributes atts) {
		if (super.validate(atts)) {
			final String size = atts.getValue("size");
			if (size != null)
				setSize(vec(size));
			return true;
		} else
			return false;
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
	
	public boolean isIn(Vector abs) {
		float[] loc = relative(abs).array();
		for (int i=0; i<3; i++)
			if (abs(loc[i]) >= dim.array()[0])
				return false;
		return true;
	}
	
	public Vector pointContre(Vector normale) {
		Vector cNorm = normale.multBy(-1);
		Vector proj = zero.copy();
		for (int i=0; i<3; i++)
			proj.add( Vector.mult( faces[i*2].normale().norm, dim.array()[i] * sgn(faces[i*2].normale().norm.dot(cNorm))) );
		proj.add(locationAbs);
		return proj;
	}
	
	public float projetteSur(Vector normale) {
		float proj = 0;
		for (int i=0; i<3; i++) {
			proj += dim.array()[i] * faces[i*2].normale().norm.dot(normale);
		}
		return proj;
	}
	
	public Plane[] plansSeparationFor(Vector colliderLocation) {
		Vector rel = colliderLocation.minus(locationAbs);
		Plane[] ret = new Plane[3];
		for (int i=0; i<3; i++) {
			ret[i] = rel.dot(faces[i*2].normale().norm) > 0
				? faces[i*2] : faces[i*2+1];
		}
		return ret;
	}

	public Line collisionLineFor(Vector p) {
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
	  float proj = abs(projetteSur(ligne.norm));
	  return new Projection(mid-proj, mid+proj);
	}
	
	public Vector projette(Vector point) {
		updateAbs();
		float bestSqDist = Float.MAX_VALUE;
		Vector bestProj = null;
		for (Plane p : faces) {
			Vector proj = p.projette(point);
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
				Vector proj = p.projette(point);
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
	
	private static Vector[] verticesRel(Vector dim) {
		return new Vector[] {
				new Vector(dim.x, dim.y, dim.z), new Vector(-dim.x, -dim.y, -dim.z), 	//+++
				new Vector(dim.x, dim.y, -dim.z), new Vector(-dim.x, -dim.y, dim.z), 	//++-
				new Vector(dim.x, -dim.y, dim.z), new Vector(-dim.x, dim.y, -dim.z),	//+-+
				new Vector(dim.x, -dim.y, -dim.z), new Vector(-dim.x, dim.y, dim.z)};	//+--
	}
	
	private static Line[] edgesRel(Vector[] verRel) {
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
	
	private static Plane[] getFaces(Vector size, Object forMe) {
	    Plane[] faces =  new Plane[6];
	    Vector[] facesLoc = new Vector[] {
		    	new Vector(size.x/2, 0, 0), new Vector(-size.x/2, 0, 0), 	//gauche,  droite  (x)
		    	new Vector(0, size.y/2, 0), new Vector(0, -size.y/2, 0), 	//dessus, dessous  (y)
		  	    new Vector(0, 0, size.z/2), new Vector(0, 0, -size.z/2)};	//devant, derriere (z)
	    Quaternion[] facesRot = new Quaternion[] {
				Quaternion.fromDirection(left), Quaternion.fromDirection(right),
				Quaternion.fromDirection(up), Quaternion.fromDirection(down),
				Quaternion.fromDirection(front), Quaternion.fromDirection(behind)};
	    Vector[] facesSize = new Vector[] {
		    	new Vector(size.y, 0, size.z), new Vector(size.y, 0, size.z), 	//gauche,  droite  (x)
		    	new Vector(size.x, 0, size.z), new Vector(size.x, 0, size.z), 	//dessus, dessous  (y)
		  	    new Vector(size.x, 0, size.y), new Vector(size.x, 0, size.y)};	//devant, derriere (z)
	    for (int i=0; i<6; i++) {
	    	faces[i] = new Plane(facesLoc[i], facesRot[i], facesSize[i]);
	    	faces[i].setParent(forMe);
	    }
	    return faces;
	}
}
