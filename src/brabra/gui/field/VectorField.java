package brabra.gui.field;

import brabra.Master;
import brabra.game.physic.geo.Vector;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VectorField extends ValueField<Vector> {

	private final Vector vector;
	private final TextField xValue;
	private final TextField yValue;
	private final TextField zValue;
	private final Label valueLabel;
	
	public VectorField(String name, Vector vector) {
		super(name);
		this.vector = vector;
		
		//--- View:
		this.valueLabel = new Label(vector.toString());
		this.xValue = new TextField(Float.toString(vector.x));
		this.yValue = new TextField(Float.toString(vector.y));
		this.zValue = new TextField(Float.toString(vector.z));
		xValue.setPrefWidth(55);
		yValue.setPrefWidth(55);
		zValue.setPrefWidth(55);
		contentClosed.getChildren().add(valueLabel);
		contentOpen.getChildren().addAll(xValue, yValue, zValue);
		
		//--- Control:
		xValue.setOnAction(e -> this.onChange());
		yValue.setOnAction(e -> this.onChange());
		zValue.setOnAction(e -> this.onChange());
	}

	protected void setModelValue(Vector val) {
		vector.set(val);
	}

	protected Vector getModelValue() {
		return vector;
	}

	protected Vector getNewValue() {
		final Float x = Master.getFloat(xValue.getText(), true);
		final Float y = Master.getFloat(yValue.getText(), true);
		final Float z = Master.getFloat(zValue.getText(), true);
		return new Vector(x,y,z);
	}

	protected void updateValue(Vector newVal) {
		valueLabel.setText(vector.toString());
		xValue.setText(Float.toString(vector.x));
		xValue.setText(Float.toString(vector.y));
		xValue.setText(Float.toString(vector.z));
	}
}
