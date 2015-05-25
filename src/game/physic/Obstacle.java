package game.physic;

import game.geo.Line;
import processing.core.PVector;

public interface Obstacle {
	public abstract void display();
	
	public abstract Line.Projection projetteSur(Line ligne);
	
	// retourne le point à la surface de l'obstacle si l'intruder est dedans. retourne null sinon.
	public abstract PVector getContact(PVector intruder);
	
	//point à la surface du collider le plus dans l'obstacle.
	public abstract PVector[] getIntruderPointOver(Line colLine);
	
	//------ obstacle: 
	
	// la ligne sur laquelle on va projeter. basé à la surface de l'obstacle
	public abstract Line collisionLineFor(Collider c);
	
	//projette ce point dans la face la plus proche de l'obstacle. 
	public abstract PVector projette(PVector point);
}
