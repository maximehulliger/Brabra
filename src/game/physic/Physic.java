package game.physic;

import game.Effect;
import game.geo.Sphere;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Physic
{
	public static final float gravityConstant = 0.8f; //0.7f
	
	// les agents B)
	public ArrayList<Collider> colliders = new ArrayList<>();
	public ArrayList<Collider> toRemove = new ArrayList<>();
	public ArrayList<Collider> toAdd = new ArrayList<>();
	public ArrayList<Effect> effects = new ArrayList<>();
	public ArrayList<Effect> effectsToRemove = new ArrayList<>();
	public ArrayList<Effect> effectsToAdd = new ArrayList<>();
	
	public void displayAll() {
		for(Collider c : colliders)
			c.display();
		for(Effect e : effects) {
			e.display();
		}
	}
	
	// just... do magic :D
	public void doMagic() {  
		//1. on update les acteurs et les effets
		for (Collider c : colliders)
			c.update();
		if (toRemove.size() > 0 ) {
			for (Collider c : toRemove) {
				colliders.remove(c);
			}
			toRemove.clear();
		}
		if (toAdd.size() > 0) {
			for (Collider c : toAdd) {
				colliders.add(c);
				//c.update();
			}
			toAdd.clear();
		}
		for (Effect e : effects)
			e.update();
		if (effectsToRemove.size() > 0) {
			for (Effect e : effectsToRemove) {
				effects.remove(e);
			}
			effectsToRemove.clear();
		}
		if (effectsToAdd.size() > 0) {
			for (Effect e : effectsToAdd) {
				effects.add(e);
				//e.update();
			}
			effectsToAdd.clear();
		}
		
		//2. on détermine et filtre les collisions pour chaque paire possible (c, o).
		List<Collision> collisions = new LinkedList<>();
		
		for (int ic=0; ic<colliders.size(); ic++) {
			Collider c = colliders.get(ic);
			for (int io=ic+1; io<colliders.size(); io++) {
				Collider o = colliders.get(io);
				
				if ( (o.affectedByCollision || c.affectedByCollision) && (o.doCollideFast(c) && c.doCollideFast(o)) ) {
					Collision col = null;
					if (c instanceof Sphere)
						col = new CollisionSphere((Sphere)c, o);
					else if (o instanceof Sphere)
						col = new CollisionSphere((Sphere)o, c);
					else if (c instanceof PseudoPolyedre && o instanceof PseudoPolyedre)
						col = new CollisionPPolyedre((PseudoPolyedre)c, (PseudoPolyedre)o);
					else
						throw new IllegalArgumentException("collider inconnu !");
					collisions.add( col );
		    	}
		  	}
		}
		
		//3. on résout les collisions
		for (Collision col : collisions) {
			col.resolve();
		}
		
		//4. on applique les collision aux agents (si nécessaire)
		for (Collision col : collisions) {
			col.apply();
		}
	}
}
