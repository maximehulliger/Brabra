package brabra.game.scene.weapons;

import brabra.game.physic.geo.Sphere;
import brabra.game.scene.SceneLoader.Attributes;

/** A sphere to destroy. */
public class Target extends Sphere {

	public Target() {
		super(50);
		setMass(30);
		setName("Target");
		super.addOnUpdate(t -> t.brake(0.07f));
	}
	
	public void validate(Attributes atts) {
		super.validate(atts);
		setName("Target ("+life()+")");
	}

	public void display() {
		app.fill(255, 255, 0);
		super.display();
	}

	public void damage(int damage) {
		super.damage(damage);
		setName("Target ("+life()+")");
	}
	
	public void onDeath() {
		app.game.scene.remove(this);
	}
}