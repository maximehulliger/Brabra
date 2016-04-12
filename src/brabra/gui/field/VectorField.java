package brabra.gui.field;


import java.util.Observable;

import brabra.game.physic.geo.Vector;
import brabra.gui.field.Field;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VectorField extends Field {

	private final Vector vector;
	private final TextField xValue;
	private final TextField yValue;
	private final TextField zValue;
	private final Label valueLabel;
	
	public VectorField(String name, Vector vector) {
		super.setName(name);
		this.vector = vector;
		
		//--- View:
		this.valueLabel = new Label(vector.toString());
		this.xValue = new TextField(Float.toString(vector.x));
		this.yValue = new TextField(Float.toString(vector.y));
		this.zValue = new TextField(Float.toString(vector.z));
		xValue.setPrefWidth(55);
		yValue.setPrefWidth(55);
		zValue.setPrefWidth(55);
		contentClose.getChildren().addAll(basicText, valueLabel);
		contentOpen.getChildren().addAll(basicText, xValue, yValue, zValue);
		
		//--- Control:
		xValue.setOnAction(e -> this.onChange());
		yValue.setOnAction(e -> this.onChange());
		zValue.setOnAction(e -> this.onChange());
	}
	
	private float getFloat(String input){
		try {
			return Float.parseFloat(input);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void onChange() {
		final Float x = getFloat(xValue.getText());
		final Float y = getFloat(yValue.getText());
		final Float z = getFloat(zValue.getText());
		vector.set(x,y,z);
	}

	public void update(Observable o, java.lang.Object arg) {
		valueLabel.setText(vector.toString());
		xValue.setText(Float.toString(vector.x));
		xValue.setText(Float.toString(vector.y));
		xValue.setText(Float.toString(vector.z));
	}
}
