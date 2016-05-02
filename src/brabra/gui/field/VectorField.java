package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Brabra;
import brabra.Master;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Vector;
import javafx.scene.control.TextField;

public class VectorField extends ValueField<Vector> {

	private final Vector vector;
	private TextField[] valueFields = new TextField[] {
			new TextField(), new TextField(), new TextField()
	};
	
	public VectorField(Vector vector) {
		
		super(Vector.zero.copy());
		this.vector = vector;
		
		for (TextField field : valueFields) {
			//--- Control:
			field.setOnAction(e->this.onChange());
			field.focusedProperty().addListener(new FieldChangeListener());
			//--- View:
			field.getStyleClass().add("fields-valueFields");
			contentOpen.getChildren().add(field);
		}
		setValue(getModelValue());
	}

	protected void setModelValue(Vector val) {
		vector.set(val);
	}

	protected Vector getModelValue() {
		return vector.copy();
	}

	protected Vector getNewValue() {
		final Float x = Master.getFloat(valueFields[0].getText(), true);
		final Float y = Master.getFloat(valueFields[1].getText(), true);
		final Float z = Master.getFloat(valueFields[2].getText(), true);
		return new Vector(x,y,z);
	}

	protected void setDisplayValue(Vector newVal) {
		if (newVal == null) {
			setTextValue("null");
			for (TextField field : valueFields)
				field.setText("0");
		} else {
			setTextValue(newVal.formated(Physic.epsilon));
			valueFields[0].setText(Master.formatFloat(newVal.x, Physic.epsilon));
			valueFields[1].setText(Master.formatFloat(newVal.y, Physic.epsilon));
			valueFields[2].setText(Master.formatFloat(newVal.z, Physic.epsilon));
		}
	}

	public static class Pro extends VectorField {

		public Pro(Vector vector) {
			super(vector);
		}

		protected void setModelValue(final Vector val) {
			Brabra.app.runLater(() -> super.setModelValue(val));
		}

		public Pro respondingTo(Object triggerArg) {
			super.respondingTo(triggerArg);
			return this;
		}
	}

	public static class ProCustom extends Pro {

		private final static Vector defaultValue = Vector.zero;
		
		private final Consumer<Vector> setModelValue;
		private final Supplier<Vector> getModelValue;
		
		public ProCustom(Consumer<Vector> setModelValue, Supplier<Vector> getModelValue) {
			super(getModelValue.get());
			this.setModelValue = setModelValue;
			this.getModelValue = getModelValue;
			this.setValue(defaultValue);
		}

		protected void setModelValue(Vector val) {
			setModelValue.accept(val);
		}

		protected final Vector getModelValue() {
			return getModelValue == null ? defaultValue : getModelValue.get();
		}
	}
}
