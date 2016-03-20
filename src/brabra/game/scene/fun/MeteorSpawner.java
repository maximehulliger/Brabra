package brabra.game.scene.fun;

import brabra.game.physic.geo.Cube;
import brabra.game.physic.geo.Sphere;
import brabra.game.scene.Effect;
import processing.core.PShape;
import processing.core.PVector;

/** Pop meteors randomly in a box (in front of the parent). */
public final class MeteorSpawner extends Effect {
	
	private static final int nbMeteorMax = 60;
	private static final int ratioRandomToPlayer = 10; //nb de météorite tirée aléatoirement pour une contre le joueur.
	private static final int minPopTime = 15, maxPopTime = 60;
	private static final float minSpeed = 1, maxSpeed = 12;
	private static final float minMass = 2, maxMass = 30;
	private static final float minRadius = 1, maxRadius = 120;
	
	private static PShape meteor;
	private Cube spawnCage;
	private int nbMeteor = 0;
	private int randomMeteorCounter = 0;
	private int nextPopTime;
	
	public MeteorSpawner(PVector location, PVector size) {
		super(location);
		spawnCage = new Cube(location, identity, size);
		setNext();
		// load resources
		if (meteor==null) {
			meteor = app.loadShape("asteroid.obj");
		}
	}
	
	public boolean update() {
		if (super.update()) {
			spawnCage.update();
			if (--nextPopTime < 0) {
				popMeteor();
				setNext();
			}
			return true;
		} else
			return false;
	}
	
	public void popMeteor() {
		if (nbMeteor < nbMeteorMax) {
			int idxStartFace = random.nextInt(6);
			PVector startPos = spawnCage.faces[idxStartFace].randomPoint();
			PVector goal;
			if (randomMeteorCounter++ >= ratioRandomToPlayer) { //temps de viser le joueur
				goal = parent().location();
				randomMeteorCounter = 0;
			} else {
				int toOtherSideIdx = (idxStartFace%2 == 0 ? 1 : -1);
				goal = spawnCage.faces[idxStartFace + toOtherSideIdx].randomPoint();
			}
			game.physic.add( new Meteor(startPos, goal) );
			nbMeteor++;
		}
	}

	private void setNext() {
		nextPopTime = minPopTime + random.nextInt(maxPopTime - minPopTime);
	}
	
	private class Meteor extends Sphere {

		public Meteor(PVector startPos, PVector goal) {
			super(startPos, identity, -1);
			
			float tailleRatio = random.nextFloat();
			setMass(minMass + tailleRatio * (maxMass - minMass));
			radius = minRadius + tailleRatio * (maxRadius - minRadius);
			
			float speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
			velocityRel.set( PVector.sub(goal, startPos).setMag(speed) );
			rotationRelVel.set( randomVec(1), speed*random.nextFloat()/30 );
		}
		
		public void onDelete() {
			nbMeteor--;
		}
		
		public void display() {
			pushLocal();
			if (!displayColliderMaybe()) {
				app.scale(radius);
				app.shape(meteor);
			}
			popLocal();
		}
	}
}
