package brabra.gui.field;

import java.util.function.Consumer;
import java.util.function.Supplier;
import brabra.Master;
import brabra.game.physic.Physic;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class FloatField extends ValueField<Float> {
	
	private final TextField floatField;
	private final Label textValue;
	private final Consumer<Float> setModelValue;
	private final Supplier<Float> getModelValue;
	private boolean clicked = false;
	
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
		floatField.setOnKeyTyped(e -> this.clicked = true);
		this.setOnMouseExited(e-> {
			if (this.clicked) {
				this.onChange();
				this.clicked = false;
			}
		});
	}
	
	private String getFloatValue(Float val){
		if (Math.abs(val) < Physic.epsilon) return "0.0";
		final String a = val.toString();
		if (a.length() > 5) return a.substring(0, 5);
		else return a;
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
		textValue.setText(getFloatValue(newVal));
		floatField.setText(getFloatValue(newVal));
	}
}