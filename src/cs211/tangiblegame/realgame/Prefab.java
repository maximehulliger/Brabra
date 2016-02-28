package cs211.tangiblegame.realgame;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.*;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.physic.Collider;
import processing.core.*;


/**
 * Help class to add object in the scene
 */
public class Prefab extends ProMaster {
	public static XmlLoader file = new XmlLoader();
	
	/**
	 *	add a new object to the physic from the name. following names supported:
	 *	box, ball, floor, starship
	 */
	public static Body add(String name, PVector location) {
		Collider col;
		if (name.equals("box"))
			col = new TCube(location);
		else if (name.equals("ball"))
			col = new TBall(location);
		else if (name.equals("floor"))
			col = new Plane(location, identity).withName("Floor");
		else if (name.equals("objectif"))
			col = new Armement.Objectif(location);
		else if (name.equals("starship")) {
			col = new Starship(location);
		} else {
			System.out.println("\""+name+"\" unknown, ignoring");
			return null;
		}
		game.physic.colliders.add( col );
		return col;
	}
	
	public static Body add(String name, PVector location, Quaternion rotation) {
		Body b = add(name, location);
		if (b == null)
			return null;
		else {
			b.rotation.set(rotation);
			return b;
		}
			
	}
	
	//une sphère soumise à la gravité.
	private static class TBall extends Sphere {
		public TBall(PVector location)  {
			super(location, 1, 10);
		}
		public void addForces() {
			pese();
			//freine(0.05f);
		}
		public void display() {
			app.fill(0, 100, 200);
			super.display();
		}
	}
	
	private static class TCube extends Cube {
		public TCube(PVector location) {
			super(location, identity, 1, vec(20,20,20));
		}
		protected void addForces() {
			this.pese();
		}
		public void display() {
			app.fill(200, 200, 0);
			super.display();
		}
	}

}