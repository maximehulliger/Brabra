package brabra.gui.field;

import java.util.Observable;

import brabra.Brabra;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;

public class QuaternionField extends ValueField<Quaternion> {

	private final Quaternion quaternion;
	private final VectorField rotAxisField;
	private final FloatField angleField;
	
	private float angleValue;
	private final Quaternion lastValidValue;
	
	private final Vector rotAxisValue;
	
	public QuaternionField(String name, Quaternion quaternion) {
		super(name, quaternion, true);
		super.setDefaultValue(Quaternion.identity);
		
		// get quaternion, rotAxis & angle
		this.quaternion = quaternion;
		this.lastValidValue = quaternion.copy();
		final Vector vec = quaternion.rotAxis() == null ? Vector.zero : quaternion.rotAxis(); // TODO: quaternion.isIdentity()
		rotAxisValue = vec;
		angleValue = quaternion.angle();
		
		// create the 2 fields
		rotAxisField = new VectorField(null, rotAxisValue, false);
		rotAxisField.setDefaultValue(null);
		rotAxisField.addOnChange(() -> this.onChange());
		angleField = new FloatField(name, a -> {angleValue = a; onChange();}, () -> angleValue, false);
				
		//--- View:
		contentOpen.getChildren().addAll(rotAxisField, angleField);
	}

	protected void setModelValue(Quaternion val) {
		quaternion.set(val);
	}

	protected Quaternion getModelValue() {
		return quaternion;
	}

	protected Quaternion getNewValue() {
		if (inputDifferent(rotAxisValue, angleValue)) 
			lastValidValue.set(rotAxisValue,angleValue);
		
		return lastValidValue;
		
	}
	
	private boolean inputDifferent(Vector rotAxis, float angle) {
		if (lastValidValue.isIdentity())
			return !rotAxis.equals(Vector.zero) && angle != 0;
		else
			return true;
	}
	
	public void update(Observable o, java.lang.Object arg) {
		super.update(o, arg);
		//rotAxisField.update(o, arg);
		//angleField.update(o, arg);
	}

	protected void setDisplayValue(Quaternion newVal) {
		setValue(newVal.formated());
		if (rotAxisField != null) {
			rotAxisField.setDisplayValue(newVal.rotAxis());
			angleField.setDisplayValue(newVal.angle());
		}
	}

	public static class Pro extends QuaternionField implements Field.Pro {

		public Pro(String name, Quaternion quaternion) {
			super(name, quaternion);
		}

		protected void setModelValue(final Quaternion val) {
			Brabra.app.runLater(() -> super.setModelValue(val));
		}
		
		public Pro respondingTo(Object triggerArg) {
			super.respondingTo(triggerArg);
			return this;
		}
	}
}
