package brabra.game.physic.geo;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DPlane;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import brabra.Debug;
import brabra.game.physic.Body;
import processing.core.PApplet;

/** A plane characterized by a normal++ and 2 direction vectors. Default norm is up (y+). Can be finite or infinite. */
public class Plane extends Body {
	
	private DPlane geom;

	// absolute variables:
	private Line normale;
	
	/** Create an infinite plan (with infinite mass). */
	public Plane() {
		super.setName("Plane");
	}

	// --- Getters --- 

	/** Return the normal of the plane (starting in the plane). */
	public Line normale() {
		updateAbs();
		return normale;
	}

	// --- Setters ---

	public void setMass(float mass) {
		if(mass != 0) {
			Debug.err("A plane should only have infinite mass.");
			mass = 0;
		}
		super.setMass(mass);
	}

	public void setOdeMass(DBody body) {
		body.setKinematic();
	}
	
	public void setOdeTransform() {
		super.setOdeTransform();
		Vector norm = normale().norm;
		geom.setParams(norm.x, norm.y, norm.z, norm.x*position.x+norm.y*position.y+norm.z*position.z);
	}
	
	// --- life cycle ---

	public void display() {
		pushLocal();
		displayInteractionMaybe();
		if (!displayColliderMaybe()) {
			color.fill();
			displayShape();
		}
		popLocal();
	}

	protected void updateAbs() {
		// for plane
		Vector[] vertices = absolute(getNatCo());
		Vector norm = vertices[2].minus(vertices[0]).cross(vertices[1].minus(vertices[0])).normalized();
		normale = new Line(position, position.plus(norm), true);
	}

	// --- Collider & physic implementation ---

	public void displayShape() {
		float x = far;
		float z = far;
		app.beginShape(PApplet.QUADS);
	    app.vertex(-x,0,-z);
	    app.vertex(x,0,-z);
	    app.vertex(x,0,z);
	    app.vertex(-x,0,z);
		app.endShape();
	}

	// --- private ---

	private static Vector[] getNatCo() {
		return new Vector[] { 
				new Vector(-1, 0, -1), 
				new Vector(1, 0, -1),
				new Vector(-1, 0, 1), 
				new Vector(1, 0, 1)};
	}

	@Override
	public void addToScene(DWorld world, DSpace space) {
		DBody body = OdeHelper.createBody (world);
		geom = OdeHelper.createPlane(space, -1, 1, 0, 0);;
		geom.setBody(body);
		super.setBody(body);
	}
}