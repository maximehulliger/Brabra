package brabra.gui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class TriangleButton extends Polygon {
	
	private final static double triangleSize = 8;
	
	public TriangleButton() {
		super.getPoints().setAll(0d, triangleSize, 2*triangleSize-2, 0d, 0d, -triangleSize);
		super.setFill(Color.GREY);
	}

	public void setState(boolean open) {
		if (open)
			this.getPoints().setAll(0d, triangleSize, 2*triangleSize-2, 0d, 0d, -triangleSize);
		else
			this.getPoints().setAll(triangleSize, 0d, 0d, 2*(triangleSize-1), -triangleSize, 0d);
	}
}