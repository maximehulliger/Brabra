package cs211.tangiblegame;

import cs211.tangiblegame.geo.Cube;
import cs211.tangiblegame.geo.Sphere;
import processing.core.PShape;
import processing.core.PVector;

/**
 * fais apparaitre des météorites aléatoirement dans une box
 */
public final class MeteorSpawner extends Cube {
	public static PShape meteor;
	private final float minSpeed = 1, maxSpeed = 12;
	private final float minMass = 2, maxMass = 4;
	private final float minRadius = 1, maxRadius = 40;
	private final int minPopTime = 1, maxPopTime = 10;
	
	private int nextPopTime;
	
	public MeteorSpawner(PVector location, PVector size) {
		super(location, zero, -1, size);
		
		setNext();
	}
	
	public void update() {
		super.update();
		if (--nextPopTime < 0) {
			popMeteor();
			setNext();
		}
	}
	
	private void setNext() {
		nextPopTime = minPopTime + random.nextInt(maxPopTime - minPopTime);
	}
	
	public void popMeteor() {
		int idxStartFace = random.nextInt(6);
		
		int toOtherSideIdx = (idxStartFace%2 == 0 ? 1 : -1);
		PVector startPos = faces[idxStartFace].randomPoint();
		PVector goal = faces[idxStartFace + toOtherSideIdx].randomPoint();
		
		app.physic.toAdd.add( new Meteor(startPos, goal) );
	}
	
	private class Meteor extends Sphere {

		public Meteor(PVector startPos, PVector goal) {
			super(startPos, -1, -1);
			
			float tailleRatio = random.nextFloat();
			setMass(minMass + tailleRatio * (maxMass - minMass));
			radius = minRadius + tailleRatio * (maxRadius - minRadius);
			
			float speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
			PVector vel = PVector.sub(goal, startPos);
			velocity.set(vel.array());
			velocity.setMag(speed);
			
			rotationVel.set(random.nextFloat(), random.nextFloat(), random.nextFloat());
			rotationVel.mult( speed/30f*random.nextFloat() );
		}
		
		public void update() {
			super.update();
			
			if ( !isIn(this.location))
				app.physic.toRemove.add(this);
		}
		
		public void display() {
			pushLocal();
			app.scale(radius);
			app.shape(meteor);
			popLocal();
		}
	}
}
