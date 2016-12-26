package brabra.game.scene.weapons;

import brabra.game.physic.Body;
import brabra.game.physic.geo.Box;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.Effect;
import processing.core.PImage;
import processing.core.PShape;

/** A weapon shooting missiles. */
public class MissileLauncher extends Weapon {

	private static final float[] tiersTRecharge = new float[] { 7, 20, 50 };
	private static final float[] tiersPuissance = new float[] { 10, 50, 200 };
	private static final float[] tiersSize = new float[] { 4, 8, 15 };
	
	private static PShape missile;
	private static PImage missileImg;
	
	private final MissileLauncher launcher;
	private int missileNextId = 1;
	
	public MissileLauncher() {
		super(tiersPuissance.length);
		this.setTier(1);
		this.launcher = this;
		// load resources
		if (missile == null) {
			missile = app.loadShape("rocket.obj");
			missileImg = app.loadImage("missile.jpg");
			int[] pixels = missileImg.pixels;
			for (int i=0; i<pixels.length; i++)
				if (pixels[i] == app.color(0))
					pixels[i] = app.color(0, 0);
		}
	}

	public PImage img() {
		return missileImg;
	}
	
	public void setTier(int tier) {
		super.setTier(tier);
		super.set(tiersTRecharge[tier-1], tiersPuissance[tier-1]);
		super.setName("Missile launcher t"+tier);
	}
	
	public boolean fire() {
		if (super.fire()) {
			game.scene.add(new Missile());
			return true;
		} else
			return false;
	}

	/** A missile that explode on collision and damage (life) the obstacle. */
	public class Missile extends Box {
		
		private final int tier;
		private final int puissance;
		
		public Missile() {
			super(vec(tiersSize[tier0()]*2, tiersSize[tier0()]*2, tiersSize[tier0()]*7));
			position.set(launcher.master().parent().absolute(launcher.position));
			rotation.set(launcher.rotation.rotatedBy(launcher.master().parent().rotation));
			this.tier = tier0();
			this.puissance = puissance();
			super.addOnUpdate(m -> m.avance( puissance()*3 ));
			super.setName("Missile t"+tier()+" p("+puissance+") ["+missileNextId++ +"]");
			super.setMass(puissance);
			super.setDisplayCollider(displayColliders());
		}

		public void display() {
			pushLocal();
			displayColliderMaybe();
			displayInteractionMaybe();
			app.scale(tiersSize[tier]);
			app.shape(missile);
			popLocal();
		}

		public void onCollision(Body col, Vector impact) {
			// explodes and disappears
			game.scene.remove( this );
			game.scene.add( new Effect.Explosion( position, tiersSize[tier] ) );
			// to damage the targets
			if (col instanceof Target) {
				((Target)col).damage(puissance);
			}	
		}

		public boolean isCollidingWith(Body col) {
			// avoid contact with the shooter's body
			return col != master().parent();
		}
	}

	@Override
	public void display() {}
}
