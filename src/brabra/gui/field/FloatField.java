package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;
import brabra.Master;
import brabra.game.physic.Physic;
import javafx.scene.control.TextField;

public class FloatField extends ValueField<Float> {
	
	private final TextField floatField;
	private final Consumer<Float> setModelValue;
	private final Supplier<Float> getModelValue;
	
	public FloatField(String name, Consumer<Float> setModelValue, Supplier<Float> getModelValue) {
		super(name, true);
		this.setModelValue = setModelValue;
		this.getModelValue = getModelValue;
		
		//--- View:
		final String val = getModelValue().toString();
		floatField = new TextField(val);
		floatField.setPrefWidth(105);
		contentOpen.getChildren().add(floatField);
		
		//--- Control:
		floatField.setOnKeyTyped(e -> onChange());
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

	protected void updateGUI(Float newVal) {
		super.updateGUI(newVal);
		final String newTextValue = Master.formatFloat(newVal, Physic.epsilon);
		setValue(newTextValue);
		floatField.setText(newTextValue);
	}
}