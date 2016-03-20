package brabra.game;

import brabra.ProMaster;
import brabra.Brabra;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Input extends ProMaster {
	
	public static final int scrollMin = 4, scrollMax = 100, scrollRange = scrollMax - scrollMin;
	public static final float dragDistMinSq, dragDistMax;
	static {
		float l = min(Brabra.height, Brabra.width);
		dragDistMinSq = sq(l/100);
		dragDistMax = l/3;
	}
	public static final Color dragIndicColor = new Color("white", true);
	
	/** In pixel/frame. */
	public final PVector mouseDragDelta = zero.copy();
	/** True when the mouse click is pressed (dragging) */
	public boolean dragging = false;
	/** True if the touch is down. */
	public boolean spaceDown = false, altDown = false, clickDown = false;
	/** Input axis (wasd) ~[-1, 1] */
	public float horizontal = 0, vertical = 0;
	/** [0, 1] */
	public float scroll = 1f/4;
	/** [scrollMin, scrollMax] */
	public int scrollDiff = 0;
	/** Indicate the need to fire during the last frame */
	public boolean fire = false;
	/** FireSlot [-1-3] -1: any */
	public int fireSlot = -1;
	
	/** The distance dragged with the mouse (since last click) in pixel. */
	private final PVector mouseDrag = zero.copy();
	private final PVector clickPos = zero.copy();
	private int scrollAcc = (int)(scrollMax*scroll);
	private int scrollDiffAcc = 0;
	private boolean fireAcc = false;
	private int fireSlotAcc = -1;
	
	public void update() {
		// scroll
		if (scrollDiffAcc != 0) {
			int oldScrollAcc = scrollAcc;
			scrollAcc = PApplet.constrain(scrollAcc+scrollDiffAcc, scrollMin, scrollMax);
			scrollDiffAcc = 0;
			scrollDiff = scrollAcc-oldScrollAcc;
			scroll = clamp(scrollAcc, scrollMin, scrollMax, true);
		} else
			scrollDiff = 0;
		// fire
		fire = fireAcc;
		fireAcc = false;
		fireSlot = fireSlotAcc;
		fireSlotAcc = -1;
	}
	
	public void gui() {
		if (dragging && app.useDrag()) {
			PVector drag = mouseDrag();
			if (!drag.equals(zero)) {
				line(clickPos, mousePos(), dragIndicColor);
			}
		}
	}
	
	public PVector mouseDrag() {
		return mouseDrag.magSq() > dragDistMinSq
			? new PVector().set(mouseDrag).limit(dragDistMax) : zero;
	}
	
	// --- event gesture ---
	
	public void keyPressed() {
		if (app.key == PApplet.ALT)	altDown = true;
		if (app.key == ' ')		spaceDown = true;
		if (app.key == 'w') 	vertical += 1;
		if (app.key == 's') 	vertical -= 1;
		if (app.key == 'a')		horizontal -= 1;
		if (app.key == 'd')		horizontal += 1;
	}

	public void keyReleased() {
		if (app.key == PApplet.ALT)	altDown = false;
		if (app.key == ' ')		spaceDown = false;
		if (app.key == 'w') 	vertical -= 1;
		if (app.key == 's') 	vertical += 1;
		if (app.key == 'a')		horizontal += 1;
		if (app.key == 'd')		horizontal -= 1;

		// armement
		if (app.key == 'e') {
			fireSlotAcc = -1;
			fireAcc = true;
		} else if (app.key >= '1' && app.key <= '5') {
			fireSlotAcc = app.key-'1';
			fireAcc = true;
		}
	}

	public void mouseDragged() {
		dragging = true;
		mouseDrag.add( vec(app.mouseX-app.pmouseX, app.mouseY-app.pmouseY) );
	}

	public void mouseWheel(MouseEvent event) {
		scrollDiffAcc += event.getCount();
	}

	public void mousePressed() {
		clickDown = true;
		clickPos.set(mousePos());
	}
	
	public PVector mousePos() {
		return new PVector(app.mouseX, app.mouseY);
	}

	void mouseReleased() {
		dragging = false;
		clickDown = false;
		mouseDrag.set(zero);
	}
}
