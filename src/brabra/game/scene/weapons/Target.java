package brabra.game.scene.weapons;

import brabra.game.physic.geo.Sphere;

/** A sphere to destroy. */
public class Target extends Sphere {

	public Target() {
		super(50);
		setMass(30);
		setName("Target");
		super.addOnUpdate(t -> t.brake(0.07f));
	}

	public String toString() {
		return super.toString()+" ("+life()+")";
	}

	public void display() {
		app.fill(255, 255, 0);
		super.display();
	}

	public void addForces() {
		
	}
}