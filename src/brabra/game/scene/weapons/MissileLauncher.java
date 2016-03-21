package brabra.game.scene.weapons;

import brabra.Brabra;
import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Sphere;
import brabra.game.scene.Effect;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;

public class MissileLauncher extends Weapon {

	private static final float sizeRatioFactor = 1f;
	private static final float tRechargeRatio = 0.7f;
	private static final float[] tiersTRecharge = new float[] { 3, 15, 30 };
	private static final float[] tiersPuissance = new float[] { 10, 50, 200 };
	private static final float[] tiersRatioSize = new float[] { 3, 8, 12.5f };
	
	private static PShape missile;
	private static PImage missileImg;
	
	public MissileLauncher(PVector loc, Quaternion rot) {
		super(loc, rot);
		setTier(1);
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
		updateState();
	}

	public void setUpgradeRatio(float ratio) {
		super.setUpgradeRatio(ratio);
		updateState();
	}
	
	private void updateState() {
		setName("Missile launcher t"+tier());
		sizeRatio = tiersRatioSize[tier] * Weapon.tiersRatioSize[tier] * upgradeRatio * sizeRatioFactor;
		puissance = tiersPuissance[tier] * Weapon.tiersRatioSize[tier] * upgradeRatio;
		tRecharge = PApplet.round(tiersTRecharge[tier] * tRechargeRatio / (Weapon.tiersRatioSize[tier]*upgradeRatio));
	}

	public void validate(Attributes attr) {
		updateState();
		super.validate(attr);
	}

	public boolean fire() {
		if (tempsRestant > 0) {
			indicateurErreur = Weaponry.tAffichageErreur;
			if (Brabra.verbosity > 2)
				System.out.println("encore "+tempsRestant+" frame(s) !");
			return false;
		} else {
			tempsRestant = tRecharge;
			game.scene.add( new Missile(location(), rotation()) );
			return true;
		}
	}

	public class Missile extends Sphere {//Cube {

		public Missile(PVector location, Quaternion rotation) {
			super(location, rotation, sizeRatio);
			//super(location, rotation, vec(sizeRatio*2, sizeRatio*2, sizeRatio*7));
			setName("Missile t"+(tier())+" p"+PApplet.round(puissance));
			setMass(puissance);
			setParent(launcher());
			setParentRel(ParentRelationship.None);
			velocityRel.set(velocity());
			setDisplayCollider(true);
		}

		public void display() {
			pushLocal();
			displayColliderMaybe();
			displayInteractionMaybe();
			app.scale(sizeRatio);
			app.shape(missile);
			popLocal();
		}

		protected void addForces() {
			avance( puissance*3 );
		}

		public void onCollision(Collider col, PVector impact) {
			game.scene.remove( this );
			game.scene.add( new Effect.Explosion( impact, 30 ) );

			//réaction objectif
			if (col instanceof Objectif) {
				((Objectif)col).damage(PApplet.round(puissance));
			}
		}
	}
}
