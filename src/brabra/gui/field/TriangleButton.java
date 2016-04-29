package brabra.gui.field;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class TriangleButton extends StackPane {
	
	private final static double triangleSize = 6;
	
	protected final Polygon triangle = new Polygon(); 
	
	
	public TriangleButton() {
		triangle.setFill(Color.BLACK);
		getStyleClass().add("field-triangleButton");
		getChildren().add(triangle);
		setOpen(false);
	}

	public void setOpen(boolean open) {
		if (open)
			triangle.getPoints().setAll(triangleSize, 0d, 0d, 2*triangleSize-2, -triangleSize, 0d);
		else
			triangle.getPoints().setAll(0d, triangleSize, 2*triangleSize-2, 0d, 0d, -triangleSize);
			
	}
}