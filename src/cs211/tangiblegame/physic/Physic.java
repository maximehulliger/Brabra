package cs211.tangiblegame.physic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Sphere;
import cs211.tangiblegame.realgame.Effect;

public class Physic extends ProMaster
{
	public float gravity = 0.8f; //0.7f
	public boolean paused = false;
	
	// les agents B)
	public ArrayList<Collider> colliders = new ArrayList<>();
	public ArrayList<Collider> toRemove = new ArrayList<>();
	public ArrayList<Collider> toAdd = new ArrayList<>();
	public ArrayList<Effect> effects = new ArrayList<>();
	public ArrayList<Effect> effectsToRemove = new ArrayList<>();
	public ArrayList<Effect> effectsToAdd = new ArrayList<>();
	
	/** Display all colliders and effects in the scene. */
	public void displayAll() {
		game.debug.setCurrentWork("display objects");
		for(Object o : both(colliders, effects))
			o.display();
	}
	
	/** Update the colliders and effects (parents first). */
	public void updateAll() {
		for (Object o : both(colliders, effects))
			o.updated = false;
		for (Object o : both(colliders, effects)) {
			if (!o.updated) {
				if (o.hasParent() && !o.parent().updated)
					o.parent().update();
				else {
					game.debug.setCurrentWork("physic: updating \""+o+"\"");
					o.update();
					o.updated = true;
				}
			}
		}
		
		if (toRemove.size() > 0 ) {
			colliders.removeAll(toRemove);
			for (Collider c : toRemove)
				c.onDelete();
			toRemove.clear();
		}
		if (toAdd.size() > 0) {
			colliders.addAll(toAdd);
			toAdd.clear();
		}
		if (effectsToRemove.size() > 0) {
			effects.removeAll(effectsToRemove);
			effectsToRemove.clear();
		}
		if (effectsToAdd.size() > 0) {
			effects.addAll(effectsToAdd);
			effectsToAdd.clear();
		}
	}
	
	/** Just... do Magic  :D */
	public void doMagic() {
		game.debug.setCurrentWork("objects update");
		//1. on update les acteurs et les effets
		updateAll();
		
		game.debug.setCurrentWork("physic magic");
		//2. on détermine et filtre les collisions pour chaque paire possible (c, o).
		List<Collision> collisions = new LinkedList<>();
		
		for (int ic=0; ic<colliders.size(); ic++) {
			Collider c = colliders.get(ic);
			for (int io=ic+1; io<colliders.size(); io++) {
				Collider o = colliders.get(io);
				
				if ( (o.affectedByCollision || c.affectedByCollision) && (o.doCollideFast(c) && c.doCollideFast(o)) ) {
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
		  	}
		}
		
		try {
			//3. on résout les collisions
			for (Collision col : collisions)
				col.resolve();
			
			//4. on applique les collision aux agents (si nécessaire)
			for (Collision col : collisions)
				col.apply();

		} catch (Exception e) {
			game.debug.err("physical error :/");
			e.printStackTrace();
		}
	}
}
