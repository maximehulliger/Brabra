package brabra.game.physic;

public class CollisionPPolyedre extends Collision<PseudoPolyedre, PseudoPolyedre> {
	
	public CollisionPPolyedre(PseudoPolyedre p1, PseudoPolyedre p2) {
		super(p1, p2);
	}

	public void resolve() {
		ignore();
		/*
		if (!areCollidingFast(c1, c2))
			return;
		
		//vecteurs relatifs normalisé
		Vector p1Top2 = p2.location().minus(p1.location()).normalized(); //TODO normale d'une face !
		Vector p2Top1 = p1Top2.multBy(-1);
		
		//1. si un point est en collision / une face
		Vector pCont1 = p1.pointContre(p2Top1); //point de p1 vers p2
		Vector pCont2 = p2.pointContre(p1Top2);
		if (p2.isIn(pCont1)) {
			impact = p2.projette(pCont1);
			Line colLine = p2.collisionLineFor(impact);
			norm = colLine.norm;
			correction = impact.minus(pCont1);
		} else if (p1.isIn(pCont2)) {
			impact = p1.projette(pCont2);
			Line colLine = p1.collisionLineFor(impact);
			norm = colLine.norm;
			correction = impact.minus(pCont2);
		}
		*/
	}
}
