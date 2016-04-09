package brabra.gui.view;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class FloatField extends Field {
	private TextField floatField;
	private Label value;
	private final Consumer<Float> onChange;
	private final Supplier<Float> modelValue;
	
	public FloatField(GridPane root, Consumer<Float> onChange, Supplier<Float> onUpdate, String name) {
		super(root);
		this.onChange = onChange;
		this.modelValue = onUpdate;
		
		floatField = new TextField(modelValue.get().toString());
		floatField.setPrefWidth(105);
		
		super.setName(name);
		
		content.getChildren().add(this.value = new Label(modelValue.get().toString()));
		hidedcontent.getChildren().add(floatField);
		
		// 2. Controller
		floatField.setOnKeyTyped(e ->{this.onChange();});
	}

	private float checkFloat(String input){
		try {
	        Float.parseFloat(input);
	        	return Float.parseFloat(input);
	        } catch (NumberFormatException e) {
	        	return 0;
	    }
	}

	public void onChange() {	
		onChange.accept(this.checkFloat(floatField.getText()));
	}

	public void update() {
		this.value.setText(modelValue.get().toString());
	}
}