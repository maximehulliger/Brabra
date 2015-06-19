package cs211.tangiblegame;

import processing.event.MouseEvent;

public abstract class Interface extends ProMaster {
	
	public abstract void init();
	
	public abstract void wakeUp();
	
	public abstract void draw();
	
	//------- EVENTS
	
	public void mouseDragged() {}

	public void mouseWheel(MouseEvent event) {}

	public void keyReleased() {}

	public void keyPressed() {}  

	public void mousePressed() {}
	
	public void mouseReleased() {}
}
