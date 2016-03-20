package brabra.game.scene.weapons;

import brabra.game.physic.geo.Quaternion;
import brabra.game.scene.Object;
import processing.core.PImage;
import processing.core.PVector;

/** 
 * Represent a weapon that can fire things.
 * on validate, the weapon add itself to the first parent weaponry. */
public abstract class Weapon extends Object {
	
	protected int tRecharge = 1;
	protected int tempsRestant = 0;
	protected int indicateurErreur = 0;
	protected float upgradeRatio = 1;
	protected float puissance = 1;
	
	protected Weaponry master;
	protected final int tier;
	
	public Weapon(PVector loc, Quaternion rot, int tier) {
		super(loc, rot);
		assert(tier > 0);
		this.tier = tier;
	}
	
	/** Validate the master weaponry. */
	public void validate() {
		super.validate();
		master = (Weaponry)parentThat(p -> p instanceof Weaponry);
		if (master != null)
			master.addWeapon(this);
	}
	
	/** Return true if success. */
	public abstract boolean fire();

	/** Retourne le nouveau point bas gauche. */
	public PVector displayGui(PVector basGauche) {
		app.noStroke();
		PVector imgDim = vec(img().width, img().height);
		imgDim.mult(upgradeRatio*master.guiRatio);
		app.image(img(), basGauche.x, basGauche.y-imgDim.y, imgDim.x, imgDim.y);
		if (tempsRestant > 0) {
			app.fill(255, 0, 0, 70);
			float hauteur = imgDim.y*(1f-((float)tempsRestant)/tRecharge);
			app.rect(basGauche.x, basGauche.y-hauteur, imgDim.x, hauteur);
			if (indicateurErreur > 0) {
				app.fill(255, 0, 0, 150*(((float)indicateurErreur)/Weaponry.tAffichageErreur));
				app.rect(basGauche.x, basGauche.y-imgDim.y, imgDim.x, imgDim.y);
			}
		} else {
			app.fill(90, 255, 90, 70);
			app.rect(basGauche.x, basGauche.y-imgDim.y, imgDim.x, imgDim.y);
		}
		return vec(basGauche.x+imgDim.x, basGauche.y);
	}
	
	// --- getters ---
	
	public abstract PImage img();

	public boolean ready() { 
		return tempsRestant == 0; 
	}

	public void setUpgradeRatio(float ratio) {
		this.upgradeRatio = ratio;
	}

	protected Weaponry launcher() {
		return master;
	}
	
	// --- update ---
	
	public boolean update() {
		if (super.update()) {
			if (tempsRestant > 0)
				tempsRestant--;
			if (indicateurErreur > 0)
				indicateurErreur--;
			return true;
		} else
			return false;
	}
}
