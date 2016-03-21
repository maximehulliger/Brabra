package brabra.game.scene.weapons;

import java.util.ArrayList;
import java.util.List;

import brabra.Brabra;
import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.geo.Quaternion;
import brabra.game.scene.Object;
import processing.core.PVector;

public class Weaponry extends Object {
	protected final static int tAffichageErreur = 15;
	
	//private static final float ratioUpgradeMax = 2, ratioUpgradeMin = 0.4f, ratioUpgradeToExist = 0.2f;
	/** Threshold for the button input (from the plate) to fire a tier of weapon. */
	private static final float[] etatThreshold = new float[] { 0, 0, 0.8f };
	private static final int guiWidthMax = 400;
	private int guiWidth = guiWidthMax;
	private final PVector basGauche = zero.copy();
	private boolean valid = false; // object is in a valid state
	
	protected float guiRatio = 1;
	protected boolean displayColliders = true;
	
	private final List<Weapon> weapons = new ArrayList<>();
	/** In the order in which the weapons will be shot. */
	private final List<Weapon> weaponsOrdered = new ArrayList<>();
	
	/** t0-2 : thresholds pour répartir la l'amélioration puissance sur les differents tiers d'armement. */
	public Weaponry(PVector loc, Quaternion rot) {
		super(loc, rot);
		setName("Weaponry");
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

	/** */
	protected void addWeapon(Weapon w) {
		weapons.add(w);
		weaponsOrdered.add(w);
		valid = false;
	}

	protected void removeWeapon(Weapon w) {
		weapons.remove(w);
		weaponsOrdered.remove(w);
		valid = false;
	}
	
	public void setDisplayColliders(boolean displayColliders) {
		this.displayColliders = displayColliders;
	}
	
	/** 
	 * idx: -2 -> any, -1 -> any after threshold, [0-nbSlot[ -> that one. 
	 * etat: filter weapons with etatThreshold. 
	 **/
	public void fire(int idx, float etat) {
		if (idx < 0) { //-2, -1
			for (Weapon w : weaponsOrdered) {
				if (w.ready() 
						&& (idx != -2 || etat >= etatThreshold[w.tier()-1]) 
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
		if (!valid)
			validate();
		PVector basGaucheCurrent = basGauche.copy();
		for(Weapon w : weapons)
			basGaucheCurrent = w.displayGui(basGaucheCurrent);
	}
	
	public void validate(Attributes atts) {
		super.validate(atts);
		final String displayColliders = atts.getValue("displayColliders");
		if (displayColliders != null) {
			setDisplayColliders(Boolean.parseBoolean(displayColliders));
		}
		
	}
	
	// --- private ---
	
	private void validate() {
		// gui
		float imagesWishedWidth = 0;
		for (Weapon w : weapons)
			imagesWishedWidth += w.img().width*Weapon.tiersRatioSize[w.tier()-1];
		guiWidth = round(min(imagesWishedWidth, guiWidthMax));
		guiRatio = guiWidth / imagesWishedWidth;
		basGauche.set((Brabra.width-guiWidth)/2, Brabra.height);
		// weapons
		weapons.sort((a,b) -> round(b.locationRel().x - a.locationRel().x));
		weaponsOrdered.sort((a,b) -> round(a.puissance - b.puissance));
		valid = true;
	}
}
