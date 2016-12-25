package brabra.game.scene.weapons;

import java.util.ArrayList;
import java.util.List;

import brabra.Brabra;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;

/** Class holding some weapons (in the children or not). */
public class Weaponry extends Object {

	//private static final float ratioUpgradeMax = 2, ratioUpgradeMin = 0.4f, ratioUpgradeToExist = 0.2f;
	protected final static int tAffichageErreur = 15;
	/** Threshold for the button input (from the plate) to fire a tier of weapon. */
	private static final float[] etatThreshold = new float[] { 0, 0, 0.8f };
	private int guiWidthWished = 150/*TODO: 400*/, puissanceWished = 400;

	// for the weapons
	protected float guiRatio = 1;
	protected boolean displayColliders = false;
	protected float puissanceRatio = 1;

	// intern
	private final List<Weapon> weapons = new ArrayList<>();
	/** in the order in which the weapons will be shot. */
	private final List<Weapon> weaponsOrdered = new ArrayList<>(); 
	private final Vector basGauche = zero.copy();
	private boolean valid = true; // object is in a valid state.
	private int guiWidth = guiWidthWished;
	private float puissance = puissanceWished;

	/** t0-2 : thresholds pour répartir la l'amélioration puissance sur les differents tiers d'armement. */
	public Weaponry() {
		setName("Weaponry");
	}
	
	private Object parent = null;
	
	public Object parent() {
		return parent;
	}
	
	// --- Modifiers ---

	/** 
	 * Add a weapon to this weaponry. 
	 * If w is validated then his master(me) should already be set,
	 * otherwise I'll set him myself. 
	 **/
	protected void addWeapon(Weapon w) {
		w.setMaster(this);
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
		Vector basGaucheCurrent = basGauche.copy();
		for(Weapon w : weapons)
			basGaucheCurrent = w.displayGui(basGaucheCurrent);
	}

	// --- life cycle ---

	public void validate(Attributes atts) {
		super.validate(atts);
		
		parent = atts.parent();
		
		app.game.physicInteraction.addWeaponry(this);
		app.game.physicInteraction.updateWeaponry();
		
		// display colliders & puissance
		final String displayColliders = atts.getValue("displayColliders");
		if (displayColliders != null)
			setDisplayColliders(Boolean.parseBoolean(displayColliders));
		
		final String puissance = atts.getValue("puissance");
		if (puissance != null)
			setPuissance(Float.parseFloat(puissance));
	}

	// --- private ---

	private void updateState() {
		if (!valid) {
			// gui + puissance
			float guiWidthWished = 0, puissanceWished = 0;
			guiRatio = 1;
			puissanceRatio = 1;
			for (Weapon w : weapons) {
				assert(w.master()==this);
				guiWidthWished += w.imgWidth();
				puissanceWished += w.puissance();
			}
			guiWidth = round(min(guiWidthWished, this.guiWidthWished));
			guiRatio = guiWidth / guiWidthWished;
			puissance = min(puissanceWished, this.puissanceWished);
			puissanceRatio = puissance / puissanceWished;
			basGauche.set((Brabra.width-guiWidth)/2, Brabra.height);
			// weapons
			weapons.sort((a,b) -> round(b.position.x - a.position.x));
			weaponsOrdered.clear();
			weaponsOrdered.addAll(weapons);
			weaponsOrdered.sort((a,b) -> {
				int score = round(a.puissance() - b.puissance());
				return score == 0 ? round(abs(a.position.x) - abs(b.position.x)) : score;
			});
			valid = true;
		}
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		
	}
}
