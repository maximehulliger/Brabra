package cs211.tangiblegame.realgame;

import processing.core.PVector;
import cs211.tangiblegame.geo.Ellipsoide;

public class Shield extends Ellipsoide {

	public Shield(PVector location, PVector rayons) {
		super(location, identity, rayons);
	}
	
	public void display() {
		app.fill(255, 100);
		super.display();
	}

}
