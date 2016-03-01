package cs211.tangiblegame;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Input extends ProMaster {
	private static final int scrollMin = 4, scrollMax = 100;
	public final PVector deplMouse = zero.copy();

	public boolean spaceDown = false;
	public float horizontal = 0, vertical = 0;
	/** [0, 1] */
	public float scroll = 1f/4;
	public int scrollAcc = (int)(scrollMax*scroll), scrollDiff = 0;
	private int scrollDiffAcc = 0;
	
	public void update() {
		if (scrollDiffAcc != 0) {
			int oldScrollAcc = scrollAcc;
			scroll = PApplet.constrain(scrollAcc+scrollDiffAcc, scrollMin, scrollMax);
			scrollDiffAcc = 0;
			scrollDiff = scrollAcc-oldScrollAcc;
			scroll = clamp(scrollAcc, scrollMin, scrollMax);
		}
	}
	
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
	}

	public void mouseDragged() {
		int diffX = app.mouseX-app.pmouseX;
		int diffY = app.mouseY-app.pmouseY;
		deplMouse.add( vec(-diffY, -diffX) );
	}

	public void mouseWheel(MouseEvent event) {
		scrollDiffAcc -= event.getCount();
	}
}
