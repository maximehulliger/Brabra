package brabra.gui.field;

import brabra.Master;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class QuaternionField extends ValueField<Quaternion> {

	private final Quaternion quaternion;
	private final TextField xValue;
	private final TextField yValue;
	private final TextField zValue;
	private final TextField angleValue;
	private final Vector rotate;
	private final Label valueLabel;
	private boolean clicked = false;
	
	public QuaternionField(String name, Quaternion quaternion) {
		super(name);
		this.quaternion = quaternion;
		this.rotate = quaternion.rotAxis();
		//--- View:
		this.valueLabel = new Label(quaternion.toString());
		this.xValue = new TextField(Float.toString(rotate.x));
		this.yValue = new TextField(Float.toString(rotate.y));
		this.zValue = new TextField(Float.toString(rotate.z));
		this.angleValue = new TextField(Float.toString(quaternion.angle()));
		xValue.setPrefWidth(55);
		yValue.setPrefWidth(55);
		zValue.setPrefWidth(55);
		angleValue.setPrefWidth(55);
		contentClosed.getChildren().add(valueLabel);
		contentOpen.getChildren().addAll(xValue,yValue,zValue, angleValue);
		
		//--- Control:
		xValue.setOnAction(e -> this.clicked = true);
		yValue.setOnAction(e -> this.clicked = true);
		zValue.setOnAction(e -> this.clicked = true);
		angleValue.setOnAction(e -> this.clicked = true);
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

	protected void setModelValue(Quaternion val) {
		quaternion.set(val);
	}

	protected Quaternion getModelValue() {
		return quaternion;
	}

	protected Quaternion getNewValue() {
		final Float x = Master.getFloat(xValue.getText(), true);
		final Float y = Master.getFloat(yValue.getText(), true);
		final Float z = Master.getFloat(zValue.getText(), true);
		final Float angle = Master.getFloat(angleValue.getText(), true);
		final Vector newRotate = new Vector(x,y,z);
		return new Quaternion(newRotate,angle);
	}

	protected void updateValue(Quaternion newVal) {
		valueLabel.setText(quaternion.toString());	//TODO override the toString to cut the digits
		xValue.setText(getFloatValue(rotate.x));
		yValue.setText(getFloatValue(rotate.y));
		zValue.setText(getFloatValue(rotate.z));
		angleValue.setText(getFloatValue(quaternion.angle()));
	}
}
