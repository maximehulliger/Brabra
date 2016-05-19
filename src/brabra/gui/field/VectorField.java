package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Master;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Vector;

public class VectorField extends ValueField<Vector> {

	private FloatField[] valueFields = new FloatField[] {
			new FloatField(), new FloatField(), new FloatField()};
	
	private final Consumer<Vector> setModelValue;
	private final Supplier<Vector> getModelValue;
	
	
	public VectorField(Consumer<Vector> setModelValue, Supplier<Vector> getModelValue) {
		super(getModelValue.get().copy());
		this.setModelValue = setModelValue;
		this.getModelValue = getModelValue;
		//this.setValue(defaultValue);

		for (FloatField field : valueFields) {
			//--- Control:
			field.addOnChange(()->this.onChange());
			field.focusedProperty().addListener(new FieldChangeListener());
			//--- View:
			field.getStyleClass().add("fields-valueFields");
			contentOpen.getChildren().add(field);
		}
		setValue(getModelValue());
	}

	protected void setModelValue(Vector val) {
		setModelValue.accept(val);
	}

	protected final Vector getModelValue() {
		//final Vector modelValue = getModelValue == null ? defaultValue : getModelValue.get();
		return getModelValue.get().copy();
	}
	

	protected Vector getGUIValue() {
//		final Float x = Master.getFloat(valueFields[0].value(), true);
//		final Float y = Master.getFloat(valueFields[1].getText(), true);
//		final Float z = Master.getFloat(valueFields[2].getText(), true);
		return new Vector(valueFields[0].value(),valueFields[1].value(),valueFields[2].value());
	}

	protected void setGUIValue(Vector newVal) {
		if (newVal == null) {
			setTextValue("null");
			for (FloatField field : valueFields)
				field.setValue(0f);
		} else {
			setTextValue(newVal.formated(Physic.epsilon));
			valueFields[0].setValue(Master.epsed(newVal.x, Physic.epsilon));
			valueFields[1].setValue(Master.epsed(newVal.y, Physic.epsilon));
			valueFields[2].setValue(Master.epsed(newVal.z, Physic.epsilon));
		}
	}
	
	// --- For a final vector ---

	/** To deal with a final vector in processing. */
	public static class Final extends VectorField {
		public Final(Vector vector) {
			super(v -> vector.set(v), () -> vector);
		}
	}
}
