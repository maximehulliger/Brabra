package brabra.game.scene;

import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import brabra.game.RealGame;
import brabra.game.physic.Body;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Box;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Sphere;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.fun.Starship;
import brabra.game.scene.weapons.MissileLauncher;
import brabra.game.scene.weapons.Target;
import brabra.game.scene.weapons.Weaponry;

/** 
 * Object representing the scene (model). 
 * Will pass to observer the object changed, argObjectAdded or argObjectRemoved  */
public class Scene extends Observable {

	public static final java.lang.Object argObjectAdded = "arg object added";
	public static final java.lang.Object argObjectRemoved = "arg object removed";
	public final ConcurrentLinkedDeque<Object> objects = new ConcurrentLinkedDeque<Object>();
	public final ConcurrentLinkedDeque<Collider> colliders = new ConcurrentLinkedDeque<Collider>();
	
	/** To let the objects let known that they have been changed. */
	protected final Set<Object> changedObjects = new HashSet<>();
	
	private final ConcurrentLinkedDeque<Object> toRemove = new ConcurrentLinkedDeque<Object>();
	private final ConcurrentLinkedDeque<Object> toAdd = new ConcurrentLinkedDeque<Object>();
	private final RealGame game;
	
	public Scene(RealGame game) {
		this.game = game;
	}

	// --- getters ---
	
	public List<Collider> activeColliders() {
		return colliders.stream().filter(c -> !c.ghost()).collect(Collectors.toList());
	}
	
	// --- modifiers ---

	public void forEachObjects(Consumer<Object> f) {
		objects.forEach(f);
	}

	/** Add an object to the scene (on next update). */
	public Object add(Object o) {
		toAdd.add(o);
		return o;
	}

	/** Add an object immediately into the scene. return the object. */
	public Object addNow(Object o) {
		if (!objects.contains(o)) {
			objects.add(o);
			o.scene = this;
			if (o instanceof Collider)
				colliders.add((Collider)o);
			setChanged();
			notifyObservers(argObjectAdded);
		}
		return o;
	}
	
	/** Remove an object from the scene (on next update). return the object. */
	public Object remove(Object o) {
		toRemove.add(o);
		return o;
	}
	
	/** Remove all object from the scene. */
	public void reset() {
		toRemove.addAll(objects);
	}
	
	// --- Prefab help method ---
	
	/**
	 *  Help method to add a new object into the scene.
	 *	Supported names: <p>
	 *	camera, box, ball, floor, target, starship, weaponry, weapon.
	 */
	public Object addPrefab(String name, Vector location, Quaternion rotation) {
		Object obj;
		Body body;
		if (name.equals("object"))
			obj = new Object(location, rotation);
		else if (name.equals("movable"))
			obj = new Movable(location, rotation);
		else if (name.equals("camera")) {
			return game.camera; // already in scene
		} else if (name.equals("box")) {
			obj = body = new Box(location, rotation, new Vector(20,20,20));
			body.setMass(1);
			body.addOnUpdate(() -> body.pese());
		} else if (name.equals("ball")) {
			obj = body = new Sphere(location, 10);
			body.setMass(1);
			body.addOnUpdate(() -> body.pese());
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
		for (Object o : changedObjects) {
			setChanged();
			notifyObservers(o);
		}
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
			setChanged();
			notifyObservers(argObjectRemoved);
		}
		if (toAdd.size() > 0) {
			toAdd.forEach(o->addNow(o));
			toAdd.clear();
		}
	}
}
