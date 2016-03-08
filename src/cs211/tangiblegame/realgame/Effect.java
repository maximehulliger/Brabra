package cs211.tangiblegame.realgame;

import processing.core.PApplet;
import processing.core.PVector;
import cs211.tangiblegame.physic.Object;

public abstract class Effect extends Object {
	protected int timeLeft;
	protected final int lifeTime;

	/** Create an effect at this location. lifetime in frame, -1 -> infinite */
	public Effect(PVector location, int lifeTime) {
		super(location);
		this.timeLeft = lifeTime;
		this.lifeTime = lifeTime;
	}

	/** Create an effect at this location. */
	public Effect(PVector location) {
		this(location, -1);
	}

	public void update() {
		if (--timeLeft == 0) // -1 -> continue
			game.physic.effectsToRemove.add(this);
	}
	
	/** Retourne l'avancement entre 0 -> 1. 0 si infini. */
	protected float etat() {
		return 1 - ((float)timeLeft)/lifeTime;
	}
	
	/** 
	 * Retourne l'avancement de l'explosion en crête:
	 * 0 au début, 1 au milieu, 0 à la fin
	 */
	protected float etatCrete() {
		float t = etat(); //0->2
		if (t < 0.3f)	return t/0.3f;
		else 			return (1-t)/0.7f;
	}

	protected static int toFrame(float seconds) {
		return PApplet.round(seconds * app.frameRate);
	}
	
	public static class Explosion extends Effect {
		private float radius;
		
		public Explosion(PVector location, float radius) {
			super(location, toFrame(0.6f));
			this.radius = radius;
		}
		
		private PVector randomPos() {
			PVector pos = new PVector(random.nextFloat(), random.nextFloat(), random.nextFloat());
			pos.mult( radius * etatCrete() );
			return PVector.add(locationAbs, pos);
		}
		
		public void update() {
			super.update();
			for (int i=0; i<5; i++)
				game.physic.effectsToAdd.add( new Bulbe(randomPos()) );
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
				pushLocal();
				app.fill(color, 150);
				app.sphere(radius*etatCrete());
				popLocal();
			}
			
		}
	}
}
