package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;
import brabra.Master;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class FloatField extends ValueField<Float> {
	
	private final TextField floatField;
	private final Label textValue;
	private final Consumer<Float> setModelValue;
	private final Supplier<Float> getModelValue;
	
	public FloatField(String name, Consumer<Float> setModelValue, Supplier<Float> getModelValue) {
		super(name);
		this.setModelValue = setModelValue;
		this.getModelValue = getModelValue;
		
		//--- View:
		final String val = getModelValue().toString();
		floatField = new TextField(val);
		textValue = new Label(val);
		floatField.setPrefWidth(105);
		contentClosed.getChildren().add(textValue);
		contentOpen.getChildren().add(floatField);
		
		//--- Control:
		floatField.setOnKeyTyped(e -> this.onChange());
	}

	protected void setModelValue(Float val) {
		setModelValue.accept(val);
	}

	protected Float getModelValue() {
		return getModelValue.get();
	}

	protected Float getNewValue() {
		return Master.getFloat(floatField.getText(), true);
	}

	protected void updateValue(Float newVal) {
		textValue.setText(newVal.toString());
		floatField.setText(newVal.toString());
	}
}