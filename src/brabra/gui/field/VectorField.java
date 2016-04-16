package brabra.gui.field;

import brabra.Master;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Vector;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VectorField extends ValueField<Vector> {

	private Vector vector;
	private final TextField xValue;
	private final TextField yValue;
	private final TextField zValue;
	private final Label valueLabel;
	private boolean clicked = false;
	
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
		xValue.setOnAction(e -> this.clicked = true);
		yValue.setOnAction(e -> this.clicked = true);
		zValue.setOnAction(e -> this.clicked = true);
		this.setOnMouseExited(e-> {
			if (this.clicked) {
				this.onChange();
				this.clicked = false;
			}
		});
	}
	
	private String getFloatValue(Float val){
		if (Math.abs(val) < Physic.epsilon) return "0.0";
		final String a = val.toString();
		if (a.length() > 5) return a.substring(0, 5);
		else return a;
	}
	
	private String toString(Vector vector){
		String vectorValue = "[ ";
		vectorValue = vectorValue + getFloatValue(vector.x) + ", ";
		vectorValue = vectorValue + getFloatValue(vector.y) + ", ";
		vectorValue = vectorValue + getFloatValue(vector.z);
		vectorValue = vectorValue + " ]";
		return vectorValue;
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
		valueLabel.setText(toString(newVal));
		xValue.setText(getFloatValue(newVal.x));
		yValue.setText(getFloatValue(newVal.y));
		zValue.setText(getFloatValue(newVal.z));
	}
}
