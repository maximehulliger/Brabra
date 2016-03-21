package brabra.game.scene;

import java.util.Arrays;
import java.util.List;

import brabra.ProMaster;
import brabra.game.physic.Body;
import brabra.game.physic.geo.*;
import brabra.game.scene.fun.Starship;
import brabra.game.scene.weapons.MissileLauncher;
import brabra.game.scene.weapons.Objectif;
import brabra.game.scene.weapons.Weaponry;
import processing.core.*;


/** Help class to add object in the scene */
public class Prefab extends ProMaster {
	/**
	 *	add a new object to the physic from the name. supported names:
	 *	box, ball, floor, starship, objectif.
	 */
	public static Body addBody(String name, PVector location, Quaternion rotation) {
		Body body;
		if (name.equals("box")) {
			body = new Cube(location, rotation, vec(20,20,20));
			body.setMass(1);
			body.addApplyForces(() -> body.pese());
		} else if (name.equals("ball")) {
			body = new Sphere(location, 10);
			body.setMass(1);
			body.addApplyForces(() -> body.pese());
		} else if (name.equals("floor"))
			body = new Plane(location, rotation).withName("Floor");
		else if (name.equals("objectif"))
			body = new Objectif(location, rotation);
		else if (name.equals("starship"))
			body = new Starship(location, rotation);
		else {
			System.err.println("\""+name+"\" unknown, ignoring.");
			return null;
		}
		game.scene.addNow( body );
		return body;
	}
	
	public static List<String> supportedObjects = Arrays.asList(new String[]{"weaponry", "missile_launcher"});
	
	/**
	 *	add a new object to the scene from the name. supported names:
	 *	weaponry, missile_launcher.
	 */
	public static Object add(String name, PVector location, Quaternion rotation) {
		Object obj;
		if (name.equals("weaponry"))
			obj = new Weaponry(location, rotation);
		else if (name.equals("missile_launcher"))
			obj = new MissileLauncher(location, rotation);
		else
			throw new IllegalArgumentException("you should check Prefab.supportedObjects..");
		game.scene.addNow( obj );
		return obj;
	}
}