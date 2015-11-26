package cs211.tangiblegame.realgame;

import cs211.tangiblegame.geo.*;
import processing.core.*;

//une sphère soumise à la gravité.
public class Mover extends Sphere {
	public Mover(PVector location)  {
		//super(location, zero, 1, new PVector(10, 20, 10));
		super(location, 1, 30);
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