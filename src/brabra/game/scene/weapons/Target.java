package brabra.game.scene.weapons;

import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Sphere;
import processing.core.PVector;

/** A sphere to destroy. */
public class Target extends Sphere {
	
	public Target(PVector location, Quaternion rotation) {
		super(location, rotation, 50);
		setMass(30);
		setName("Target");
	}
	
	public String toString() {
		return super.toString()+" ("+life()+")";
	}

	public void display() {
		app.fill(255, 255, 0);
		super.display();
	}

	public void addForces() {
		brake(0.07f);
	}
	
	public void onDeath() {
		game.debug.msg(2, presentation()+" destroyed");
	}
}