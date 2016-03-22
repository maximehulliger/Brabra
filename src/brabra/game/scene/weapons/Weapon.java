package brabra.game.scene.weapons;

import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.geo.Quaternion;
import brabra.game.scene.Object;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/** 
 * Represent a weapon that can fire things.
 * It can be in the scene or [not in the scene and with a master].
 * On validate, the weapon add itself to the first parent weaponry (xml case). 
 **/
public abstract class Weapon extends Object {
	
	protected static final float[] tiersRatioSize = new float[] { 0.8f, 1, 1.2f };
	protected static final int tierMax = 3;
	
	// handled variable.
	/** Tier in [0,tierMax[ */
	protected int tier = 0;
	protected int indicateurErreur = 0;
	protected int tempsRestant = 0;
	protected float puissanceRatio = 1, puissance = 1;
	
	// unhandled variables: to set on state update (updateState()).
	protected float upgradeRatio = 1;
	protected int tRecharge = 1;
	protected float sizeRatio = 0;
	/** Pure weapon puissance without upgradeRatio or puissanceRatio. */
	protected float puissanceBase;
	
	private Weaponry master;
	private boolean displayColliders = false;
	
	public Weapon(PVector loc, Quaternion rot) {
		super(loc, rot);
	}
	
	/** Try to shoot from this weapon. Return true if success. */
	public abstract boolean fire();
	
	/** To override to set global weapons variable. */
	protected abstract void updateState();

	// --- Setters ---
	
	/** Set tier & update state. Tier should be in [1, tierMax]. */
	public void setTier(int tier) {
		if (tier < 1 || tier > tierMax) {
			final int newTier = PApplet.constrain(tier, 1, tierMax);
			debug.err("tier should be in [1,"+tierMax+"] ("+tier+"), taking "+newTier+".");
			tier = newTier;
		}
		this.tier = tier-1;
		updateState();
	}
	
	public Weapon withTier(int tier) {
		setTier(tier);
		return this;
	}

	public void setUpgradeRatio(float ratio) {
		this.upgradeRatio = ratio;
		updateState();
	}
	
	public void setPuissance(float puissanceRatio) {
		this.puissanceRatio = puissanceRatio;
		puissance = puissanceBase * upgradeRatio 
				* puissanceRatio * (master==null ? 1 : master.puissanceRatio);
	}
	
	
	protected void setMaster(Weaponry master) {
		this.master = master;
		setPuissance(puissanceRatio);
		updateState();
	}
	
	// --- Getters ---
	
	protected abstract PImage img();

	/** Return true if the weapon is ready to fire. */
	public boolean ready() {
		return tempsRestant == 0; 
	}

	/** Return the tier of the weapon in [1, tierMax]. */
	protected int tier() {
		return tier+1;
	}

	protected Weaponry master() {
		return master;
	}

	/** Return true if the colliders of the projectiles should be displayed. */
	protected boolean displayColliders() {
		return displayColliders || (master != null && master.displayColliders);
	}
	
	// --- Life cycle: validate, update & gui ---

	/** Takes: tier, displayColliders & check for master. */
	public void validate(Attributes atts) {
		super.validate(atts);
		String tier = atts.getValue("tier");
		setTier(tier==null ? 1 : Integer.parseInt(tier));
		final String displayColliders = atts.getValue("displayColliders");
		if (displayColliders != null)
			this.displayColliders = Boolean.parseBoolean(displayColliders);
		final String puissance = atts.getValue("puissance");
		if (puissance != null)
			this.puissanceRatio = Float.parseFloat(puissance);
		// validate the master weaponry.
		Weaponry newMaster = (Weaponry)parentThat(p -> p instanceof Weaponry);
		if (newMaster != master) {
			if (master != null)
				master.removeWeapon(this);
			master = newMaster;
			if (newMaster != null)
				newMaster.addWeapon(this);
		}
		// validate it
		updateState();
	}
	
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

	/** Return the new left down point (right down of this' img). */
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
