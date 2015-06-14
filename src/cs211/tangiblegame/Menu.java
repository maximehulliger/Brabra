package cs211.tangiblegame;

public class Menu extends Interface {

	public void init() {
		
	}

	public void draw() {
		app.fill(0);                       
		app.text("1: Real game", 200 ,200);
		app.text("2: Trivial game", 200 ,250);
		app.text("3: Calibration", 200 ,300);
			
	}
	
	public void keyPressed() {
		if (app.key == '1') {
			app.setInterface(app.intRealGame);
			app.setInterface(app.intTrivialGame);
			app.setInterface(app.intCalibration);
		}
	}

}
