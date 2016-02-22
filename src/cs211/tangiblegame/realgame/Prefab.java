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
	 *	box, ball, floor
	 */
	public static Body add(String name, PVector location) {
		Collider col;
		if (name == "box")
			col = new TCube(location);
		else if (name == "ball")
			col = new TBall(location);
		else if (name == "floor")
			col = new Plane(location, identity, -1, vec(1000, 0, 1000));
		else {
			System.out.println("\""+name+"\" unknown, ignoring");
			return null;
		}
		
		RealGame.physic.colliders.add( col );
		return col;
	}
	
	//une sphère soumise à la gravité.
	private static class TBall extends Sphere {
		public TBall(PVector location)  {
			super(location, 1, 1);
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
			super(location, identity, 1, vec(1,1,1));
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