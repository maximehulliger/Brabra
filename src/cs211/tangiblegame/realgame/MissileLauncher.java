package cs211.tangiblegame.realgame;

import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.geo.Cube;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.physic.Collider;
import cs211.tangiblegame.realgame.Weaponry.Objectif;
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
	
	private float sizeRatio;
	private float puissance;

	public MissileLauncher(PVector loc, int tier) {
		super(loc, identity, tier);
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

	public void setUpgradeRatio(float ratio) {
		this.upgradeRatio = ratio;
		this.sizeRatio = tiersRatioSize[tier] * ratio * sizeRatioFactor;
		this.puissance = tiersPuissance[tier] * ratio;
		this.tRecharge = PApplet.round(tiersTRecharge[tier] * tRechargeRatio);
	}

	public boolean fire() {
		if (tempsRestant > 0) {
			indicateurErreur = Weaponry.tAffichageErreur;
			if (TangibleGame.verbosity > 2)
				System.out.println("encore "+tempsRestant+" frame(s) !");
			return false;
		} else {
			tempsRestant = tRecharge;
			game.physic.add( new Missile(location(), rotation()) );
			return true;
		}
	}

	public class Missile extends Cube {

		public Missile(PVector location, Quaternion rotation) {
			super(location, rotation, vec(sizeRatio*2, sizeRatio*2, sizeRatio*7));
			setName("Missile");
			setMass(puissance);
			setParent(launcher());
			setParentRel(ParentRelationship.None);
			velocityRel.set(velocity());
		}

		public void display() {
			pushLocal();
			displayColliderMaybe();
			displayInteractionMaybe();
			app.scale(sizeRatio);
			app.shape(missile);
			popLocal();
		}

		/*public boolean doCollideFast(Collider col) {
			ret (col == launcher())
				return false;
			return super.doCollideFast(col);
		}*/

		protected void addForces() {
			avance( puissance*3 );
		}

		public void onCollision(Collider col, PVector impact) {
			game.physic.remove( this );
			game.physic.add( new Effect.Explosion( impact, 30 ) );

			//réaction objectif
			if (col instanceof Objectif) {
				((Objectif)col).damage(PApplet.round(puissance));
			}
		}
	}
}
