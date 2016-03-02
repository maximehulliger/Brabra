package cs211.tangiblegame;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Input extends ProMaster {
	public static final int scrollMin = 4, scrollMax = 100, scrollRange = scrollMax - scrollMin;
	
	/** in pixel/frame. */
	public final PVector deplMouse = zero.copy();
	/** true if space is down. */
	public boolean spaceDown = false;
	/** input axis (wasd) ~[-1, 1] */
	public float horizontal = 0, vertical = 0;
	/** [0, 1] */
	public float scroll = 1f/4;
	/** [scrollMin, scrollMax] */
	public int scrollDiff = 0;
	/** indicate the need to fire during the last frame */
	public boolean fire = false;
	/** fireSlot [-1-3] -1: any */
	public int fireSlot = -1;
	
	private int scrollAcc = (int)(scrollMax*scroll);
	private int scrollDiffAcc = 0;
	private final PVector deplMouseAcc = zero.copy();
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
		// depl mouse
		if (!deplMouseAcc.equals(zero)) {
			deplMouse.set(deplMouseAcc);
			deplMouseAcc.set(zero);
		} else
			deplMouse.set(zero);
		// fire
		fire = fireAcc;
		fireAcc = false;
		fireSlot = fireSlotAcc;
		fireSlotAcc = -1;
	}
	
	// --- event gesture ---
	
	public void keyPressed() {
		if (app.key == ' ')		spaceDown = true;
		if (app.key == 'w') 	vertical += 1;
		if (app.key == 's') 	vertical -= 1;
		if (app.key == 'a')		horizontal -= 1;
		if (app.key == 'd')		horizontal += 1;
	}

	public void keyReleased() {
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
		int diffX = app.mouseX-app.pmouseX;
		int diffY = app.mouseY-app.pmouseY;
		deplMouseAcc.add( vec(diffX, diffY) );
	}

	public void mouseWheel(MouseEvent event) {
		scrollDiffAcc = event.getCount();
	}
}
