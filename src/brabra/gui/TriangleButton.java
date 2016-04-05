package brabra.gui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class TriangleButton extends Polygon{
	
	private double len;
	private boolean clicked = false;
	
	public TriangleButton(double i) {
		this.len = i;
	    this.getPoints().setAll(0d, len, 2*len-2, 0d, 0d, -len);
	    this.setFill(Color.GREY);
	}
	
	public void changeTriangle() {
	    if (clicked){
	    	this.getPoints().setAll(
	    	        0d, len,
	    	        2*len-2, 0d,
	    	        0d, -len
	    	    );
	    	this.clicked = false;
	    }else{
	    	this.getPoints().setAll(
	    			len, 0d,
	    			0d, 2*(len-1),
	    			-len, 0d
	    		);
	    	this.clicked = true;
	    }
	  }
}