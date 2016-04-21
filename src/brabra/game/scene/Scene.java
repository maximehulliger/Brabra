package brabra.game.scene;

import java.util.List;
import java.util.Observable;
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
import brabra.gui.ToolWindow;

/** 
 * Object representing the scene (model). 
 * Will pass to observer argObjectAdded or argObjectRemoved  */
public class Scene extends Observable {

	// --- Observable ---
	
	public enum Change { ObjectAdded, ObjectRemoved }
	
	public static class Arg {
		public final Object object;
		public final Change change;
		public Arg(Object object, Change modif) {
			this.object = object;
			this.change = modif;
		}
	}
	
	// --- Scene ---
	
	public final ConcurrentLinkedDeque<Object> objects = new ConcurrentLinkedDeque<Object>();
	public final ConcurrentLinkedDeque<Collider> colliders = new ConcurrentLinkedDeque<Collider>();
	
	//private final ConcurrentLinkedDeque<Object> toRemove = new ConcurrentLinkedDeque<Object>();
	//private final ConcurrentLinkedDeque<Object> toAdd = new ConcurrentLinkedDeque<Object>();
	private final RealGame game;
	
	public Scene(RealGame game) {
		this.game = game;
	}

	// --- Getters ---
	
	public List<Collider> activeColliders() {
		return colliders.stream().filter(c -> !c.ghost()).collect(Collectors.toList());
	}
	
	//--- Modifiers ---

	public void forEachObjects(Consumer<Object> f) {
		objects.forEach(f);
	}

	/** Add an object to the scene (on next update). */
	public Object add(Object o) {
		addNow(o);
		//toAdd.add(o); TODO
		return o;
	}

	/** Add an object immediately into the scene. return the object. */
	public Object addNow(Object o) {
		if (!objects.contains(o)) {
			objects.add(o);
			o.scene = this;
			if (o instanceof Collider)
				colliders.add((Collider)o);
			notifyChange(o, Change.ObjectAdded);
		}
		return o;
	}
	
	/** Remove an object from the scene immediately. */
	public void removeNow(Object o) {
		objects.remove(o);
		colliders.remove(o);
		o.onDelete();
		notifyChange(o, Change.ObjectRemoved);
	}
	
	/** Remove an object from the scene (on next update). return the object. */
	public Object remove(Object o) {
		removeNow(o);
		//toRemove.add(o);
		return o;
	}
	
	/** Remove all object from the scene. */
	public void reset() {
		objects.forEach(o -> remove(o));
		//toRemove.addAll(objects);
	}
	
	// --- Prefab help method ---
	
	/**
	 *  Help method to get a new object (not in the scene).
	 *	Supported names: <p>
	 *	Object, Movable, Camera, Box, Ball, Floor, Target, Starship, Weaponry, missile_launcher.
	 */
	public Object getPrefab(String name, Vector location, Quaternion rotation) {
		Object obj;
		Body body;
		if (name.equals(Object.class.getSimpleName()))
			obj = new Object(location, rotation);
		else if (name.equals(Movable.class.getSimpleName()))
			obj = new Movable(location, rotation);
		else if (name.equals(Camera.class.getSimpleName())) {
			return game.camera; // already in scene
		} else if (name.equals(Box.class.getSimpleName())) {
			obj = body = new Box(location, rotation, new Vector(20,20,20));
			body.setMass(1);
			body.addOnUpdate(b -> b.pese());
		} else if (name.equals(Sphere.class.getSimpleName()) || name.equals("Ball")) {
			obj = body = new Sphere(location, 10);
			body.setMass(1);
			body.addOnUpdate(b -> b.pese());
		} else if (name.equals("Floor"))
			obj = new Plane(location, rotation).withName("Floor");
		else if (name.equals(Plane.class.getSimpleName()))
			obj = new Plane(location, rotation);
		else if (name.equals(Target.class.getSimpleName()))
			obj = new Target(location, rotation);
		else if (name.equals(Starship.class.getSimpleName()))
			obj = new Starship(location, rotation);
		else if (name.equals(Weaponry.class.getSimpleName()))
			obj = new Weaponry(location, rotation);
		else if (name.equals("missile_launcher"))
			obj = new MissileLauncher(location, rotation);
		else {
			System.err.println("\""+name+"\" unknown, ignoring.");
			return null;
		}
		return obj;
	}
	
	// --- on all methods ---
	
	/** To call before all update methods. */
	public void beforeUpdateAll() {
		game.debug.setCurrentWork("objects pre-update");
		//updateObjectLists();
		for (Object o : objects) 
			o.beforeUpdate();
	}
	
	/** Update the colliders and effects (parents first (automatic)). */
	public void updateAll() {
		game.debug.setCurrentWork("objects update");
		for (Object o : objects)
			o.update();
		//updateObjectLists();
	}

	/** Display all colliders and effects in the scene. */
	public void displayAll() {
		game.debug.setCurrentWork("display objects");
		for(Object o : objects)
			o.display();
	}
	
	// --- private stuff ---

	/** Effectively remove / add objects to the lists. */
	/*private void updateObjectLists() {
		if (toRemove.size() > 0) {
			objects.removeAll(toRemove);
			colliders.removeAll(toRemove);
			toRemove.forEach(o -> {
				o.onDelete();
				notifyChange(o, Change.ObjectRemoved);
			});
			toRemove.clear();
		}
		if (toAdd.size() > 0) {
			toAdd.forEach(o->addNow(o));
			toAdd.clear();
		}
	}*/

	private void notifyChange(Object o, Change change) {
		ToolWindow.runLater(() -> {
			synchronized (this) {
				this.setChanged();
				this.notifyObservers(new Arg(o, change));
			}
		});
	}
}
