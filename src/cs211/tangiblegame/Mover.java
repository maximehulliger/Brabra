package cs211.tangiblegame;

import cs211.tangiblegame.geo.*;
import processing.core.*;

//une sphère soumise à la gravité.
public class Mover extends Sphere {
	public Mover(PVector location)  {
		//super(location, zero, 1, new PVector(10, 20, 10));
		super(location, 1, 10);
	}

	public void addForces() {
		pese();
		freine(0.05f);
	}
}