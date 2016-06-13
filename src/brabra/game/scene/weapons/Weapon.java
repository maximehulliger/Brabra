package brabra.game.scene.weapons;

import brabra.Debug;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;
import processing.core.PApplet;
import processing.core.PImage;

/** 
 * Represent a weapon that can fire things.
 * It can be in the scene or [not in the scene and with a master].
 * On validate, the weapon add itself to the first parent weaponry if it finds one (xml case). 
 **/
public abstract class Weapon extends Object {
	
	private static final float reloadingTimeRatio = 1f;
	private static final float[] tiersGuiRatio = new float[] { 1, 2, 3.5f };
	private static final int tierMaxWeapon = tiersGuiRatio.length;
	private final int tierMax;
	
	/** Tier in [0,tierMax[ */
	private int tier = 0;
	private float upgradeRatio = 1;
	
	private float puissance = 1;
	private int tRecharge = 1;
	private int indicateurErreur = 0;
	private int tempsRestant = 0;
	private Weaponry master;
	private boolean displayColliders = false;
	
	public Weapon(Vector loc, Quaternion rot, int tierMax) {
		super(loc, rot);
		this.tierMax = min(tierMax, tierMaxWeapon);
	}
	
	// --- Setters ---
	
	/** Set tier & update state. Tier should be in [1, tierMax]. */
	public void setTier(int tier) {
		if (tier < 1 || tier > tierMax) {
			final int newTier = PApplet.constrain(tier, 1, tierMax);
			Debug.err("tier should be in [1,"+tierMax+"] ("+tier+"), taking "+newTier+".");
			tier = newTier;
		}
		this.tier = tier-1;
	}
	
	public Weapon withTier(int tier) {
		setTier(tier);
		return this;
	}

	public void setUpgradeRatio(float ratio) {
		this.upgradeRatio = ratio;
	}
	
	protected void set(float tRecharge, float puissance) {
		this.tRecharge = round(tRecharge * reloadingTimeRatio);
		this.puissance = puissance;
	}

	protected void setMaster(Weaponry master) {
		this.master = master;
	}
	
	// --- Getters ---

	/** Return the tier of the weapon in [1, tierMax]. */
	public int tier() {
		return tier+1;
	}

	/** Return true if the weapon is ready to fire. */
	public boolean ready() {
		return tempsRestant == 0; 
	}
	
	/** Return the image that should be displayed */
	protected abstract PImage img();
	
	protected int puissance() {
		return round(puissance * upgradeRatio * (master!=null ? master.puissanceRatio : 1));
	}

	protected float imgWidth() {
		return img().width * tiersGuiRatio[tier] * (master!=null ? master.guiRatio : 1);
	}

	protected int tier0() {
		return tier;
	}

	protected Weaponry master() {
		return master;
	}

	/** Return true if the colliders of the projectiles should be displayed. */
	protected boolean displayColliders() {
		return displayColliders || (master != null && master.displayColliders);
	}

	// --- Life cycle: fire, validate, update & gui ---

	/** Try to shoot from this weapon. Return true if success. */
	public boolean fire() {
		if (tempsRestant > 0) {
			indicateurErreur = Weaponry.tAffichageErreur;
			return false;
		} else {
			tempsRestant = tRecharge;
			return true;
		}
	}
	
	/** Takes: tier, displayColliders & check for master. */
	public void validate(Attributes atts) {
		super.validate(atts);
		String tier = atts.getValue("tier");
		setTier(tier==null ? 1 : Integer.parseInt(tier));
		final String displayColliders = atts.getValue("displayColliders");
		if (displayColliders != null)
			this.displayColliders = Boolean.parseBoolean(displayColliders);
		// validate the master weaponry.
		Weaponry newMaster = (Weaponry)parentThat(p -> p instanceof Weaponry);
		if (newMaster != master) {
			if (master != null)
				master.removeWeapon(this);
			master = newMaster;
			if (newMaster != null)
				newMaster.addWeapon(this);
		}
	}
	
	public void update() {
		super.update();
		if (tempsRestant > 0)
			tempsRestant--;
		if (indicateurErreur > 0)
			indicateurErreur--;
	}

	/** Return the new left down point (right down of this' img). */
	public Vector displayGui(final Vector basGauche) {
		app.noStroke();
		Vector imgDim = vec(img().width, img().height);
		imgDim.mult(imgWidth() / imgDim.x);
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
