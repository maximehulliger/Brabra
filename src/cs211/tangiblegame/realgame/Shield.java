package cs211.tangiblegame.realgame;

import processing.core.PVector;
import cs211.tangiblegame.geo.Ellipsoide;

public class Shield extends Ellipsoide {

	public Shield(PVector location, PVector rayons) {
		super(location, -1, rayons);
	}
	
	public void display() {
		app.fill(255, 100);
		super.display();
	}

}
