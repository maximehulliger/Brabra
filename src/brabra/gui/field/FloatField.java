package brabra.gui.field;

import java.util.Observable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Brabra;
import brabra.Master;
import brabra.game.physic.Physic;

public class FloatField extends ValueField.WithCustomModel<Float> {
	
	private StringField floatField;
	
	public FloatField(String name, Consumer<Float> setModelValue, Supplier<Float> getModelValue, boolean withTriangle) {
		super(name, setModelValue, getModelValue, withTriangle);
	}
	
	protected Float getNewValue() {
		return Master.getFloat(floatField.getNewValue(), true);
	}

	protected void setDisplayValue(Float newVal) {
		final String newTextValue = Master.formatFloat(newVal, Physic.epsilon);
		setValue(newTextValue);
		if (notInitialized()) {
			//--- View (&control):
			floatField = new StringField(null, s -> onChange(), 
					() -> value() == null ? "0" : value().toString(), false);
			floatField.setPrefWidth(105);
			floatField.setDefaultValue("0");
			contentOpen.getChildren().add(floatField);
			setDefaultValue(0f);
		}
			
		floatField.setDisplayValue(newTextValue);
	}
	
	public void update(Observable o, java.lang.Object arg) {
		super.update(o, arg);
		floatField.update(o, arg);
	}

	public static class Pro extends FloatField implements Field.Pro {

		public Pro(String name, Consumer<Float> setModelValue, Supplier<Float> getModelValue, boolean withTriangle) {
			super(name, setModelValue, getModelValue, withTriangle);
		}

		protected void setModelValue(final Float val) {
			Brabra.app.runLater(() -> super.setModelValue(val));
		}

		public Pro respondingTo(Object triggerArg) {
			super.respondingTo(triggerArg);
			return this;
		}
	}
}