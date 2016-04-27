package brabra.game.physic.geo;

import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.PseudoPolyedre;
import brabra.game.physic.geo.Line.Projection;
import brabra.game.scene.Object;

public class Box extends PseudoPolyedre {
	
	/** Total size (local). */
	public final Vector size = new Vector();
	/** Size / 2. */
	private Vector dim;
	private Plane[] faces;
	/** Vertices relative to the object. */
	private Vector[] verticesRel;
	/** Edges relative to the object. */
	private Line[] edgesRel;

	/** Create a cube with arretes of lenght dim. */
	public Box(Vector location, Quaternion rotation, Vector size) {
	    super(location, rotation);
	    super.setName("Cube");
	    setSize(size);
	}

	public void copy(Object o) {
		super.copy(o);
		Box ob = this.as(Box.class);
		if (ob != null) {
			setSize(size);
		}
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
		this.size.set(size);
	    this.dim = size.multBy(0.5f);
		
	    // --- Physic things:
	    this.verticesRel = new Vector[] {
				new Vector(dim.x, dim.y, dim.z), new Vector(-dim.x, -dim.y, -dim.z), 	//+++
				new Vector(dim.x, dim.y, -dim.z), new Vector(-dim.x, -dim.y, dim.z), 	//++-
				new Vector(dim.x, -dim.y, dim.z), new Vector(-dim.x, dim.y, -dim.z),	//+-+
				new Vector(dim.x, -dim.y, -dim.z), new Vector(-dim.x, dim.y, dim.z)};	//+--
	
		this.edgesRel = new Line[] {
				// -x -> x
				new Line(verticesRel[1], verticesRel[6], true), new Line(verticesRel[3], verticesRel[4], true),
				new Line(verticesRel[5], verticesRel[2], true), new Line(verticesRel[7], verticesRel[0], true),
				// -y -> y
				new Line(verticesRel[1], verticesRel[5], true), new Line(verticesRel[3], verticesRel[7], true),
				new Line(verticesRel[4], verticesRel[0], true), new Line(verticesRel[6], verticesRel[2], true),
				// -z -> z
				new Line(verticesRel[1], verticesRel[3], true), new Line(verticesRel[2], verticesRel[0], true),
				new Line(verticesRel[5], verticesRel[7], true), new Line(verticesRel[6], verticesRel[4], true)};
		
	    super.setRadiusEnveloppe(dim.mag());
	    

		// --- Faces
		this.faces =  new Plane[6];
	    Vector[] facesLoc = new Vector[] {
		    	new Vector(size.x/2, 0, 0), new Vector(-size.x/2, 0, 0), 	//gauche,  droite  (x)
		    	new Vector(0, size.y/2, 0), new Vector(0, -size.y/2, 0), 	//dessus, dessous  (y)
		  	    new Vector(0, 0, size.z/2), new Vector(0, 0, -size.z/2)};	//devant, derriere (z)
	    Quaternion[] facesRot = new Quaternion[] {
				Quaternion.fromDirection(left, up), Quaternion.fromDirection(right, up),
				Quaternion.fromDirection(up, up), Quaternion.fromDirection(down, up),
				Quaternion.fromDirection(front, up), Quaternion.fromDirection(behind, up)};
	    Vector[] facesSize = new Vector[] {
		    	new Vector(size.y, 0, size.z), new Vector(size.y, 0, size.z), 	//gauche,  droite  (x)
		    	new Vector(size.x, 0, size.z), new Vector(size.x, 0, size.z), 	//dessus, dessous  (y)
		  	    new Vector(size.x, 0, size.y), new Vector(size.x, 0, size.y)};	//devant, derriere (z)
	    for (int i=0; i<6; i++) {
	    	faces[i] = new Plane(facesLoc[i], facesRot[i], facesSize[i]);
	    }
	    for (Plane f : faces)
	    	f.setParent(this, null);
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

	public void validate(Attributes atts) {
		super.validate(atts);
		
		final String size = atts.getValue("size");
		if (size != null)
			setSize(vec(size));
	}
	
	public void updateAbs() {
		super.updateAbs();
		// for Cube
		for (Plane p : faces)
			p.updateAbs();
		// for polyhedron
		super.setAbs(absolute(verticesRel), absolute(edgesRel));
	}
	
	// --- from PseudoPolyedre ---
	
	public boolean isIn(Vector abs) {
		float[] loc = transform.relative(abs).array();
		for (int i=0; i<3; i++)
			if (abs(loc[i]) >= dim.array()[0])
				return false;
		return true;
	}
	
	public Vector pointContre(Vector normaleAbs) {
		Vector cNorm = normaleAbs.multBy(-1);
		Vector proj = zero.copy();
		for (int i=0; i<3; i++)
			proj.add( Vector.mult( faces[i*2].normale().norm, dim.array()[i] * sgn(faces[i*2].normale().norm.dot(cNorm))) );
		proj.add(location());
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
		Vector rel = colliderLocation.minus(location());
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
	  float mid = ligne.projectionFactor(location());
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
}
