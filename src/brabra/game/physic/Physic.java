package brabra.game.physic;

import java.util.List;
import java.util.stream.Collectors;

import brabra.ProMaster;
import brabra.game.physic.geo.Sphere;
import brabra.game.scene.Object;

import java.util.ArrayList;

public class Physic extends ProMaster
{
	public float gravity = 0.8f; //0.7f
	public boolean running = true;
	
	// les agents B)
	private List<Object> objects = new ArrayList<Object>();
	private List<Collider> colliders = new ArrayList<Collider>();
	private List<Object> toRemove = new ArrayList<Object>();
	private List<Object> toAdd = new ArrayList<Object>();
	private int errCount = 0;
	
	// --- getters / modifiers

	/** Return all the objects in the scene. */
	public List<Object> objects() {
		return objects;
	}
	
	/** Return all the colliders in the scene. */
	public List<Collider> colliders() {
		return colliders;
	}
	
	public List<Collider> activeColliders() {
		return colliders.stream().filter(c -> !c.ghost).collect(Collectors.toList());
	}
	
	public String state() {
		return running ? "running !" : "paused :)";
	}
	
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
	
	/** Update the colliders and effects (parents first (automatic)). */
	public void updateAll() {
		if (running) {
			game.debug.setCurrentWork("objects update");
			updateObjectLists();
			for (Object o : objects) o.beforeUpdate();
			for (Object o : objects) o.update();
			updateObjectLists();
		}
	}

	/** Display all colliders and effects in the scene. */
	public void displayAll() {
		game.debug.setCurrentWork("display objects");
		for(Object o : objects)
			o.display();
	}
	
	/** Just... do Magic  :D <p>Actually resolve collisions. */
	public void doMagic() {
		game.debug.setCurrentWork("physic magic");
		try {
			//2. on détermine et filtre les collisions pour chaque paire possible (c, o).
			List<Collision> collisions = new ArrayList<>();
			forAllPairs(activeColliders(), (c,o)-> {
				if ( (o.affectedByCollision || c.affectedByCollision) && !c.isRelated(o) && (o.doCollideFast(c) && c.doCollideFast(o)) ) {
					Collision col = null;
					if (c.affectedByCollision && c instanceof Sphere)
						col = new CollisionSphere((Sphere)c, o);
					else if (o.affectedByCollision && o instanceof Sphere)
						col = new CollisionSphere((Sphere)o, c);
					else if (c instanceof PseudoPolyedre && o instanceof PseudoPolyedre) {
						PseudoPolyedre ppc, ppo;
						if (c.affectedByCollision) {
							ppc = (PseudoPolyedre)c;
							ppo = (PseudoPolyedre)o;
						} else {
							ppc = (PseudoPolyedre)o;
							ppo = (PseudoPolyedre)c;
						}
						col = new CollisionPPolyedre(ppc, ppo);
					} else
						throw new IllegalArgumentException("collider inconnu !");
					collisions.add( col );
		    	}
			});
		
			//3. on résout les collisions
			for (Collision col : collisions)
				col.resolve();
			
			//4. on applique les collision aux agents (si nécessaire)
			for (Collision col : collisions)
				col.apply();

		} catch (Exception e) {
			game.debug.err("physical error :/");
			e.printStackTrace();
			if (++errCount >= 3) {
				running = false;
				game.debug.msg(1, "physic paused (after 3 errors)");
			}
		}
	}
	
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
