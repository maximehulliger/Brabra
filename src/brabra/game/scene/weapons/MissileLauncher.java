package brabra.game.scene.weapons;

import brabra.game.physic.Collider;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Sphere;
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
	
	public MissileLauncher(Vector loc, Quaternion rot) {
		super(loc, rot, tiersPuissance.length);
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
	public class Missile extends Sphere {//Cube {
		
		private final int tier;
		private final int puissance;
		
		public Missile() {
			super(launcher.location(), launcher.rotation(), tiersSize[tier0()]);
			//super(location, rotation, vec(sizeRatio*2, sizeRatio*2, sizeRatio*7));
			this.tier = tier0();
			this.puissance = puissance();
			super.addOnUpdate(m -> m.avance( puissance()*3 ));
			super.setName("Missile t"+tier()+" p("+puissance+")");
			super.setMass(puissance);
			super.setParent(master().parent(), null);
			super.velocityRel.set(launcher.velocity());
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

		public void onCollision(Collider col, Vector impact) {
			// explodes and disappears
			game.scene.remove( this );
			game.scene.add( new Effect.Explosion( impact, 30 ) );
			// to damage the targets
			if (col instanceof Target) {
				((Target)col).damage(puissance);
			}
		}
	}
}
