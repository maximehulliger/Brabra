package brabra.game.physic;

import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Vector;

public class CollisionPPolyedre extends Collision {
	private final PseudoPolyedre p1, p2;
	
	public CollisionPPolyedre(PseudoPolyedre p1, PseudoPolyedre p2) {
		super(p1, p2);
		this.p1 = p1;
		this.p2 = p2;
	}

	public void resolve() {
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
			nulle = false;
		} else if (p1.isIn(pCont2)) {
			impact = p1.projette(pCont2);
			Line colLine = p1.collisionLineFor(impact);
			norm = colLine.norm;
			correction = impact.minus(pCont2);
			nulle = false;
		}
		
		/*
		impact = obstacle.projette(sphere.location);
		if (distSq(sphere.location, impact) >= sq(sphere.radius)) 
			return;
		
		Line colLine = obstacle.collisionLineFor(sphere.location);
		norm = colLine.norm;
		Vector contact = sphere.projette(impact);
		correction = Vector.sub(impact, contact);
		nulle = false;*/
	}
}
