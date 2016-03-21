package brabra.game.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import brabra.ProMaster;
import brabra.game.physic.Collider;

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
	public void add(Object o) {
		toAdd.add(o);
	}

	/** Add an object immediately into the scene. */
	public void addNow(Object o) {
		objects.add(o);
		if (o instanceof Collider)
			colliders.add((Collider)o);
	}
	
	/** Remove an object from the scene (on next update). */
	public void remove(Object o) {
		toRemove.add(o);
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
