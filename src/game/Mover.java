package game;

import game.geo.*;
import processing.core.*;

//une sphère soumise à la gravité.
public class Mover extends Sphere {
	public Mover(PVector location)  {
		//super(location, zero, 1, new PVector(10, 20, 10));
		super(location, 1, 10);
	}

	public void addForces() {
		pese();
		freineAbs(0.05f);
		freineRot(0.05f);
	}
}