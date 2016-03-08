package cs211.tangiblegame.geo;

import cs211.tangiblegame.geo.Line.Projection;
import cs211.tangiblegame.physic.Collider;
import cs211.tangiblegame.physic.PseudoPolyedre;
import processing.core.*;

/** A plane characterized by 3 points. Default norm is up. Can be finite or infinite. */
public class Plane extends PseudoPolyedre {
	private final PVector size; //x et z
	private final PVector[] natCo; 	//native relative coordonates (4 points)
	private final boolean finite;

	// update quand transformChanged
	public Line normale;
	private Line v1; //sur x
	private Line v2; //sur z

	/** Create a plan of size size2d (x,z). */
	public Plane(PVector loc, Quaternion rot, float mass, PVector size2d) {
		super( loc, rot, size2d.mag()/2 );
		this.size = size2d;
		this.natCo = getNatCo(size2d);
		this.finite = true;
		updateAbs();
		setMass(mass);
	}

	/** Create an infinite plan. */
	public Plane(PVector loc, Quaternion rot) {
		super( loc, rot, Float.MAX_VALUE );
		this.size = null;
		this.natCo = getNatCo(new PVector(1, 0, 1));
		this.finite = false;
		updateAbs();
		setMass(-1);
	}

	/** Retourne un point appartenant au plan. le plan doit �tre fini. */
	public PVector randomPoint() {
		if (!finite)
			throw new IllegalArgumentException(
					"are you sure that do you want a random point in an infinite plane ?");
		float a = random.nextFloat() * size.x;
		float b = random.nextFloat() * size.z;
		PVector ret = v1.base.copy();
		ret.add( PVector.mult(v1.norm, a) );
		ret.add( PVector.mult(v2.norm, b) );
		return ret;
	}

	//------ surcharge:

	public void setMass(float mass) {
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
			color.fill();
			displayPlane(v2.vectorMag, v1.vectorMag);
		} else {
			color.fill();
			displayPlane(far*2, far*2);
		}
		popLocal();
	}
	
	private void displayPlane(float magX, float magZ) {
		float x = magX/2;
		float z = magZ/2;
		app.beginShape(PApplet.QUADS);
		{
		    app.vertex(-x,0,-z);
		    app.vertex(x,0,-z);
		    app.vertex(x,0,z);
		    app.vertex(-x,0,z);
		}
		app.endShape();
	}

	public Line collisionLineFor(PVector p) {
		if (isFacing(p))
			return normale;
		else
			return new Line(projette(p), p, false);
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
		PVector proj = PVector.mult( v1.norm, size.x/2 * -sgn(v1.norm.dot(normale)) );
		proj.add( PVector.mult( v2.norm, size.z/2 * -sgn(v2.norm.dot(normale))) );
		proj.add(locationAbs);
		return proj;
	}

	public Projection projetteSur(Line ligne) {
		if (ligne == normale)
			return new Projection(0, 0);
		else if (finite)
			return ligne.projette( sommets );
		else
			return new Projection(Float.MIN_VALUE, Float.MAX_VALUE);
		//throw new IllegalArgumentException("infinite plane projected !!");
	}

	public Plane[] plansSeparationFor(PVector colliderLocation) {
		return new Plane[] {this};
	}

	//----- private

	//update les coordonn�es absolue. (à chaque transform change du parent)
	public void updateAbs() {
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
}