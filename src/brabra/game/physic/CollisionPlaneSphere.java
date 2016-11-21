package brabra.game.physic;

import brabra.game.physic.geo.Line.Projection;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Sphere;

public class CollisionPlaneSphere extends Collision<Plane, Sphere> {

	public CollisionPlaneSphere(Plane c1, Sphere c2) {
		super(c1, c2);
	}

	@Override
	public void resolve() {
		// check that collision happened
		Projection proj = c2.projetteSur(c1.normale());
		if (!proj.comprend(0))
			ignore();
		else {
			impact = c1.projette(c2.location());
			norm = c1.normale().norm;
			correction = norm.withMag(-proj.from);
		}
	}
}
