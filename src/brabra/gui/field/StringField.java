package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Brabra;
import javafx.scene.control.TextField;

public class StringField extends ValueField.WithCustomValue<String> {

	private TextField stringBox;
	
	public StringField(String name, Consumer<String> setModelValue, Supplier<String> getModelValue, boolean withTriangle) {
		super(name, setModelValue, getModelValue, withTriangle);
		
		//--- View:
		stringBox.setPrefWidth(105);
		contentOpen.getChildren().addAll(stringBox);
		
		//--- Control:
		stringBox.setOnAction(e -> onChange());
	}

	protected String getNewValue() {
		return stringBox.getText();
	}

	protected void setDisplayValue(String newVal) {
		setValue(newVal);
		if (notInitialized())
			stringBox = new TextField();
		
		stringBox.setText(newVal);
	}

	public static class Pro extends StringField implements Field.Pro {
		
		public Pro(String name, Consumer<String> setModelValue, Supplier<String> getModelValue, boolean withTriangle) {
			super(name, setModelValue, getModelValue, withTriangle);
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