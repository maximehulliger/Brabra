package brabra.gui.field;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class StringField extends ValueField<String> {

	private String value;
	private final TextField stringBox;
	private final Label valueLabel;
	private boolean clicked = false;
	
	public StringField(String name, String value) {
		super(name);
		this.value = value;
		
		//--- View:
		this.valueLabel = new Label(value.toString());
		this.stringBox = new TextField(value);
		stringBox.setPrefWidth(55);
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
		this.value = val;
	}

	protected String getModelValue() {
		return value;
	}

	protected String getNewValue() {
		return stringBox.getText();
	}

	protected void updateValue(String newVal) {
		System.out.println(value); //TODO 4 debug
		valueLabel.setText(value);
		stringBox.setText(value);
	}
}