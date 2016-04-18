package brabra.gui.field;

import brabra.Master;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import javafx.scene.control.TextField;

public class QuaternionField extends ValueField<Quaternion> {

	private final Quaternion quaternion;
	private final TextField xValue = new TextField();
	private final TextField yValue = new TextField();
	private final TextField zValue = new TextField();
	private final TextField angleField = new TextField();
	
	//private Vector rotAxisValue;
	//private float angleValue;
	
	public QuaternionField(String name, Quaternion quaternion) {
		super(name, true);
		this.quaternion = quaternion;
		//rotAxisValue = quaternion.rotAxis();
		//angleValue = quaternion.angle();
		//--- View:
		xValue.setPrefWidth(55);
		yValue.setPrefWidth(55);
		zValue.setPrefWidth(55);
		angleField.setPrefWidth(70);
		contentOpen.getChildren().addAll(xValue,yValue,zValue, angleField);
		
		//--- Control:
		xValue.setOnAction(e -> onChange());
		yValue.setOnAction(e -> onChange());
		zValue.setOnAction(e -> onChange());
		angleField.setOnAction(e -> onChange());
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
		final Float angle = Master.getFloat(angleField.getText(), true);
		final Vector newRotate = new Vector(x,y,z);
		return new Quaternion(newRotate,angle);
	}

	protected void updateGUI(Quaternion newVal) {
		super.updateGUI(newVal);
		setValue(newVal.formated());
		final Vector rotAxis = newVal.rotAxis();
		xValue.setText(quaternion.isIdentity() ? "0" : Master.formatFloat(rotAxis.x, Physic.epsilon));
		yValue.setText(quaternion.isIdentity() ? "0" : Master.formatFloat(rotAxis.y, Physic.epsilon));
		zValue.setText(quaternion.isIdentity() ? "0" : Master.formatFloat(rotAxis.z, Physic.epsilon));
		angleField.setText(Master.formatFloat(newVal.angle(), Physic.epsilon));
	}
}
