package cs211.tangiblegame;

import processing.event.MouseEvent;

public abstract class Interface extends ProMaster {
	
	/** Initialize all dynamic stuff */
	public abstract void init();
	
	/** On view change, when this interface is set. */
	public void onShow() {}

	/** On view change, when another interface is set. */
	public void onHide() {}
	
	/** To draw and update. */
	public abstract void draw();
	
	/** To draw gui (depth already ignored). */
	public void gui() {}
	
	// --- EVENTS ---
	
	public void mouseDragged() {}

	public void mouseWheel(MouseEvent event) {}

	public void keyReleased() {}

	public void keyPressed() {}  

	public void mousePressed() {}
	
	public void mouseReleased() {}
	
	// --- other ---
	
	/** Return the name of the parameters changed by scroll. If null scroll isn't used. */
	public String scrollUse() { return null; }
	
}
