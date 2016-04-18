package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class StringField extends ValueField<String> {

	private final Consumer<String> setModelValue;
	private final Supplier<String> getModelValue;
	private final TextField stringBox;
	private final Label valueLabel;
	private boolean clicked = false;
	
	public StringField(String name, Consumer<String> setModelValue, Supplier<String> getModelValue, boolean withTriangle) {
		super(name, withTriangle);
		this.setModelValue = setModelValue;
		this.getModelValue = getModelValue;
		
		//--- View:
		final String val = getModelValue().toString();
		this.valueLabel = new Label(val);
		this.stringBox = new TextField(val);
		stringBox.setPrefWidth(105);
		contentClosed.getChildren().add(valueLabel);
		contentOpen.getChildren().addAll(stringBox);
		
		//--- Control:
		stringBox.setOnAction(e -> this.clicked = true);
		this.setOnMouseExited(e-> {
			if (this.clicked) {
				this.onChange();
				this.clicked = false;
			}
		});
	}

	protected void setModelValue(String val) {
		setModelValue.accept(val);
	}

	protected String getModelValue() {
		return getModelValue.get();
	}

	protected String getNewValue() {
		return stringBox.getText();
	}

	protected void updateGUI(String newVal) {
		super.updateGUI(newVal);
		valueLabel.setText(newVal);
		stringBox.setText(newVal);
	}
}