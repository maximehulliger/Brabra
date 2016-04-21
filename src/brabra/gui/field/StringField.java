package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Brabra;
import javafx.scene.control.TextField;

public class StringField extends ValueField.WithCustomModel<String> {

	private final TextField stringBox = new TextField();
	
	public StringField(Consumer<String> setModelValue, Supplier<String> getModelValue) {
		super(setModelValue, getModelValue, "");
		
		//--- Control:
		stringBox.focusedProperty().addListener(new FieldChangeListener());
		stringBox.setOnAction(e -> onChange());

		//--- View:
		stringBox.setPrefWidth(105);
		contentOpen.getChildren().add(stringBox);
		setValue(getModelValue());
	}
	
	protected String getNewValue() {
		return stringBox.getText();
	}

	protected void setDisplayValue(String newVal) {
		if (open())
			stringBox.setText(newVal);
		else
			setTextValue(newVal);
	}

	public static class Pro extends StringField implements Field.Pro {
		
		public Pro(Consumer<String> setModelValue, Supplier<String> getModelValue) {
			super(setModelValue, getModelValue);
		}

		protected void setModelValue(final String val) {
			Brabra.app.runLater(() -> super.setModelValue(val));
		}
		public Pro respondingTo(Object triggerArg) {
			super.respondingTo(triggerArg);
			return this;
		}
	}
}