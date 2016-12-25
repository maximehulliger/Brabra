package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Brabra;
import javafx.scene.control.TextField;

public class StringField extends ValueField.WithCustomModel<String> {

	protected final TextField textField = new TextField();
	
	public StringField(Consumer<String> setModelValue, Supplier<String> getModelValue) {
		super(setModelValue, getModelValue, "");
		
		//--- Control:
		textField.focusedProperty().addListener(new FieldChangeListener());
		textField.setOnAction(e -> onChange());

		//--- View:
		textField.setPrefWidth(60);
		textField.getStyleClass().add("fields-stringBox");
		contentOpen.getChildren().add(textField);
		setValue(getModelValue());
	}
	
	protected String getGUIValue() {
		return textField.getText();
	}

	protected void setGUIValue(String newVal) {
		if (open())
			textField.setText(newVal);
		else
			setTextValue(newVal);
	}

	public static class Pro extends StringField {
		
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