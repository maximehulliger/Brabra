package brabra.gui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class TriangleButton extends Polygon {
	
	private final static double triangleSize = 8;
	
	public TriangleButton() {
		super.setFill(Color.GREY);
		setOpen(false);
	}

	public void setOpen(boolean open) {
		if (open)
			getPoints().setAll(triangleSize, 0d, 0d, 2*(triangleSize-1), -triangleSize, 0d);
		else
			getPoints().setAll(0d, triangleSize, 2*triangleSize-2, 0d, 0d, -triangleSize);
			
	}
}