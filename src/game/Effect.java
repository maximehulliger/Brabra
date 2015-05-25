package game;

//import java.util.LinkedList;
//import java.util.List;

import processing.core.PVector;


public abstract class Effect extends ProMaster {
	public PVector location;
	protected int timeLeft;
	protected final int lifeTime;
	
	public Effect(PVector location, int lifeTime) {
		this.location = location;
		timeLeft = lifeTime;
		this.lifeTime = lifeTime;
	}
	
	// retourne l'avancement entre 0 -> 1;
	protected float etat() {
		return 1 - ((float)timeLeft)/lifeTime;
	}
	
	// retourne l'avancement de l'explosion en crête; 0 au début, 1 au milieu, 0 à la fin
	protected float etatCrete() {
		float t = etat(); //0->2
		if (t < 0.3f)	return t/0.3f;
		else 			return (1-t)/0.7f;
	}
	
	public void update() {
		if (--timeLeft <= 0)
			app.physic.effectsToRemove.add(this);
	}
	
	public abstract void display();
	
	public static class Explosion extends Effect {
		private float radius;
		
		public Explosion(PVector location, float radius) {
			super(location, toFrame(0.6f));
			this.radius = radius;
		}
		
		private PVector randomPos() {
			PVector pos = new PVector(random.nextFloat(), random.nextFloat(), random.nextFloat());
			pos.mult( radius * etatCrete() );
			return PVector.add(location, pos);
		}
		
		public void update() {
			super.update();
			for (int i=0; i<5; i++)
				app.physic.effectsToAdd.add( new Bulbe(randomPos()) );
		}
		
		//affiche l'onde de choc
		public void display() {
			
		}
		
		private class Bulbe extends Effect {
			private static final float minRad = 5;
			private static final float maxRad = 10;
			private final float radius;
			private final int color;
			
			public Bulbe(PVector location) {
				super(location, toFrame(0.2f));
				this.color = app.color(255, random.nextInt(256), 0);
				this.radius = random(minRad, maxRad);
			}
			
			public void display() {
				app.pushMatrix();
				app.translate(location.x, location.y, location.z);
				app.fill(color, 150);
				app.sphere(radius*etatCrete());
				app.popMatrix();
			}
			
		}
	}
}
