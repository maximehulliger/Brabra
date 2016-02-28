package cs211.tangiblegame.realgame;

import java.util.ArrayList;
import java.util.List;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Cube;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.geo.Sphere;
import cs211.tangiblegame.physic.Collider;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;

public class Armement extends ProMaster {
	private static final int guiWidth = 400;
	private static final float ratioSizeMissile = 1f;
	private static final float ratioTRechargeMissile = 0.7f;
	private static final float ratioUpgradeMax = 1.5f; //ratio max d'amélioratio 
	private static final float ratioUpgradeMin = 0.4f; 
	private static final float ratioUpgradeToExist = 0.2f; // -> pour que l'armement existe
	private static final float[] tiersRatioSize = new float[] { 1, 8, 12.5f };
	private static final float[] tiersPuissance = new float[] { 10, 50, 200 };
	private static final float[] tiersTRecharge = new float[] { 3, 15, 30 };
	
	public static PShape missile;
	public static PImage missileImg;
	private final List<LanceMissile> lmissiles = new ArrayList<Armement.LanceMissile>(5);
	private final List<LanceMissile> lmissilesByPrioritiy = new ArrayList<Armement.LanceMissile>(5);
	private float ratioToImageScale;
	private final Collider launcher;
	private float[] ratioIn;
	
	public interface Armed {
		public Armement armement();
	}
	
	/** t0-2 : thresholds pour r�partir la l'am�lioration puissance sur les differents tiers d'armement. */
	public Armement(Collider launcher, float ratio0, float ratio1, float ratio2) {
		this.launcher = launcher;
		
		//get ratios relative to 1
		if (ratio0 < ratioUpgradeToExist) ratio0 = 0;
		if (ratio1 < ratioUpgradeToExist) ratio1 = 0;
		if (ratio2 < ratioUpgradeToExist) ratio2 = 0;
		float ratiotot = ratio0 + ratio1 + ratio2;
		ratioIn = PVector.div(vec(ratio0, ratio1, ratio2), ratiotot).array();
		float imagesWidth = missileImg.width * 2 * (ratioIn[1]*tiersRatioSize[1] + ratioIn[2]*tiersRatioSize[2]);
		ratioToImageScale = guiWidth / imagesWidth;
		
		boolean hasGatling = false; //ratioIn[0] > 0
		if (hasGatling) {
			//float ratioUpgrade0 = PApplet.map(ratio0, 0, ratiotot, ratioUpgradeMin, ratioUpgradeMax);
			
			//add laser gatling
			//lmissilesByPrioritiy.add()
		} 
		if (ratioIn[2] > 0) {
			float ratioUpgrade2 = PApplet.map(ratioIn[2], 0, 1, ratioUpgradeMin, ratioUpgradeMax) * ratioIn[2];
			
			LanceMissile leftBigMissLaunch = new LanceMissile(vec(launcher.radiusEnveloppe*0.25f, -10, 0), 2, ratioUpgrade2);
			LanceMissile rightBigMissLaunch = new LanceMissile(vec(launcher.radiusEnveloppe*-0.25f, -10, 0), 2, ratioUpgrade2);
			
			lmissiles.add( 0, leftBigMissLaunch);
			lmissiles.add( rightBigMissLaunch);
			lmissilesByPrioritiy.add( 0, leftBigMissLaunch);
			lmissilesByPrioritiy.add( 1, rightBigMissLaunch);
		} 
		if  (ratioIn[1] > 0) {
			float ratioUpgrade1 = PApplet.map(ratioIn[1], 0, 1, ratioUpgradeMin, ratioUpgradeMax) * ratioIn[1];
			
			LanceMissile leftSmallMissLaunch = new LanceMissile(vec(launcher.radiusEnveloppe*0.65f, -10, 0), 1, ratioUpgrade1);
			LanceMissile rightSmallMissLaunch = new LanceMissile(vec(launcher.radiusEnveloppe*-0.65f, -10, 0), 1, ratioUpgrade1);
			
			lmissiles.add( 0, leftSmallMissLaunch );
			lmissiles.add( rightSmallMissLaunch);
			int offset = ratio2 > 0 ? 2 : 0;
			lmissilesByPrioritiy.add( offset + 0, leftSmallMissLaunch);
			lmissilesByPrioritiy.add( offset + 1, rightSmallMissLaunch);
		}
	}
	
	// tire le premier missile disponible
	private static final float[] etatThreshold = new float[] { 0, 0.0f, 0.8f };
	public void fire(float etat) {
		for (LanceMissile lm : lmissilesByPrioritiy)
			if (lm.ready() && etat > etatThreshold[lm.tier]) {
				lm.fire();
				return;
			}
		//System.out.println("rien de pr�t, capitaine !");
	}

	public void fireFromSlot(int idx) {
		if (idx < lmissiles.size() && idx >= 0)
			lmissiles.get(idx).fire();
	}

