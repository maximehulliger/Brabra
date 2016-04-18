package brabra.gui.field;

import brabra.Master;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Vector;
import javafx.scene.control.TextField;

public class VectorField extends ValueField<Vector> {

	private final Vector vector;
	private final TextField 
			xValue = new TextField(),
			yValue = new TextField(),
			zValue = new TextField();
	
	public VectorField(String name, Vector vector, boolean withTriangle) {
		super(name, withTriangle);
		this.vector = vector;
		
		//--- View:
		xValue.setPrefWidth(55);
		yValue.setPrefWidth(55);
		zValue.setPrefWidth(55);
		contentOpen.getChildren().addAll(xValue, yValue, zValue);
		
		// set text
		updateGUI(vector.copy());
		
		//--- Control:
		xValue.setOnAction(e -> this.onChange());
		yValue.setOnAction(e -> this.onChange());
		zValue.setOnAction(e -> this.onChange());
	}

	protected void setModelValue(Vector val) {
		vector.set(val);
	}

	protected Vector getModelValue() {
		return vector.copy();
	}

	protected Vector getNewValue() {
		final Float x = Master.getFloat(xValue.getText(), true);
		final Float y = Master.getFloat(yValue.getText(), true);
		final Float z = Master.getFloat(zValue.getText(), true);
		return new Vector(x,y,z);
	}

	protected void updateGUI(Vector newVal) {
		super.updateGUI(newVal);
		setValue(newVal.formated(Physic.epsilon));
		xValue.setText(Master.formatFloat(newVal.x, Physic.epsilon));
		yValue.setText(Master.formatFloat(newVal.y, Physic.epsilon));
		zValue.setText(Master.formatFloat(newVal.z, Physic.epsilon));
	}
}
