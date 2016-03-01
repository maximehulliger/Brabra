package cs211.tangiblegame;

import processing.event.MouseEvent;

public abstract class Interface extends ProMaster {
	
	/** Init all dynamic stuff */
	public abstract void init();
	
	/** On view change */
	public abstract void wakeUp();
	
	public abstract void draw();
	
	public void gui() {}
	
	// --- EVENTS ---
	
	public void mouseDragged() {}

	public void mouseWheel(MouseEvent event) {}

	public void keyReleased() {}

	public void keyPressed() {}  

	public void mousePressed() {}
	
	public void mouseReleased() {}
	
	// --- other ---
	
	public String scrollUse() { return null; }
	
}
