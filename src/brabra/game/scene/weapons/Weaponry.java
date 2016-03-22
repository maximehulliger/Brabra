package brabra.game.scene.weapons;

import java.util.ArrayList;
import java.util.List;

import brabra.Brabra;
import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.geo.Quaternion;
import brabra.game.scene.Object;
import processing.core.PVector;

/** Class holding some weapons (in the children or not). */
public class Weaponry extends Object {
	
	//private static final float ratioUpgradeMax = 2, ratioUpgradeMin = 0.4f, ratioUpgradeToExist = 0.2f;
	protected final static int tAffichageErreur = 15;
	/** Threshold for the button input (from the plate) to fire a tier of weapon. */
	private static final float[] etatThreshold = new float[] { 0, 0, 0.8f };
	private static final int nbPrefab = 2;
	private int guiWidthWished = 400, puissanceWished = 400;
	
	// for the weapons
	protected float guiRatio = 1;
	protected boolean displayColliders = false;
	protected float puissanceRatio = 1;
	
	// intern
	private final List<Weapon> weapons = new ArrayList<>();
	/** in the order in which the weapons will be shot. */
	private final List<Weapon> weaponsOrdered = new ArrayList<>(); 
	private final PVector basGauche = zero.copy();
	private boolean valid = true; // object is in a valid state.
	private int guiWidth = guiWidthWished;
	private float puissance = puissanceWished;
	private int prefab = 0;
	
	/** t0-2 : thresholds pour r�partir la l'am�lioration puissance sur les differents tiers d'armement. */
	public Weaponry(PVector loc, Quaternion rot) {
		super(loc, rot);
		setName("Weaponry");
	}
	
	// --- Modifiers ---
	
	/** 
	 * Add a weapon to this weaponry. 
	 * If w is validated then his master(me) should already be set,
	 * otherwise I'll set him myself. 
	 **/
	protected void addWeapon(Weapon w) {
		if (w.validated())
			assert(w.master() == this);
		else {
			w.setParent(this);
			w.setMaster(this);
		}
		weapons.add(w);
		valid = false;
	}

	/** Remove this weapon from this weaponry. */
	protected void removeWeapon(Weapon w) {
		weapons.remove(w);
		valid = false;
	}
	
	public void setDisplayColliders(boolean displayColliders) {
		this.displayColliders = displayColliders;
	}
	
	public void setPuissance(float p) {
		this.puissance = p;
	}
	
	// --- Usage ---
	
	/** 
	 * idx: -2 -> all, -1 -> any after threshold, [0-nbSlot[ -> that one. 
	 * etat: filter weapons with etatThreshold. 
	 **/
	public void fire(int idx, float etat) {
		if (idx == -2)
			for (Weapon w : weaponsOrdered)
				w.fire();
		else if (idx == -1)
			for (Weapon w : weaponsOrdered)
				if (etat >= etatThreshold[w.tier()-1] && w.fire())
					return;
		else if (idx < weapons.size() && idx >= 0)
			weapons.get(idx).fire();
	}
	
	/**  idx: -2: all, -1 -> any, [0-nbSlot] -> that one. */
	public void fire(int idx) {
		fire(idx, 1);
	}
	
	/** display the state of the missiles in the gui */
	public void displayGui() {
		updateState();
		PVector basGaucheCurrent = basGauche.copy();
		for(Weapon w : weapons)
			basGaucheCurrent = w.displayGui(basGaucheCurrent);
	}
	
	// --- life cycle ---
	
	public void validate(Attributes atts) {
		super.validate(atts);
		// display colliders & puissance
		final String displayColliders = atts.getValue("displayColliders");
		if (displayColliders != null)
			setDisplayColliders(Boolean.parseBoolean(displayColliders));
		final String puissance = atts.getValue("puissance");
		if (puissance != null)
			setPuissance(Float.parseFloat(puissance));
		// prefab
		final String prefabString = atts.getValue("prefab");
		if (prefabString != null && !prefabString.equals("not")) {
			prefab = Integer.parseInt(prefabString);
			if (prefab != 0) {
				if (prefab < 1 || prefab > nbPrefab) {
					int newPrefab = constrain(prefab, 1, nbPrefab);
					game.debug.err("prefab for weaponry should be in [1,"+nbPrefab+"] "
							+ "("+prefab+") (0/not for nothing), taking "+newPrefab);
					prefab = newPrefab;
				}
				if (weapons.size() > 0) {
					game.debug.err("weaponry should not have weapons when prefab is set. removing them.");
					for (Weapon w : weapons)
						game.scene.remove(w);
					weapons.clear();
				}
				setName("Weaponry prefab "+prefab);
				final float r = parent().radiusEnveloppe();
				if (prefab == 1) {
					addWeapon(new MissileLauncher(vec(r*-0.65f, -10, 0), null).withTier(1));
					addWeapon(new MissileLauncher(vec(r*-0.25f, -10, 0), null).withTier(2));
					addWeapon(new MissileLauncher(vec(r*+0.25f, -10, 0), null).withTier(2));
					addWeapon(new MissileLauncher(vec(r*+0.65f, -10, 0), null).withTier(1));
				} else if (prefab == 2) {
					addWeapon(new MissileLauncher(vec(r*-0.55f, -10, 0), null).withTier(1));
					addWeapon(new MissileLauncher(vec(r*+0.55f, -10, 0), null).withTier(1));
					addWeapon(new MissileLauncher(vec(r*-0.30f, -5,  0), null).withTier(1));
					addWeapon(new MissileLauncher(vec(r*+0.30f, -5,  0), null).withTier(1));
					addWeapon(new MissileLauncher(vec(r*-0.20f, -15, 0), null).withTier(1));
					addWeapon(new MissileLauncher(vec(r*+0.20f, -15, 0), null).withTier(1));
				} else
					assert(false);
			}
		}
	}
	
	protected boolean update() {
		if (super.update()) {
			// we make sure all the weapons are updated (in case they're not in the scene).
			for (Weapon w : weapons)
				if (!w.inScene())
					w.forceUpdate();
			return true;
		} else
			return false;
	}
	
	// --- private ---
	
	private void updateState() {
		if (!valid) {
			// gui + puissance
			float guiWidthWished = 0, puissanceWished = 0;
			for (Weapon w : weapons) {
				assert(w.validated() || w.parent()==this);
				guiWidthWished += w.img().width*Weapon.tiersRatioSize[w.tier()-1];
				puissanceWished += w.puissance;
			}
			guiWidth = round(min(guiWidthWished, this.guiWidthWished));
			guiRatio = guiWidth / guiWidthWished;
			puissance = min(puissanceWished, this.puissanceWished);
			puissanceRatio = puissance / puissanceWished;
			basGauche.set((Brabra.width-guiWidth)/2, Brabra.height);
			// weapons
			weapons.sort((a,b) -> round(b.locationRel().x - a.locationRel().x));
			weaponsOrdered.clear();
			weaponsOrdered.addAll(weapons);
			weaponsOrdered.sort((a,b) -> {
				int score = round(a.puissance - b.puissance);
				if (score == 0)
					score = round(abs(a.locationRel().x) - abs(b.locationRel().x));
				return score;
			});
			valid = true;
		}
	}
}
