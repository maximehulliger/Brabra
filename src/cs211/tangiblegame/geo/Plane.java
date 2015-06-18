package cs211.tangiblegame.geo;

import cs211.tangiblegame.physic.Collider;
import cs211.tangiblegame.physic.PseudoPolyedre;
import processing.core.*;

/** un plan caracterisé par 3 points. peut être fini*/
public class Plane extends PseudoPolyedre {
	private final PVector size; //x et z
	private final PVector[] natCo; 	//native relative coordonates (4 points)
	private final boolean finite;
	
	// updaté quand transformChanged
	public Line normale;
	private Line v1; //sur x
	private Line v2; //sur z
	
	// crée un plan de taille size2d (x,z)
	public Plane(PVector loc, Quaternion rot, float mass, PVector size2d) {
		super( loc, rot, size2d.mag()/2 );
		this.size = size2d;
		this.natCo = getNatCo(size2d);
		this.finite = true;
		updateAbs();
		setMass(mass);
	}
	
	// crée un plan infini
	public Plane(PVector loc, Quaternion rot) {
		super( loc, rot, 0 );
		this.size = null;
		this.natCo = getNatCo(new PVector(1, 0, 1));
		this.finite = false;
		updateAbs();
		setMass(-1);
	}
	
	protected void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0) {
			float fact = mass/12;
			super.inertiaMom = new PVector(
					fact*sq(size.z), 
					fact*(sq(size.x) + sq(size.z)), 
					fact*sq(size.x) );
			super.inverseInertiaMom = new PVector(
					1/inertiaMom.x,
					1/inertiaMom.y,
					1/inertiaMom.z );
		}
	}
	
	//------ surcharge:

	public float projetteSur(PVector normale) {
		float proj = 0;
		proj += v1.norm.dot(normale) * size.x/2;
		proj += v2.norm.dot(normale) * size.z/2;
		return proj;
	}
	
	public void update() {
		super.update();
		if (transformChanged) {
			updateAbs();
		}
	}
	
	public void display() {
		pushLocal();
	    	if (finite) {
	    		app.stroke(255);
	    		app.fill(200, 150);
	      		app.box(v2.vectorMag, 1, v1.vectorMag);
	      	} else {
	      		app.stroke(0, 140, 0);
	    		app.fill(0, 100, 100, 1);
	      		app.box(1000, 1, 1000);
	      	}
	    popLocal();
	}

	public Line collisionLineFor(PVector p) {
		/*if (isFacing(p))
			return normale;
		else
			*/return new Line(projette(p), p, false);
	}
	
	public boolean isIn(PVector abs) {
		return isFacing(abs) && normale.projectionFactor(abs)<0;
	}

	public PVector projette(PVector point) {
		PVector proj1 = v1.projette(point);
		PVector proj2 = v2.projetteLocal(point);
		return PVector.add(proj1, proj2);
	}
	
	public boolean doCollideFast(Collider c) {
		return c.projetteSur(normale).comprend(0) && (!finite || super.doCollideFast(c));
	}
	
	//retourne le point qui est le plus contre cette normale (par rapport au centre)
	public PVector pointContre(PVector normale) {
		if (!finite)
			return farfarAway;
		PVector cNorm = PVector.mult(normale, -1);
		PVector proj = PVector.mult( v1.norm, size.x/2 * sgn(v1.norm.dot(cNorm)) );
		proj.add( PVector.mult( v2.norm, size.z/2 * sgn(v2.norm.dot(cNorm))) );
		proj.add(location);
		return proj;
	}
	
	public Line.Projection projetteSur(Line ligne) {
		if (ligne == normale)
			return ligne.new Projection(0, 0);
		else if (finite)
			return ligne.projette( sommets );
		else
			throw new IllegalArgumentException("infinite plane projected !!");
	}
	
	public Plane[] plansSeparationFor(PVector colliderLocation) {
		return new Plane[] {this};
	}
	
	//retourne un point appartenant au plan fini
	public PVector randomPoint() {
		if (!finite)
			throw new IllegalArgumentException("aïe !!");
		float a = random.nextFloat() * size.x;
		float b = random.nextFloat() * size.z;
		PVector ret = v1.base.get();
		ret.add( PVector.mult(v1.norm, a) );
		ret.add( PVector.mult(v2.norm, b) );
		return ret;
	}
	
	//----- private
	
	//update les coordonnées absolue. (à chaque transform change du parent)
	protected void updateAbs() {
		super.sommets = absolute(natCo);
		
		v1 = new Line(sommets[0], sommets[1], finite); //sur x
		v2 = new Line(sommets[0], sommets[2], finite); //sur z
	  
		PVector norm = v2.norm.cross(v1.norm);
		norm.normalize(); // ? plus besoin
		normale = new Line(sommets[0], PVector.add( sommets[0], norm ), false);
		
		super.arretes = new Line[] {
				v1, v2,
				new Line(sommets[1], sommets[3], true),
				new Line(sommets[2], sommets[3], true)};
	}
	
	private boolean isFacing(PVector p) {
		return !finite || (v1.isFacing(p) && v2.isFacing(p));
	}
	
	private static PVector[] getNatCo(PVector size2d) {
		return new PVector[] { 
				new PVector(-size2d.x/2, 0, -size2d.z/2), 
				new PVector(size2d.x/2, 0, -size2d.z/2),
				new PVector(-size2d.x/2, 0, size2d.z/2), 
				new PVector(size2d.x/2, 0, size2d.z/2)};
	}

	/*public boolean hasIntrudersOver(Line colLine) {
		// TODO Auto-generated method stub
		return false;
	}*/
}