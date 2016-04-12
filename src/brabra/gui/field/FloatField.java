package brabra.gui.field;

import java.util.Observable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import brabra.Master;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class FloatField extends Field {
	
	private final TextField floatField;
	private Label textValue;
	private final Consumer<Float> onChange;
	private final Supplier<Float> modelValue;
	
	public FloatField(String name, Consumer<Float> onChange, Supplier<Float> onUpdate) {
		super.setName(name);
		this.onChange = onChange;
		this.modelValue = onUpdate;
		
		//--- View:
		floatField = new TextField(modelValue.get().toString());
		floatField.setPrefWidth(105);
		textValue = new Label(modelValue.get().toString());
		contentClose.getChildren().addAll(basicText, textValue);
		contentOpen.getChildren().addAll(basicText, floatField);
		
		//--- Control:
		floatField.setOnKeyTyped(e ->{this.onChange();});
	}

	public void onChange() {	
		onChange.accept(Master.getFloat(floatField.getText(), true));
	}

	public void update(Observable o, java.lang.Object arg) {
		Float newVal = modelValue.get();
		textValue.setText(newVal.toString());
		floatField.setText(newVal.toString());
	}
}