package brabra.game.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import brabra.ProMaster;
import brabra.game.physic.Body;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Cube;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Sphere;
import brabra.game.scene.fun.Starship;
import brabra.game.scene.weapons.MissileLauncher;
import brabra.game.scene.weapons.Target;
import brabra.game.scene.weapons.Weaponry;
import processing.core.PVector;

public class Scene extends ProMaster {

	private List<Object> objects = new ArrayList<Object>();
	private List<Collider> colliders = new ArrayList<Collider>();
	private List<Object> toRemove = new ArrayList<Object>();
	private List<Object> toAdd = new ArrayList<Object>();

	// --- getters ---

	/** Return all the objects in the scene. */
	public List<Object> objects() {
		return objects;
	}
	
	/** Return all the colliders in the scene. */
	public List<Collider> colliders() {
		return colliders;
	}
	
	public List<Collider> activeColliders() {
		return colliders.stream().filter(c -> !c.ghost()).collect(Collectors.toList());
	}
	
	// --- modifiers ---

	/** Add an object to the scene (on next update). */
	public Object add(Object o) {
		toAdd.add(o);
		return o;
	}

	/** Add an object immediately into the scene. return the object. */
	public Object addNow(Object o) {
		objects.add(o);
		if (o instanceof Collider)
			colliders.add((Collider)o);
		return o;
	}
	
	/** Remove an object from the scene (on next update). return the object. */
	public Object remove(Object o) {
		toRemove.add(o);
		return o;
	}
	
	// --- Prefab help method ---
	
	/**
	 *  Help method to add a new object into the scene.
	 *	Supported names: <p>
	 *	camera, box, ball, floor, target, starship, weaponry, weapon.
	 */
	public Object addPrefab(String name, PVector location, Quaternion rotation) {
		Object obj;
		Body body;
		if (name.equals("object")) {
			obj = new Object(location, rotation);
		} else if (name.equals("camera")) {
			return game.camera; // already in scene
		} else if (name.equals("box")) {
			obj = body = new Cube(location, rotation, vec(20,20,20));
			body.setMass(1);
			body.addApplyForces(() -> body.pese());
		} else if (name.equals("ball")) {
			obj = body = new Sphere(location, 10);
			body.setMass(1);
			body.addApplyForces(() -> body.pese());
		} else if (name.equals("floor"))
			obj = new Plane(location, rotation).withName("Floor");
		else if (name.equals("targer"))
			obj = new Target(location, rotation);
		else if (name.equals("starship"))
			obj = new Starship(location, rotation);
		else if (name.equals("weaponry"))
			obj = new Weaponry(location, rotation);
		else if (name.equals("missile_launcher"))
			obj = new MissileLauncher(location, rotation);
		else {
			System.err.println("\""+name+"\" unknown, ignoring.");
			return null;
		}
		return addNow(obj);
	}
	
	// --- on all methods ---
	
	/** To call before all update methods. */
	public void beforeUpdateAll() {
		game.debug.setCurrentWork("objects pre-update");
		updateObjectLists();
		for (Object o : objects) 
			o.beforeUpdate();
	}
	
	/** Update the colliders and effects (parents first (automatic)). */
	public void updateAll() {
		game.debug.setCurrentWork("objects update");
		for (Object o : objects)
			o.update();
		updateObjectLists();
	}

	/** Display all colliders and effects in the scene. */
	public void displayAll() {
		game.debug.setCurrentWork("display objects");
		for(Object o : objects)
			o.display();
	}
	
	// --- private stuff ---

	/** Effectively remove / add objects to the lists. */
	private void updateObjectLists() {
		if (toRemove.size() > 0) {
			objects.removeAll(toRemove);
			colliders.removeAll(toRemove);
			toRemove.forEach(o -> o.onDelete());
			toRemove.clear();
		}
		if (toAdd.size() > 0) {
			toAdd.forEach(o->addNow(o));
			toAdd.clear();
		}
	}
}
