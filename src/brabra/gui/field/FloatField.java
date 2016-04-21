package brabra.gui.field;

import java.util.Observable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Brabra;
import brabra.Master;
import brabra.game.physic.Physic;

public class FloatField extends ValueField.WithCustomModel<Float> {
	
	private final StringField floatField;
	
	public FloatField(Consumer<Float> setModelValue, Supplier<Float> getModelValue) {
		super(setModelValue, getModelValue, 0f);
		
		//--- View (&control):
		floatField = new StringField(s -> onChange(), () -> getModelValue().toString());
		floatField.getStyleClass().add("fields-floatField");
		//floatField.setPrefWidth(105);
		floatField.setDefaultValue("0");
		contentClosed.getChildren().add(floatField.contentClosed);
		contentOpen.getChildren().add(floatField.contentOpen);
		setValue(getModelValue());
	}
	
	protected Float getNewValue() {
		return Master.getFloat(floatField.getNewValue(), true);
	}

	protected void setDisplayValue(Float newVal) {
		final String newTextValue = Master.formatFloat(newVal, Physic.epsilon);
		setTextValue(newTextValue);
		floatField.setDisplayValue(newTextValue);
	}
	
	public void update(Observable o, java.lang.Object arg) {
		super.update(o, arg);
		floatField.update(o, arg);
	}

	public static class Pro extends FloatField implements Field.Pro {

		public Pro(Consumer<Float> setModelValue, Supplier<Float> getModelValue) {
			super(setModelValue, getModelValue);
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