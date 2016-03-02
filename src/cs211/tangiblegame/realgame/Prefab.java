package cs211.tangiblegame.realgame;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.*;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.physic.Collider;
import processing.core.*;


/** Help class to add object in the scene */
public class Prefab extends ProMaster {
	/**
	 *	add a new object to the physic from the name. supported names:
	 *	box, ball, floor, starship, objectif.
	 */
	public static Body add(String name, PVector location) {
		Collider col;
		boolean pese = true;
		if (name.equals("box"))
			col = new Cube(location, identity, 1, vec(20,20,20));
		else if (name.equals("ball"))
			col = new Sphere(location, 1, 10);
		else if (name.equals("floor")) {
			col = new Plane(location, identity).withName("Floor");
			pese = false;
		} else if (name.equals("objectif")) {
			col = new Armement.Objectif(location);
			pese = false;
		} else if (name.equals("starship")) {
			col = new Starship(location);
			pese = false;
		} else {
			System.err.println("\""+name+"\" unknown, ignoring.");
			return null;
		}
		if (pese)
			col.addApplyForces(() -> col.pese());
		game.physic.colliders.add( col );
		return col;
	}
	
	/**
	 *	add a new object to the physic from the name. supported names:
	 *	box, ball, floor, starship, objectif.
	 */
	public static Body add(String name, PVector location, Quaternion rotation) {
		Body b = add(name, location);
		if (b == null)
			return null;
		else {
			b.rotation.set(rotation);
			return b;
		}
	}
}