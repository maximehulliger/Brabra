package brabra.game.scene.weapons;

import brabra.game.physic.geo.Ellipsoide;
import brabra.game.physic.geo.Vector;

public class Shield extends Ellipsoide {

	public Shield(Vector location, Vector rayons) {
		super(location, identity, rayons);
	}
	
	public void display() {
		app.fill(255, 100);
		super.display();
	}

}
