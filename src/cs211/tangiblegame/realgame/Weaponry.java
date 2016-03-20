package cs211.tangiblegame.realgame;

import java.util.ArrayList;
import java.util.List;

import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.geo.Sphere;
import cs211.tangiblegame.physic.Object;
import processing.core.PApplet;
import processing.core.PVector;

public class Weaponry extends Object {
	protected final static int tAffichageErreur = 15;
	
	//private static final float ratioUpgradeMax = 2, ratioUpgradeMin = 0.4f, ratioUpgradeToExist = 0.2f;
	/** Threshold for the button input to fire a tier of weapon. */
	private static final float[] etatThreshold = new float[] { 0, 0, 0.8f };
	

	protected static final int guiWidth = 400;
	protected float guiRatio;
	
	private final List<Weapon> weapons = new ArrayList<>();
	/** In the order in which the weapons will be shot. */
	private final List<Weapon> weaponsOrdered = new ArrayList<>();
	
	public interface Armed {
		public Weaponry armement();
	}

	/** t0-2 : thresholds pour répartir la l'amélioration puissance sur les differents tiers d'armement. */
	public Weaponry(PVector loc, Quaternion rot) {
		super(loc, rot);
	}
	
	/*public static Armement ShipArmement(Object launcher) {
		Armement it = new Armement(zero, identity);
		float r = launcher.radiusEnveloppe();
		it.addMissileLauncher(vec(r*-0.65f, -10, 0), 1);
		it.addMissileLauncher(vec(r*-0.25f, -10, 0), 2);
		it.addMissileLauncher(vec(r*+0.25f, -10, 0), 2);
		it.addMissileLauncher(vec(r*+0.65f, -10, 0), 1);
		it.setParent(launcher);
		it.validate();
		return it;
		
	public void addMissileLauncher(PVector loc, int tier) {
		addWeapon(new MissileLauncher(loc, tier));
	}
	}*/

	public void validate() {
		float imagesWidth = 0;
		for (Weapon w : weapons)
			imagesWidth += w.img().width*w.upgradeRatio;
		guiRatio = Weaponry.guiWidth / imagesWidth;
	}
	
	protected void addWeapon(Weapon w) {
		weapons.add(w);
		weapons.sort((a,b) -> PApplet.round(a.puissance - b.puissance)); //TODO location.x
		weaponsOrdered.add(w);
		weaponsOrdered.sort((a,b) -> PApplet.round(a.puissance - b.puissance));
	}
	
	/** 
	 * idx: -2 -> any, -1 -> any after threshold, [0-nbSlot[ -> that one. 
	 * etat: filter weapons with etatThreshold. 
	 **/
	public void fire(int idx, float etat) {
		if (idx < 0) { //-2, -1
			for (Weapon w : weaponsOrdered) {
				if (w.ready() 
						&& (idx != -2 || etat > etatThreshold[w.tier]) 
						&& w.fire())
					return;
				}
		} else if (idx < weapons.size() && idx >= 0)
			weapons.get(idx).fire();
	}
	
	/**  idx: -1 -> any, [0-nbSlot] -> that one. */
	public void fire(int idx) {
		fire(idx, 1);
	}
	
	/** display the state of the missiles in the gui */
	public void displayGui() {
		PVector basGauche = new PVector((TangibleGame.width-guiWidth)/2, TangibleGame.height);
		for(Weapon w : weapons) {
			basGauche = w.displayGui(basGauche);
		}
	}

	
	/** A sphere to destroy. */
	public static class Objectif extends Sphere {
		
		public Objectif(PVector location, Quaternion rotation) {
			super(location, rotation, 50);
			setMass(30);
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
		
		public void onDeath() {
			game.debug.msg(2, this+" destroyed");
		}
	}
}
