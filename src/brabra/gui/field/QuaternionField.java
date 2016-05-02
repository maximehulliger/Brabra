package brabra.gui.field;

import java.util.Observable;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
	
	public QuaternionField(Quaternion quaternion) {
		super(Quaternion.identity.copy());
		this.quaternion = quaternion;
		this.lastValidValue = quaternion.copy();
		// TODO: quaternion.isIdentity()
		this.rotAxisValue = (quaternion.rotAxis() == null ? Vector.zero : quaternion.rotAxis()).copy();
		this.angleValue = quaternion.angle();
		
		//--- Fields (vector + float):
		rotAxisField = new VectorField(rotAxisValue);
		rotAxisField.setDefaultValue(null);
		rotAxisField.addOnChange(() -> this.onChange());
		angleField = new FloatField(a -> {angleValue = a; onChange();}, () -> this.angleValue);
				
		//--- View:
		contentOpen.getChildren().addAll(rotAxisField.contentOpen, angleField.contentOpen);
		setValue(getModelValue());
	}

	protected void setModelValue(Quaternion val) {
		quaternion.set(val);
	}

	protected Quaternion getModelValue() {
		return quaternion.copy();
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
	}

	protected void setDisplayValue(Quaternion newVal) {
		setTextValue(newVal.formated());
		if (rotAxisField != null) {
			rotAxisField.setDisplayValue(newVal.rotAxis());
			angleField.setDisplayValue(newVal.angle());
		}
	}

	public static class Pro extends QuaternionField {

		public Pro(Quaternion quaternion) {
			super(quaternion);
		}

		protected void setModelValue(final Quaternion val) {
			Brabra.app.runLater(() -> super.setModelValue(val));
		}
		
		public Pro respondingTo(Object triggerArg) {
			super.respondingTo(triggerArg);
			return this;
		}
	}

	public static class ProCustom extends Pro {

		private final static Quaternion defaultValue = Quaternion.identity;
		
		private final Consumer<Quaternion> setModelValue;
		private final Supplier<Quaternion> getModelValue;
		
		public ProCustom(Consumer<Quaternion> setModelValue, Supplier<Quaternion> getModelValue) {
			super(getModelValue.get());
			this.setModelValue = setModelValue;
			this.getModelValue = getModelValue;
			this.setValue(defaultValue);
		}

		protected final void setModelValue(Quaternion val) {
			setModelValue.accept(val);
		}

		protected final Quaternion getModelValue() {
			return getModelValue == null ? defaultValue : getModelValue.get();
		}
	}
}
