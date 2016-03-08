package cs211.tangiblegame.physic;

import cs211.tangiblegame.geo.Line;
import processing.core.PVector;

public class CollisionPPolyedre extends Collision {
	private final PseudoPolyedre p1, p2;
	
	public CollisionPPolyedre(PseudoPolyedre p1, PseudoPolyedre p2) {
		super(p1, p2);
		this.p1 = p1;
		this.p2 = p2;
	}

	public void resolve() {
		if (!areCollidingFast(collider, obstacle))
			return;
		
		//vecteurs relatifs normalisé
		PVector p1Top2 = PVector.sub( p2.locationAbs, p1.locationAbs ); //TODO normale d'une face !
		p1Top2.normalize();
		PVector p2Top1 = PVector.mult( p1Top2, -1 );
		
		//1. si un point est en collision / une face
		PVector pCont1 = p1.pointContre(p2Top1); //point de p1 vers p2
		PVector pCont2 = p2.pointContre(p1Top2);
		if (p2.isIn(pCont1)) {
			impact = p2.projette(pCont1);
			Line colLine = p2.collisionLineFor(impact);
			norm = colLine.norm;
			correction = PVector.sub(impact, pCont1);
			nulle = false;
		} else if (p1.isIn(pCont2)) {
			impact = p1.projette(pCont2);
			Line colLine = p1.collisionLineFor(impact);
			norm = colLine.norm;
			correction = PVector.sub(impact, pCont2);
			nulle = false;
		}
		
		/*
		impact = obstacle.projette(sphere.location);
		if (distSq(sphere.location, impact) >= sq(sphere.radius)) 
			return;
		
		Line colLine = obstacle.collisionLineFor(sphere.location);
		norm = colLine.norm;
		PVector contact = sphere.projette(impact);
		correction = PVector.sub(impact, contact);
		nulle = false;*/
	}
}