	public void update() {
		for (LanceMissile lm : lmissiles)
			lm.update();
	}
	
	
	//affiche l'état des missiles dans la gui
	public void displayGui() {
		//app.fill(255, 255);
		//app.tint(255, 255); TODO
		PVector basGauche = new PVector((app.width-guiWidth)/2, app.height);
		for(LanceMissile lm : lmissiles) {
			basGauche = lm.displayGui(basGauche);
		}
	}

	public class LanceMissile {
		private final static int tAffichageErreur = 15;
		private final PVector loc;
		private final int tier;
		private final int puissance;
		private final int tRecharge;
		public int tempsRestant;
		private int indicateurErreur = 0;

		public LanceMissile(PVector loc, int tier, float ratioUpgrade) {
			this.loc = loc;
			this.tier = tier;
			this.puissance = PApplet.round(tiersPuissance[tier] * ratioUpgrade);
			this.tRecharge = PApplet.round(tiersTRecharge[tier] * ratioTRechargeMissile / ratioUpgrade);
			tempsRestant = tRecharge;
		}

		public void update() {
			if (tempsRestant > 0)
				tempsRestant--;
			if (indicateurErreur > 0)
				indicateurErreur--;
		}
		
		public boolean ready() { return tempsRestant == 0; }

		public void fire() {
			if (tempsRestant > 0) {
				indicateurErreur = tAffichageErreur;
				System.out.println("encore "+tempsRestant+" frame(s) !");
			} else {
				tempsRestant = tRecharge;
				Missile m = new Missile(launcher.absolute(loc), launcher.rotation, puissance);
				ProMaster.app.intRealGame.physic.toAdd.add( m );
			}
		}

		//retourne le nouveau point bas gauche
		public PVector displayGui(PVector basGauche) {
			app.noStroke();
			PVector imgDim = new PVector(missileImg.width, missileImg.height);
			imgDim.mult(tiersRatioSize[tier] * ratioIn[tier] * ratioToImageScale);  //TODO
			app.image(missileImg, basGauche.x, basGauche.y-imgDim.y, imgDim.x, imgDim.y);
			if (tempsRestant > 0) {
				app.fill(255, 0, 0, 70);
				float hauteur = imgDim.y*(1f-((float)tempsRestant)/tRecharge);
				app.rect(basGauche.x, basGauche.y-hauteur, imgDim.x, hauteur);
				if (indicateurErreur > 0) {
					app.fill(255, 0, 0, 150*(((float)indicateurErreur)/tAffichageErreur));
					app.rect(basGauche.x, basGauche.y-imgDim.y, imgDim.x, imgDim.y);
				}
			} else {
				app.fill(90, 255, 90, 70);
				app.rect(basGauche.x, basGauche.y-imgDim.y, imgDim.x, imgDim.y);
			}
			return new PVector(basGauche.x+imgDim.x, basGauche.y);
		}
		
		public class Missile extends Cube {
			
			public Missile(PVector location, Quaternion rotation, int puissance) {
				super(location, rotation, puissance, 
						vec( tiersRatioSize[tier]*2*ratioSizeMissile, tiersRatioSize[tier]*2*ratioSizeMissile, tiersRatioSize[tier]*7*ratioSizeMissile) );
				assert(tier > 0);
				velocity.set(launcher.velocity);
			}
			
			public void update() {
				super.update();

			}
	
			public void display() {
				pushLocal();
				app.scale( tiersRatioSize[tier] * ratioSizeMissile );
				app.shape(missile);
				popLocal();
				if (drawCollider) {
					app.fill(255, 0, 0, 100);
					super.display();
				}
			}
	
			public boolean doCollideFast(Collider col) {
				if (col == launcher)
					return false;
				if(col instanceof Missile && ((Missile)col).launcher() == launcher) //cousin
					return false;
				return super.doCollideFast(col);
			}
			
			private Collider launcher() {
				return launcher;
			}
	
			protected void addForces() {
				avance( puissance*3 );
			}
	
			public void onCollision(Collider col, PVector impact) {
				app.intRealGame.physic.toRemove.add( this );
				app.intRealGame.physic.effectsToAdd.add( new Effect.Explosion( impact, 30 ) );
	
				//réaction objectif
				if (col instanceof Objectif) {
					((Objectif)col).damage(puissance);
				}
			}
		}
	}
	
	// une sphère à détruire
	public static class Objectif extends Sphere {
		
		public Objectif(PVector location) {
			super(location, 30, 50);
			setName("Objectif");
		}
		
		public String toString() {
			return super.toString()+" ("+life()+")";
		}

		public void display() {
			app.fill(255, 255, 0);
			super.display();
		}

		public void addForces() {
			freine(0.07f);
		}

		public void damage(int damage) {
			life -= damage;
			if (life < 0) {
				app.intRealGame.physic.toRemove.add(this);
				System.out.println("détruit !");
			}
		}
	}
}
