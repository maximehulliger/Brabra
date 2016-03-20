package brabra.game.scene;

import brabra.ProMaster;
import brabra.game.physic.Body;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.*;
import brabra.game.scene.fun.Starship;
import brabra.game.scene.weapons.Weaponry;
import processing.core.*;


/** Help class to add object in the scene */
public class Prefab extends ProMaster {
	/**
	 *	add a new object to the physic from the name. supported names:
	 *	box, ball, floor, starship, objectif.
	 */
	public static Body add(String name, PVector location, Quaternion rotation) {
		Collider col;
		if (name.equals("box")) {
			col = new Cube(location, rotation, vec(20,20,20));
			col.setMass(1);
			col.addApplyForces(() -> col.pese());
		} else if (name.equals("ball")) {
			col = new Sphere(location, 10);
			col.setMass(1);
			col.addApplyForces(() -> col.pese());
		} else if (name.equals("floor"))
			col = new Plane(location, rotation).withName("Floor");
		else if (name.equals("objectif"))
			col = new Weaponry.Objectif(location, rotation);
		else if (name.equals("starship"))
			col = new Starship(location, rotation);
		else {
			System.err.println("\""+name+"\" unknown, ignoring.");
			return null;
		}
		game.physic.addNow( col );
		return col;
	}
}