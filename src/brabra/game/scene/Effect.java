package brabra.game.scene;

import java.util.ArrayList;
import java.util.List;

import brabra.Brabra;
import brabra.Master;
import brabra.game.physic.geo.Vector;

public abstract class Effect extends Object {
	protected int timeLeft;
	protected final int lifeTime;

	/** Create an effect at this location. lifetime in frame, -1 -> infinite */
	public Effect(Vector location, int lifeTime) {
		position.set(location);
		this.timeLeft = lifeTime;
		this.lifeTime = lifeTime;
	}

	/** Create an effect at this location. */
	public Effect(Vector location) {
		this(location, -1);
	}

	public void update() {
		super.update();
		if (--timeLeft == 0) // -1 -> continue
				game.scene.remove(this);
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
		return round(seconds * Brabra.frameRate);
	}
	
	public static class Explosion extends Effect {
		private float radius;
		private List<Bulbe> bulbes =  new ArrayList<>();
		
		public Explosion(Vector location, float radius) {
			super(location, toFrame(0.6f));
			this.radius = radius;
		}
		
		private Vector randomPos() {
			Vector pos = new Vector(Master.randomBi(), Master.randomBi(), Master.randomBi());
			pos.mult( radius * etatCrete() );
			return position.plus(pos);
		}
		
		public void update() {
			super.update();
			for (int i=0; i<5; i++)
				bulbes.add( new Bulbe(randomPos()) );
		}
		
		//affiche l'onde de choc
		public void display() {
			bulbes.forEach(b -> b.display());
		}

		private class Bulbe extends Effect {
			private static final float minRad = 5;
			private static final float maxRad = 10;
			private final float radius;
			private final int color;
			
			public Bulbe(Vector location) {
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
