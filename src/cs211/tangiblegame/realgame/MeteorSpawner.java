package cs211.tangiblegame.realgame;

import cs211.tangiblegame.geo.Cube;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.geo.Sphere;
import cs211.tangiblegame.physic.Body;
import processing.core.PShape;
import processing.core.PVector;

/**
 * fais apparaitre des météorites aléatoirement dans une box
 */
public final class MeteorSpawner extends Effect {
	public static PShape meteor;
	private static final int nbMeteorMax = 60;
	private static final int ratioRandomToPlayer = 10; //nb de météorite tirée aléatoirement pour une contre le joueur.
	private static final int minPopTime = 15, maxPopTime = 60;
	private static final float minSpeed = 1, maxSpeed = 12;
	private static final float minMass = 2, maxMass = 30;
	private static final float minRadius = 1, maxRadius = 120;
	
	private Cube spawnCage;
	private int nbMeteor = 0;
	private int randomMeteorCounter = 0;
	private int nextPopTime;
	
	public MeteorSpawner(Body parent, PVector location, PVector size) {
		super(location);
		spawnCage = new Cube(location, new Quaternion(), -1, size);
		spawnCage.parent = parent;
		setNext();
	}
	
	public void update() {
		spawnCage.update();
		if (--nextPopTime < 0) {
			popMeteor();
			setNext();
		}
	}

	public void display() {
		// TODO Auto-generated method stub
		
	}
	
	public void popMeteor() {
		if (nbMeteor < nbMeteorMax) {
			int idxStartFace = random.nextInt(6);
			PVector startPos = spawnCage.faces[idxStartFace].randomPoint();
			PVector goal;
			if (randomMeteorCounter++ >= ratioRandomToPlayer) { //temps de viser le joueur
				goal = parent.location;
				randomMeteorCounter = 0;
			} else {
				int toOtherSideIdx = (idxStartFace%2 == 0 ? 1 : -1);
				goal = spawnCage.faces[idxStartFace + toOtherSideIdx].randomPoint();
			}
			app.intRealGame.physic.toAdd.add( new Meteor(startPos, goal) );
			nbMeteor++;
		}
	}

	private void setNext() {
		nextPopTime = minPopTime + random.nextInt(maxPopTime - minPopTime);
	}
	
	private class Meteor extends Sphere {

		public Meteor(PVector startPos, PVector goal) {
			super(startPos, -1, -1);
			
			float tailleRatio = random.nextFloat();
			setMass(minMass + tailleRatio * (maxMass - minMass));
			radius = minRadius + tailleRatio * (maxRadius - minRadius);
			
			float speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
			velocity.set( PVector.sub(goal, startPos).setMag(speed) );
			rotationVel.set( randomVec(), speed*random.nextFloat()/30 );
		}
		
		public void onDelete() {
			nbMeteor--;
		}
		
		public void display() {
			pushLocal();
			app.scale(radius);
			app.shape(meteor);
			popLocal();
			if (drawCollider) {
				app.fill(255, 0, 0, 100);
				super.display();
			}
		}
	}
}
