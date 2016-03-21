package brabra.game.scene.weapons;

import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.geo.Quaternion;
import brabra.game.scene.Object;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/** 
 * Represent a weapon that can fire things.
 * on validate, the weapon add itself to the first parent weaponry. */
public abstract class Weapon extends Object {
	
	protected static final float[] tiersRatioSize = new float[] { 0.8f, 1, 1.2f };
	protected static final int tierMax = 3;
	
	/** Tier in [0,tierMax[ */
	protected int tier = 0;
	protected int indicateurErreur = 0;
	protected int tempsRestant = 0;
	
	// to set on state update:
	protected float puissance = 1;
	protected float upgradeRatio = 1;
	protected int tRecharge = 1;
	protected float sizeRatio = 0;
		
	protected Weaponry master;
	protected boolean displayColliders = false;
	
	public Weapon(PVector loc, Quaternion rot) {
		super(loc, rot);
	}
	
	public void validate(Attributes atts) {
		super.validate(atts);
		String tier = atts.getValue("tier");
		setTier(tier==null ? 1 : Integer.parseInt(tier));

		final String displayColliders = atts.getValue("displayColliders");
		if (displayColliders != null) {
			this.displayColliders = Boolean.parseBoolean(displayColliders);
		}
		// Validate the master weaponry.
		Weaponry newMaster = (Weaponry)parentThat(p -> p instanceof Weaponry);
		if (newMaster != master) {
			if (master != null)
				master.removeWeapon(this);
			if (newMaster != null)
				newMaster.addWeapon(this);
			master = newMaster;
		}
	}
	
	/** Return true if success. */
	public abstract boolean fire();

	// --- setters ---
	
	public void setTier(int tier) {
		if (tier < 1 || tier > tierMax) {
			final int newTier = PApplet.constrain(tier, 1, tierMax);
			debug.err("tier should be in [1,"+tierMax+"] ("+tier+"), taking "+newTier+".");
			tier = newTier;
		}
		this.tier = tier-1;
	}

	public void setUpgradeRatio(float ratio) {
		this.upgradeRatio = ratio;		
	}
	
	// --- getters ---
	
	public abstract PImage img();

	public boolean ready() { 
		return tempsRestant == 0; 
	}

	protected Weaponry launcher() {
		return master;
	}
	
	/** Return the tier of the weapon in [1, tierMax]. */
	protected int tier() {
		return tier+1;
	}
	
	// --- update & gui ---
	
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

	/** Retourne le nouveau point bas gauche. */
	public PVector displayGui(final PVector basGauche) {
		app.noStroke();
		PVector imgDim = vec(img().width, img().height);
		imgDim.mult(upgradeRatio*master.guiRatio*Weapon.tiersRatioSize[tier]);
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
}
